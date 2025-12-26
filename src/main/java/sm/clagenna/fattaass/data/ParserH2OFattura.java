package sm.clagenna.fattaass.data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import sm.clagenna.fattaass.data.rec.RecH2OFattura;
import sm.clagenna.fattaass.data.rec.RecH2OLettura;
import sm.clagenna.fattaass.data.rec.RecH2Oconsumo;
import sm.clagenna.fattaass.enums.ETipoH2OConsumo;
import sm.clagenna.fattaass.enums.ETipoLettProvvenienza;
import sm.clagenna.fattaass.sys.ex.ReadFattPDFException;
import sm.clagenna.stdcla.pdf.ETipiDato;
import sm.clagenna.stdcla.pdf.FromPdf2Html;
import sm.clagenna.stdcla.utils.ParseData;

public class ParserH2OFattura extends ParserFattura implements IParserFatture {
  private static final Logger s_log = LogManager.getLogger(ParserH2OFattura.class);

  @Getter
  private RecH2OFattura       fattura;
  @Getter
  private List<RecH2OLettura> liLetture;
  private int                 inizDettLett;
  @Getter
  private List<RecH2Oconsumo> liConsumi;
  private boolean             stimati;

  public ParserH2OFattura(FattAassModel p_model) {
    super(p_model);
  }

  @Override
  public Logger getLog() {
    return s_log;
  }

  @Override
  public int parse(FromPdf2Html pdf2html) {
    int qtaToks = super.parse(pdf2html);
    if (qtaToks <= 0)
      return 0;

    stimati = false;
    fattura = new RecH2OFattura();
    liLetture = new ArrayList<>();
    liConsumi = new ArrayList<>();
    startIndex(100);
    parsePeriodiVari();
    try {
      parseLetture();
    } catch (ReadFattPDFException e) {
      s_log.error("Errore parsing delle letture, err={}", e.getMessage());
      return -1;
    }

    cercaConsumi();
    parseConsumi();
    parseAssicurazione();
    fillFattura();
    endParsing();
    return mapTgv.size() + liLetture.size() + liConsumi.size();
  }

  private boolean cercaConsumi() {
    boolean bRet = false;
    // becco la prima riga valida di consumi...
    s_log.debug("Vai coi consumi,  indx={}", getCurrIndx());
    int maxIndx = getCurrIndx() + 200;
    checkPoint();
    for (; getCurrIndx() < maxIndx && !bRet; nextIndex())
      bRet = parseWithTokens("servizio", "idrico", "integrato");
    if (bRet)
      commitIndex();
    return bRet;
  }

