package sm.clagenna.fattaass.data;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.fattaass.enums.ETipoFatt;
import sm.clagenna.fattaass.sql.BaseSqlServ;
import sm.clagenna.fattaass.sql.FactorySql;
import sm.clagenna.fattaass.sys.ex.ReadFattException;
import sm.clagenna.stdcla.pdf.ETipiDato;
import sm.clagenna.stdcla.pdf.FromPdf2Html;
import sm.clagenna.stdcla.pdf.HtmlValue;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public abstract class ParserFattura implements IParserFatture {

  protected static final Pattern pat_Page = Pattern.compile("Pag\\.([0-9]+)");

  @Getter @Setter
  private FattAassModel         model;
  @Getter @Setter
  private ETipoFatt             tipoFattura;
  @Getter @Setter
  private Integer               idFattura;
  @Getter
  protected List<HtmlValue>     liVals;
  protected Map<String, Object> mapTgv;
  @Getter @Setter
  private boolean               debug;
  private int                   currIndx;
  private int                   backTrackIndex;
  private int                   lastGoodIndex;
  @Getter
  private int                   inizDett;
  @Getter
  private int                   actIndx;
  protected HtmlValue           hv;
  /**
   * Il tipo dato assegnato dalla {@link #isMyToken(ETipiDato...)} oppure dalla
   * {@link #parseWithTokens(Object...)} in cui passo un elemento di tipo
   * {@link ETipiDato}
   */
  protected HtmlValue           hvtpd;
  @Getter
  protected int                 pagNo;
  @Setter
  private int                   tipoDebugPrint;
  @Setter
  protected boolean             evidenziaTokens;
  private List<HtmlValue>       liPwtHv;
  @Getter @Setter
  private FromPdf2Html          pdf2html;
  @Getter @Setter
  private String                threadName;

  public ParserFattura() {
    tipoDebugPrint = 1;
  }

  public ParserFattura(FattAassModel p_model) {
    tipoDebugPrint = 1;
    setModel(p_model);
  }

  public abstract Logger getLog();

  @Override
  public int parse(FromPdf2Html pdf2html) {
    setPdf2html(pdf2html);
    liVals = pdf2html.getLiHtml(); // .stream().filter(h -> h.getTipoDato() != ETipiDato.HTML).toList() ;
    mapTgv = new TreeMap<>();
    currIndx = 0;
    actIndx = 0;

    if (null == tipoFattura && !parseTipoFatt()) {
      getLog().error("Non ho capito che tipo di fattura sia:{}", pdf2html.getFilePDF().toString());
      return 0;
    }
    parseFatturaNo();
    parseDataEmiss();
    parseTotalePag();
    parseDtScad();
    parsePeriodoFatt();
    return mapTgv.size();
  }
  
  protected void endParsing() {
    getLog().debug("{}, Fine Parsing Fattura {} \"{}\", indx={}", //
        getThreadName(), //
        getTipoFattura().getTitolo(), //
        getFattura().getNomeFile(), //
        getCurrIndx());
  }

  protected boolean parseTipoFatt() {
    int goodHtm = contaHtmlValidi(100);
    if (0 == goodHtm)
      return false;
    checkPoint();
    for (; getCurrIndx() < 40; nextIndex()) {
      if (parseWithTokens("Servizio", "Energia", "Elettrica")) {
        setTipoFattura(ETipoFatt.EnergiaElettrica);
        commitIndex();
        return true;
      }
    }
    backTrack();
    for (; getCurrIndx() < 40; nextIndex()) {
      if (parseWithTokens("Servizio", "Gas", "Naturale")) {
        setTipoFattura(ETipoFatt.GAS);
        commitIndex();
        return true;
      }
    }
    backTrack();
    for (; getCurrIndx() < 40; nextIndex()) {
      if (parseWithTokens("Servizio", "Idrico", "Integrato")) {
        setTipoFattura(ETipoFatt.Acqua);
        commitIndex();
        return true;
      }
    }
    return false;
  }

  private int contaHtmlValidi(int maxI) {
    checkPoint();
    HtmlValue tok = nextToken();
    if (null != tok)
      return getActIndx();
    return 0;
  }

  protected boolean parseFatturaNo() {
    int maxIndx = getCurrIndx() + 100;
    ETipiDato[] arrtp = new ETipiDato[] { ETipiDato.Barrato, ETipiDato.Stringa };
    checkPoint();
    for (; getCurrIndx() < maxIndx; nextIndex()) {
      if (parseWithTokens("Fattura", "N.", arrtp)) {
        mapTgv.put(Consts.TGV_FattNr, hv.getTxt());
        commitIndex();
        return true;
      }
    }
    backTrack();
    return false;
  }

  protected boolean parseDataEmiss() {
    int maxIndx = getCurrIndx() + 100;
    ETipiDato[] arrtp = new ETipiDato[] { ETipiDato.Data };
    checkPoint();
    for (; getCurrIndx() < maxIndx; nextIndex()) {
      if (parseWithTokens("Data", "Emissione", arrtp)) {
        mapTgv.put(Consts.TGV_DataEmiss, ParseData.toLocalDateTime(hv.getValData()));
        debugEstrVal();
        commitIndex();
        return true;
      }
    }
    backTrack();
    return false;
  }

  /**
   * Tornero 'False' fintanto che non trovo la sequenza di hv giusta
   *
   * <pre>
   * Totale
   * da
   * Pagare
   * 304,33
   * </pre>
   *
   * @return
   */
  protected boolean parseTotalePag() {
    int maxIndx = getCurrIndx() + 100;
    checkPoint();
    for (; getCurrIndx() < maxIndx; nextIndex()) {
      if (parseWithTokens("Totale", "da", "Pagare", ETipiDato.Float)) {
        Double totDaPag = hv.getValDouble();
        // -------- Totale da Pagare nnn,nn Euro ---------
        if ( !isMyToken("€"))
          return false;
        // --------------------------------------------
        mapTgv.put(Consts.TGV_TotPagare, totDaPag);
        commitIndex();
        return true;
      }
    }
    backTrack();
    return false;
  }

  private boolean parseDtScad() {
    int maxIndx = getCurrIndx() + 100;
    checkPoint();
    for (; getCurrIndx() < maxIndx; nextIndex()) {
      if (parseWithTokens("Data", "Scadenza", ETipiDato.Data)) {
        LocalDateTime dtScad = ParseData.toLocalDateTime(hv.getValData());
        mapTgv.put(Consts.TGV_dtScad, dtScad);
        commitIndex();
        return true;
      }
    }
    backTrack();
    return false;
  }

  /**
   * <pre>
   * Periodo
   * di
   * fatturazione
   * dal
   * 01/08/2020
   * al
   * 31/12/2020
   * </pre>
   *
   * @return
   */
  private boolean parsePeriodoFatt() {
    int maxIndx = getCurrIndx() + 200;
    boolean bRet = false;
    checkPoint();
    for (; getCurrIndx() < maxIndx; nextIndex()) {
      bRet = parseWithTokens("Periodo", "di", "Fatturazione", "dal", ETipiDato.Data);
      if ( !bRet)
        bRet = parseWithTokens("Periodo", "di", "Fatturazione:", "dal", ETipiDato.Data);
      if (bRet) {
        LocalDateTime periodDa = ParseData.toLocalDateTime(hv.getValData());
        mapTgv.put(Consts.TGV_PeriodFattDtIniz, periodDa);
        if (parseWithTokens("al", ETipiDato.Data)) {
          LocalDateTime periodAl = ParseData.toLocalDateTime(hv.getValData());
          mapTgv.put(Consts.TGV_PeriodFattDtFine, periodAl);
          int annoComp = periodDa.getYear();
          mapTgv.put(Consts.TGV_annoComp, annoComp);
          commitIndex();
          return bRet;
        }
      }
    }
    return bRet;
  }

  /**
   * Partendo dal indice {@link #currIndx} scandisce {@link #liVals} e verifica
   * che consecutivo token sia del tipo fornito come argomento (vararg), es:
   *
   * <pre>
   * parseWithTokens("Servizio", "Energia", "Elettrica")
   * parseWithTokens("Totale", "da", "Pagare", ETipiDato.Float)
   * </pre>
   *
   * <b>NOTA</b>: il vararg puo' contenere anche un array di tipo
   * {@link ETipiDato}, es:
   *
   * <pre>
   * ETipiDato[] arrtp = new ETipiDato[] { ETipiDato.Barrato, ETipiDato.Stringa };
   * parseWithTokens("Fattura", "N.", arrtp)
   * </pre>
   *
   * La tipologia dentro l'array "arrtp" verra' valutato in <b>OR</b> sul token
   * corrente.<br/>
   * Se la valutazione è corretta allora la funzione:
   * <ul>
   * <li>torna true</li>
   * <li>currIndx viene spostato di qta elementi contenuti nel vararg</li>
   * </ul>
   *
   * @param ele
   * @return true se la sequenza e' corretta
   */
  protected boolean parseWithTokens(Object... ele) {
    int bckActIndx = actIndx;
    for (Object ob : ele) {
      if ( !isMyToken(ob)) {
        actIndx = bckActIndx;
        return false;
      }
      evidenzia();
    }
    return true;
  }

  /**
   * Come la {@link #parseWithTokens(Object...)} ma con una stringa con piu
   * parole che poi splitto in un vararg di singole word da passare alla
   * {@link #parseWithTokens(Object...)}
   *
   * @param ele
   * @return true se la sequenza e' corretta
   */
  protected boolean parseWithTokens(String ele) {
    if (null == ele || ele.length() == 0)
      return false;
    String[] arr = ele.split(" ");
    List<String> li = new ArrayList<>();
    for (String sz : arr) {
      String sz2 = sz.trim();
      if (sz2.trim().length() > 0)
        li.add(sz2);
    }
    return parseWithTokens(li.toArray());
  }

  protected int cercaInizio(String tit) {
    if (null == tit)
      throw new UnsupportedOperationException("Non hai fornito la stringa di ricerca");
    String[] arr = tit.split(" ");
    List<String> li = new ArrayList<>();
    for (String sz : arr) {
      String sz2 = sz.trim();
      if (sz2.trim().length() > 0)
        li.add(sz2);
    }
    return cercaInizio(li.toArray(new String[0]));
  }

  protected int cercaInizio(String... tit) {
    boolean bRet = false;
    // becco la prima riga valida delle Letture ...
    int maxIndx = getCurrIndx() + 200;
    checkPoint();
    for (; getCurrIndx() < maxIndx && !bRet; nextIndex()) {
      if (parseWithTokens((Object[]) tit)) {
        commitIndex();
        inizDett = getCurrIndx();
        return inizDett;
      }
    }
    return -1;
  }

  /**
   * Solo se il flag {@link #evidenziaTokens} e' true,<br/>
   * Inizializza l'array delle evidenziazioni della chiamata
   * {@link #evidenzia()}
   */
  private void startEvidenzia() {
    if ( !evidenziaTokens /* || null != liPwtHv */ )
      return;
    liPwtHv = new ArrayList<>();
  }

  /**
   * Solo se il flag {@link #evidenziaTokens} e' true,<br/>
   * Registra il token HTML per l'evidenziazione (background-color: yellow;),
   * vedi {@link #endEvidenzia()}
   */
  protected void evidenzia() {
    if ( !evidenziaTokens || null == liPwtHv)
      return;
    if ( !liPwtHv.contains(hv))
      liPwtHv.add(hv);
  }

  /**
   * Solo se il flag {@link #evidenziaTokens} e' true,<br/>
   * evidenzia tutto cio' che è stato registrato nel array {@link #liPwtHv}
   * partendo dalla {@link #startEvidenzia()} in poi con le successive chiamate
   * a {@link #evidenzia()}
   */
  protected void endEvidenzia() {
    if ( !evidenziaTokens || null == liPwtHv || liPwtHv.size() == 0)
      return;
    liPwtHv.stream().forEach(h -> h.evidenzia());
    liPwtHv = null;
  }

  /**
   * Verifica che il token presente al "actIndex" può essere:
   * <ul>
   * <li>Testo (stringa) uguale a <code>(String) ob</code></li>
   * <li>Tipologia di dato del tipo <code>(ETipoDato) ob</code></li>
   * </ul>
   * Con la particolarita che #ETipoDato può essere un array (vararg) allora il
   * test avviene in <b>OR</b> fra tutti
   *
   * @param ob
   * @return
   */
  public boolean isMyToken(Object ob) {
    if (ob instanceof String str)
      return isMyToken(str);
    if (ob instanceof ETipiDato tpd) {
      return isMyToken(new ETipiDato[] { tpd });
    }
    if (ob instanceof ETipiDato[] tpdarr)
      return isMyToken(tpdarr);
    return false;
  }

  @Override
  public boolean isMyToken(String sz) {
    int lAct = actIndx;
    nextToken();
    if (isThatText(sz)) {
      return true;
    }
    actIndx = lAct;
    return false;
  }

  @Override
  public boolean isMyToken(ETipiDato... tp) {
    nextToken();
    for (ETipiDato ll : tp)
      if (hv.getTipoDato() == ll) {
        hvtpd = hv;
        evidenzia();
        return true;
      }
    return false;
  }

  /**
   * Funzione che deposita in {@link #hv} il token di tipo {@link HtmlValue}. E'
   * l'indice attuale {@link #actIndx} che punta nel array di {@link #liVals}
   * che guida l'estrazione
   */
  @Override
  public HtmlValue nextToken() {
    if (actIndx < 0) {
      return null;
    }
    // elimino gli eventuali tag di tipo "HTML" che non danno utile
    do {
      if (actIndx >= liVals.size())
        return null;
      //      if (actIndx < 0 || actIndx >= liVals.size()) {
      //        getLog().error("Parse HTML Tokens with index out of range {}, size={}", actIndx, liVals.size());
      //        return null;
      //      }
      debugEstrVal();
      lastGoodIndex = actIndx;
      hv = liVals.get(actIndx++);
    } while (hv.getTipoDato() == ETipiDato.HTML);
    // vado alla ricerca di una stringa con il valore del numero pagina
    if (hv.getTipoDato() != ETipiDato.Stringa)
      return hv;
    // cerco la dicitura di Pagina "Pag.nn"
    String sz = hv.getTxt();
    if (null == sz || !sz.toLowerCase().startsWith("pag"))
      return hv;
    // cerco il numero di pagina nella forma "Pag.nn"
    Matcher mt = pat_Page.matcher(sz);
    try {
      if (mt.find()) {
        String sz2 = mt.group(1);
        pagNo = Integer.parseInt(sz2);
      }
    } catch (NumberFormatException e) {
      getLog().error("Num pagina non parseable:{}", sz);
    }
    return hv;
  }

  public void backTrack() {
    startIndex(backTrackIndex);
    actIndx = backTrackIndex;
  }

  /**
   * Indica l'inizio della sequenza che stiamo cercando.<br/>
   * Ferma il punto di ripristino del {@link #backTrack()} nel caso falisse la
   * sequenza
   */
  public void checkPoint() {
    backTrackIndex = currIndx;
    startEvidenzia();
  }

  public int getCurrIndx() {
    return currIndx;
  }

  /**
   * Mi riporto al punto di {@link #checkPoint()} per ricominciare la scansione
   * dal vecchio punto
   *
   * @param iv
   */
  public void startIndex(int iv) {
    currIndx = iv;
    actIndx = iv;
  }

  public int nextIndex() {
    actIndx = ++currIndx;
    if (currIndx >= liVals.size())
      currIndx = -1;
    return currIndx;
  }

  /**
   * Quando sono sicuro di aver interpretato tutta la stringa come me
   * l'aspettavo allora sposto il cursore sull'ultimo <i>token valido</i>
   */
  public void commitIndex() {
    currIndx = lastGoodIndex;
    actIndx = lastGoodIndex;
    endEvidenzia();
  }

  @Override
  public boolean isThatText(String str) {
    if (hv.getTipoDato() != ETipiDato.Stringa)
      return false;
    String sz = hv.getTxt();
    if (null == sz)
      return false;
    boolean bRet = sz.equalsIgnoreCase(str);
    if (bRet && null != liPwtHv) {
      evidenzia();
    }
    return bRet;
  }

  @Override
  public LocalDateTime getDateOrNull(String tgNam) {
    if (null == mapTgv)
      return null;
    Object obj = mapTgv.get(tgNam);
    LocalDateTime ldt = ParseData.toLocalDateTime(obj);
    return ldt;
  }

  @Override
  public LocalDateTime getDate(String tgNam) throws ReadFattException {
    if (null == mapTgv)
      throw new ReadFattException("Nessun map di Tagged Values");
    Object obj = mapTgv.get(tgNam);
    LocalDateTime ldt = ParseData.toLocalDateTime(obj);
    return ldt;
  }

  @Override
  public int getIntegerOrNull(String tgNam) {
    if (null == mapTgv)
      return 0;
    Object obj = mapTgv.get(tgNam);
    if (null == obj)
      return 0;
    Integer ii = Utils.parseInt(obj);
    return ii;
  }

  @Override
  public Integer getInteger(String tgNam) throws ReadFattException {
    if (null == mapTgv)
      throw new ReadFattException("Nessun map di Tagged Values");
    Object obj = mapTgv.get(tgNam);
    if (null == obj)
      throw new ReadFattException("Nessun TGV di nome : tgNam");
    Integer ii = Utils.parseInt(obj);
    return ii;
  }

  @Override
  public String getStringOrNull(String tgNam) {
    if (null == mapTgv)
      return null;
    Object obj = mapTgv.get(tgNam);
    if (null == obj)
      return null;
    String sz = obj.toString();
    return sz;
  }

  @Override
  public String getString(String tgNam) throws ReadFattException {
    if (null == mapTgv)
      throw new ReadFattException("Nessun map di Tagged Values");
    Object obj = mapTgv.get(tgNam);
    if (null == obj)
      throw new ReadFattException("Nessun TGV di nome : tgNam");
    String sz = obj.toString();
    return sz;
  }

  @Override
  public double getDoubleOrNull(String tgNam) {
    if (null == mapTgv)
      return 0f;
    Object obj = mapTgv.get(tgNam);
    if (null == obj)
      return 0f;
    Double dbl = Utils.parseDouble(obj);
    return dbl;
  }

  @Override
  public Double getDouble(String tgNam) throws ReadFattException {
    if (null == mapTgv)
      throw new ReadFattException("Nessun map di Tagged Values");
    Object obj = mapTgv.get(tgNam);
    if (null == obj)
      throw new ReadFattException("Nessun TGV di nome : tgNam");
    Double dbl = Utils.parseDouble(obj);
    return dbl;
  }

  protected void debugEstrVal() {
    if ( !debug)
      return;
    StringBuilder sb = new StringBuilder();
    int indx, indxTo;

    switch (tipoDebugPrint) {
      // da currIndx <--> actIndx
      case 1:
        indx = currIndx - 1;
        if (indx < 0)
          indx = 0;
        indxTo = indx + 10;
        if (indxTo <= actIndx)
          indxTo = actIndx + 1;
        if (indxTo > liVals.size())
          indxTo = liVals.size();
        break;
      // dintorni di actIndx
      case 2:
        indx = actIndx - 3;
        if (indx < 0)
          indx = 0;
        indxTo = indx + 10;
        if (indxTo > liVals.size())
          indxTo = liVals.size();
        break;
      default:
        return;
    }

    String sz = null;
    for (int j = indx; j < indxTo; j++) {
      HtmlValue lhv = liVals.get(j);
      sz = lhv.getTxt();
      switch (lhv.getTipoDato()) {
        case Aster:
          sz = "*";
          break;
        case Minus:
          sz = "-";
          break;
        case Less:
        case Minor:
          sz = "<";
          break;
        case Great:
          sz = ">";
          break;
        case Barrato:
          sz = lhv.getFattNo();
          break;
        case Data:
          sz = ParseData.formatDate(lhv.getValData());
          break;
        case Ora:
          sz = ParseData.formatOra(lhv.getValData());
          break;
        case Email:
          sz = lhv.getTxt();
          break;
        case Float:
          sz = Utils.formatDouble(lhv.getvDbl());
          break;
        case HTML:
          sz = "<HTML>";
          break;
        case Intero:
          Double dbl = lhv.getvDbl();
          if (null != dbl)
            sz = Integer.valueOf(dbl.intValue()).toString();
          else
            System.out.printf("ParserFattura.debugEstrVal(%s)\n", lhv.getRigaHtml());
          break;
        case IntN15:
          sz = lhv.getTxt();
          break;
        case MinMax:
          sz = "<:>";
          break;
        case PAper:
          sz = "(";
          break;
        case PChiu:
          sz = ")";
          break;
        case Perc:
          sz = "%";
          break;
        case Plus:
          sz = "+";
          break;
        case QAper:
          sz = "[";
          break;
        case QChiu:
          sz = "]";
          break;
        case Stringa:
          break;
        default:
          break;
      }
      if (null == sz || sz.length() == 0)
        sz = "?";
      int lenForm = 20;
      if (sz.length() > lenForm)
        sz = sz.substring(0, lenForm - 3) + "...";
      if (sz.length() < lenForm)
        sz += " ".repeat(lenForm - sz.length());
      sz += "|";
      sb.append(sz);
    }
    sb.append("\n");
    for (int j = indx; j < indxTo; j++) {
      sz = "";
      String szNum = String.format("(%d)", j);
      if (j == actIndx) {
        sz = "----+----A----+-----|";
      }
      if (j == currIndx) {
        if (sz.length() == 0)
          sz = "----+----C----+-----|";
        else
          sz = sz.replace("A-", "CA");
      }
      if (sz.length() == 0)
        sz = String.format("%-20s|", "-".repeat(20));
      String sz1 = sz.substring(0, (17 - szNum.length()));
      String sz2 = sz.substring(17);
      sz = sz1 + szNum + sz2;
      sb.append(sz);
    }
    sb.append("\n");
    System.out.println(sb.toString());
  }

  public void printTGV() {
    if (null == mapTgv) {
      System.out.println("map Tagged Values is **NULL**");
      return;
    }
    // mapTgv.keySet().stream().toList()
    for (String key : mapTgv.keySet()) {
      System.out.printf("%-16s%s\n", key, mapTgv.get(key));
    }
  }

  @Override
  public String toString() {
    String szRet = "* NULL *";
    if (null == mapTgv || mapTgv.size() == 0)
      return szRet;
    szRet = "-------- Tagged values ----------\n";
    szRet += mapTgv //
        .keySet() //
        .stream() //
        .map(key -> String.format("%-20s%s", key, mapTgv.get(key))) //
        .collect(Collectors.joining("\n"));
    return szRet;
  }

  public Integer cercaIdFatturaInDB() {
    BaseSqlServ sql = (BaseSqlServ) FactorySql.getFatturaInserter(getTipoFattura(), null);
    setIdFattura(null);
    sql.setParsePdf(this);
    sql.setModel(model);
    LocalDateTime dtEmiss = null;
    try {
      dtEmiss = getDate(Consts.TGV_DataEmiss);
      if (sql.fatturaExist())
        setIdFattura(sql.getIdFattura());
    } catch (SQLException | ReadFattException e) {
      getLog().error("Non trovo l'id {}Fattura per emiss = {}", getTipoFattura().getTitolo(), ParseData.formatDate(dtEmiss));
    }
    return idFattura;
  }

}