  private boolean parsePeriodiVari() {
    boolean bRet = false;
    int maxIndx = getCurrIndx() + 150;

    // Dettaglio Letture, salvo l'indx per cominciare da li in parseLetture()
    for (; getCurrIndx() < maxIndx && !bRet; nextIndex()) {
      checkPoint();
      if (parseWithTokens("dettaglio", "letture")) {
        inizDettLett = getCurrIndx();
        commitIndex();
        break;
      }
    }

    // Periodo di fatturazione: dal 01/07/2025 al 31/08/2025
    bRet = false;
    maxIndx = getCurrIndx() + 100;
    for (; getCurrIndx() < maxIndx && !bRet; nextIndex()) {
      checkPoint();
      bRet = parseWithTokens("Periodo", "di", "fatturazione", "dal", ETipiDato.Data);
      if ( !bRet)
        bRet = parseWithTokens("Periodo", "di", "fatturazione:", "dal", ETipiDato.Data);
      if (bRet) {
        LocalDateTime periodFattDa = ParseData.toLocalDateTime(hv.getValData());
        mapTgv.put(Consts.TGV_PeriodFattDtIniz, periodFattDa);
        bRet = parseWithTokens("al", ETipiDato.Data);
        if (bRet) {
          LocalDateTime periodFattAl = ParseData.toLocalDateTime(hv.getValData());
          mapTgv.put(Consts.TGV_PeriodFattDtFine, periodFattAl);
          commitIndex();
          break;
        }
      }
    }
    // Consumi Effettivi: dal 01/07/2025 al 31/08/2025
    bRet = false;
    maxIndx = getCurrIndx() + 100;
    for (; getCurrIndx() < maxIndx && !bRet; nextIndex()) {
      checkPoint();
      bRet = parseWithTokens("Consumi", "effettivi", "dal", ETipiDato.Data);
      if ( !bRet)
        bRet = parseWithTokens("Consumi", "effettivi:", "dal", ETipiDato.Data);
      if (bRet) {
        LocalDateTime periodEffDa = ParseData.toLocalDateTime(hv.getValData());
        mapTgv.put(Consts.TGV_periodConsEffDtIniz, periodEffDa);
        bRet = parseWithTokens("al", ETipiDato.Data);
        if (bRet) {
          LocalDateTime periodEffAl = ParseData.toLocalDateTime(hv.getValData());
          mapTgv.put(Consts.TGV_periodConsEffDtFine, periodEffAl);
          commitIndex();
          break;
        }
      }
    }
    // Consumi Stimati: dal 01/07/2025 al 31/08/2025
    bRet = false;
    maxIndx = getCurrIndx() + 100;
    for (; getCurrIndx() < maxIndx && !bRet; nextIndex()) {
      checkPoint();
      bRet = parseWithTokens("Consumi", "stimati", "dal", ETipiDato.Data);
      if ( !bRet)
        bRet = parseWithTokens("Consumi", "stimati:", "dal", ETipiDato.Data);
      if (bRet) {
        LocalDateTime periodStimDa = ParseData.toLocalDateTime(hv.getValData());
        mapTgv.put(Consts.TGV_periodConsStimDtIniz, periodStimDa);
        bRet = parseWithTokens("al", ETipiDato.Data);
        if (bRet) {
          LocalDateTime periodStimAl = ParseData.toLocalDateTime(hv.getValData());
          mapTgv.put(Consts.TGV_periodConsStimDtFine, periodStimAl);
          commitIndex();
          break;
        }
      }
    }
    return bRet;
  }

  private void fillFattura() {
    fattura.setIdIntesta(getModel().getRecIntesta().getIdIntestaInt());
    // fattura.setDataEmiss((LocalDateTime) getMapTgv().get(Consts.TGV_DataEmiss));
    fattura.setDataEmiss(getDateOrNull(Consts.TGV_DataEmiss));
    //    fattura.setAnnoComp((int) getMapTgv().get(Consts.TGV_annoComp));
    fattura.setAnnoComp(getIntegerOrNull(Consts.TGV_annoComp));
    //    String sz = (String) getMapTgv().get(Consts.TGV_FattNr);
    String sz = getStringOrNull(Consts.TGV_FattNr);
    if (null == sz) {
      s_log.error("Il No Fattura mancante");
      throw new UnsupportedOperationException("Il No Fattura mancante");
    }
    String[] arr = sz.split("/");
    if (null != arr && arr.length == 2) {
      int nrAnno = Integer.parseInt(arr[0]);
      String NrNumero = arr[1];
      fattura.setFattNrAnno(nrAnno);
      fattura.setFattNrNumero(NrNumero);
    } else
      s_log.error("Il No Fattura \"{}\" e' incongruente", sz);

    fattura.setPeriodFattDtIniz(getDateOrNull(Consts.TGV_PeriodFattDtIniz));
    fattura.setPeriodFattDtFine(getDateOrNull(Consts.TGV_PeriodFattDtFine));
    fattura.setPeriodEffDtIniz(getDateOrNull(Consts.TGV_periodConsEffDtIniz));
    fattura.setPeriodEffDtFine(getDateOrNull(Consts.TGV_periodConsEffDtFine));
    fattura.setPeriodStimDtIniz(getDateOrNull(Consts.TGV_periodConsStimDtIniz));
    fattura.setPeriodStimDtFine(getDateOrNull(Consts.TGV_periodConsStimDtFine));

    fattura.setAssicurazione(getDoubleOrNull(Consts.TGV_assicurazione));
    fattura.setImpostaQuiet(getDoubleOrNull(Consts.TGV_impostaQuiet));
    fattura.setRestituzAccPrec(getDoubleOrNull(Consts.TGV_RestituzAccPrec));

    fattura.setTotPagare(getDoubleOrNull(Consts.TGV_TotPagare));
    fattura.setNomeFile(getPdf2html().getFilePDF().toString());
  }

  /**
   * <pre>
   * 89                 mc
   * 1/07/2020          date
   * EFFETTIVA          tipoLett
   * MTSB03BF07285756   Matricola
   * 1,0000000          C
   * 9,0000000          Consumo
   * </pre>
   *
   * @return
   * @throws ReadFattPDFException
   */
  private boolean parseLetture() throws ReadFattPDFException {
    if (inizDettLett < 3)
      throw new ReadFattPDFException("Non ho l'indice per parsare le letture");
    startIndex(inizDettLett);
    int maxIndx = getCurrIndx() + 200;
    boolean stopParse = false;
    for (; getCurrIndx() < maxIndx && !stopParse; nextIndex()) {
      checkPoint();
      RecH2OLettura recLett = new RecH2OLettura();

      if (parseWithTokens("Totale", "Consumi", "mc")) {
        stopParse = true;
        continue;
      }

      // -----  Metri cubici --------
      if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float))
        continue;
      recLett.setLettQtaMc(hv.getvDbl().intValue());

      // -----  Dt Lettura Attuale  --------
      if ( !isMyToken(ETipiDato.Data))
        continue;
      recLett.setLettData(ParseData.toLocalDateTime(hv.getValData()));

      // -----  Tipo Provenienza Lettura  --------
      if ( !isMyToken(ETipiDato.Stringa))
        continue;
      String szTpLett = hv.getTxt();
      ETipoLettProvvenienza tpLett = ETipoLettProvvenienza.parse(szTpLett);
      if (null == tpLett) {
        s_log.error("Non interpreto Tipo Lettura:{}", szTpLett);
        continue;
      }
      recLett.setTipoLett(tpLett);

      // -----  Matricola Contatore  --------
      if ( !isMyToken(ETipiDato.Stringa, ETipiDato.IntN15))
        continue;
      recLett.setMatricola(hv.getTxt());

      // -----  Coeff K --------
      if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float))
        continue;
      recLett.setCoeffK(hv.getvDbl());

      // -----  Consumo  --------
      if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float))
        continue;
      recLett.setConsumo(hv.getvDbl());

      // ------ fine con successo di scansione della riga -----------------
      liLetture.add(recLett);
      commitIndex();
    }
    return true;
  }

  private boolean parseConsumi() {
    int qtaFails = 0;
    boolean stopScan = false;
    for (; getCurrIndx() > 0 && qtaFails < 10 && !stopScan; nextIndex()) {

      checkPoint();
      // startEvidenzia();
      RecH2Oconsumo recCons = new RecH2Oconsumo();
      recCons.setStimato(stimati);

      // ----------------------------------------
      //  restituzione ACCONTI BOLLETTE PRECEDENTI
      if (parseWithTokens("RESTITUZIONE", "ACCONTI", "BOLLETTE", "PRECEDENTI", ETipiDato.Float)) {
        double rimb = hv.getValDouble();
        mapTgv.put(Consts.TGV_RestituzAccPrec, rimb);
        fattura.setRestituzAccPrec(rimb);
        commitIndex();
        continue;
      }

      // ----------------------------------------
      //  CONSUMI EFFETTIVI
      if (parseWithTokens("CONSUMI", "EFFETTIVI")) {
        stimati = false;
        commitIndex();
        continue;
      }

      // ----------------------------------------
      //  CONSUMI STIMATI
      if (parseWithTokens("CONSUMI", "STIMATI")) {
        stimati = true;
        commitIndex();
        continue;
      }

      // ----------------------------------------
      // I Scaglione 1°/2° ... Scaglione
      if (parseWithTokens(ETipiDato.Stringa, "Scaglione")) {
        // recupero il topek con tpd = ETipiDato.Stringa
        String sz = String.format(" %s scaglione", hvtpd.getTxt());
        ETipoH2OConsumo tip = ETipoH2OConsumo.parse(sz);
        if (null == tip) {
          s_log.error("Non interpreto Tipo consumo:{}", sz);
          continue;
        }
        recCons.setTipoSpesa(tip);
        // "scaglione"
        if ( !consumiRestoColonne(recCons))
          continue;
        commitIndex();
        continue;
      }

      // ----------------------------------------
      // Tariffa Ambientale I Scaglione 1°/2° ... Scaglione
      if (parseWithTokens("Tariffa", "Ambientale", ETipiDato.Stringa, "Scaglione")) {
        String sz = String.format("tariffa ambientale %s ", hvtpd.getTxt());
        ETipoH2OConsumo tip = ETipoH2OConsumo.parse(sz);
        if (null == tip) {
          s_log.error("Non interpreto Tipo consumo:{}", sz);
          continue;
        }
        recCons.setTipoSpesa(tip);
        if ( !consumiRestoColonne(recCons))
          continue;
        commitIndex();
        continue;
      }

      // ----------------------------------------
      // Quota Fissa
      if (parseWithTokens("Quota", "fissa")) {
        recCons.setTipoSpesa(ETipoH2OConsumo.QuotaFissa);
        if ( !consumiRestoColonne(recCons))
          continue;
        commitIndex();
      }

      if (parseWithTokens("TOTALE", "SERVIZI", "ACQUEDOTTO")) {
        stopScan = true;
        commitIndex();
        continue;
      }
    }
    return true;
  }

  private boolean consumiRestoColonne(RecH2Oconsumo recCons) {
    boolean bRet = false;
    if ( !isMyToken(ETipiDato.Data)) // data Inizio
      return bRet;
    recCons.setDtIniz(ParseData.toLocalDateTime(hv.getValData()));

    if ( !isMyToken(ETipiDato.Data)) // data Fine
      return bRet;
    recCons.setDtFine(ParseData.toLocalDateTime(hv.getValData()));

    bRet = parseWithTokens("metri", "cubi");
    if ( !bRet)
      bRet = parseWithTokens("giorni");
    if ( !bRet)
      bRet = parseWithTokens("Smc");
    if ( !bRet)
      bRet = parseWithTokens("mc");
    if ( !bRet)
      return bRet;

    if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float)) // prezzo unitario
      return false;
    recCons.setPrezzoUnit(hv.getValDouble());

    if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float)) // quantita
      return false;
    recCons.setQuantita(hv.getvDbl());

    if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float)) // Totale riga
      return false;
    recCons.setImporto(hv.getvDbl());

    liConsumi.add(recCons);
    return true;
  }

  private boolean parseAssicurazione() {
    int qtaFails = 0;
    boolean bRet = false;

    for (; getCurrIndx() > 0 && !bRet && qtaFails < 50; nextIndex()) {
      checkPoint();
      if (parseWithTokens("Assicurazione", ETipiDato.Float)) {
        // -------- Assicurazione  -----------
        mapTgv.put(Consts.TGV_assicurazione, hv.getvDbl());
        fattura.setAssicurazione(hv.getvDbl());
        commitIndex();
        return true;
      }
      qtaFails++;
    }
    commitIndex();
    return false;
  }

}
