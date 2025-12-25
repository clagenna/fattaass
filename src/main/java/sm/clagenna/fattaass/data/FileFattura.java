package sm.clagenna.fattaass.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.fattaass.enums.ETipoFatt;
import sm.clagenna.fattaass.sql.ISql;
import sm.clagenna.stdcla.pdf.FromPdf2Html;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.sql.EServerId;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;
import sm.clagenna.stdcla.utils.sys.TimerMeter;

public class FileFattura implements Callable<FileFattura> {
  private static final Logger s_log  = LogManager.getLogger(FileFattura.class);
  private static int          s_seed = 0;

  private int           idFatt;
  private FattAassModel model;
  @Getter @Setter
  private RecIntesta    recint;
  @Getter @Setter
  private boolean       selezionato;
  @Getter @Setter
  private boolean       changed;
  @Getter @Setter
  private boolean       inDb;
  @Getter @Setter
  private boolean       fileExist;
  @Getter @Setter
  private boolean       inError;
  @Getter @Setter
  private int           idFattura;
  @Getter @Setter
  private ETipoFatt     tipoFatt;
  @Getter @Setter
  private int           annoComp;
  @Getter @Setter
  private LocalDateTime dtEmiss;
  @Getter @Setter
  private LocalDateTime dtIniz;
  @Getter // @Setter
  private LocalDateTime dtFine;
  @Getter @Setter
  private Double        totFattura;
  @Getter @Setter
  private Path          fullPath;
  private Random        random;
  @Getter
  private String        threadName;
  private String        szThId;
  @Getter @Setter
  ETipoBatch            tipoBatch;

  public static enum ETipoBatch {
    indovina, importa
  }

  public FileFattura(FattAassModel p_model) {
    model = p_model;
    recint = model.getRecIntesta();
    random = new Random(new Date().getTime());
    tipoBatch = ETipoBatch.indovina;
    idFatt = FileFattura.s_seed++;
  }

  public int getIdIntesta() {
    if (null != recint)
      return recint.getIdIntestaInt();
    return -1;
  }

  public FileFattura popolaDaDB(DtsRow row) {
    try {
      String szTpFatt = (String) row.get(Consts.SqlCol_tipoFatt);
      ETipoFatt tpf = ETipoFatt.parse(szTpFatt);

      setSelezionato(false);
      setInDb(true);

      setIdFattura((Integer) row.get(Consts.SqlCol_idFattura));
      setTipoFatt(tpf);
      String szFile = (String) row.get(Consts.SqlCol_fullPath);
      setFullPath(Paths.get(szFile));
      setAnnoComp((Integer) row.get(Consts.SqlCol_annoComp));
      setDtEmiss(ParseData.toLocalDateTime(row.get(Consts.SqlCol_DataEmiss)));
      setDtIniz(ParseData.toLocalDateTime(row.get(Consts.SqlCol_DtIniz)));
      setDtFine(ParseData.toLocalDateTime(row.get(Consts.SqlCol_DtFine)));
      setTotFattura((Double) row.get(Consts.SqlCol_totFattura));
      setFileExist(Files.exists(fullPath, LinkOption.NOFOLLOW_LINKS));
      if (annoComp == 0 && null != dtIniz)
        annoComp = dtIniz.getYear();
    } catch (Exception e) {
      s_log.error("Popola FileFattura da DB, err={}", e.getMessage(), e);
    }
    return this;
  }

  public void setDtFine(LocalDateTime dd) {
    dtFine = dd;
    //    String sz = ParseData.formatDate(dd);
    //    if (null != sz && sz.length() > 10)
    //      sz = sz.substring(0, 10);
    //    if (null != parser) {
    //      sz = ParseData.formatDate((LocalDateTime) parser.getMapTgv().get(Consts.TGV_PeriodFattDtFine));
    //      if (null != sz && sz.endsWith("-01"))
    //        System.out.println("FileFattura.indovinaFatturaDaPDF()");
    //    }
    //    if (null != sz && sz.endsWith("-01"))
    //      System.out.println("FileFattura.indovinaFatturaDaPDF()");
  }

  public void indovinaFatturaDaPDF() {
    FromPdf2Html pdf2Html = new FromPdf2Html();
    pdf2Html.setDebug(model.isDebug());
    pdf2Html.setSaveHTML(model.isSaveHTML());
    pdf2Html.setSaveCSV(model.isSaveCSV());
    pdf2Html.setSaveTXT(model.isSaveTXT());
    pdf2Html.parsePDF(getFullPath());

    ParserFatturaSoloTipo parser = new ParserFatturaSoloTipo(model);
    parser.evidenziaTokens(model.isSaveHTML());
    int qtaToks = parser.parse(pdf2Html);
    if (qtaToks <= 0) {
      inError = true;
      return;
    }
    setRecint(recint);
    setSelezionato(false);
    setInDb(false);
    setFileExist(true);
    setTipoFatt(parser.getTipoFattura());
    setDtIniz(parser.getDateOrNull(Consts.TGV_PeriodFattDtIniz));
    setDtFine(parser.getDateOrNull(Consts.TGV_PeriodFattDtFine));
    if (null != getDtIniz())
      setAnnoComp(getDtIniz().getYear());
    setDtEmiss(parser.getDateOrNull(Consts.TGV_DataEmiss));
    setTotFattura(parser.getDoubleOrNull(Consts.TGV_TotPagare));
  }

  public void importaFileFattura() {
    FromPdf2Html pdf2html = new FromPdf2Html();

    pdf2html.setDebug(model.isDebug());
    pdf2html.setSaveHTML(model.isSaveHTML());
    pdf2html.setSaveCSV(model.isSaveCSV());
    pdf2html.setSaveTXT(model.isSaveTXT());
    pdf2html.parsePDF(getFullPath());
    setFileExist(true);
    setInError(false);
    ParserFattura parser = (ParserFattura) ParserFactory.get(getTipoFatt(), model);
    parser.setEvidenziaTokens(model.isSaveHTML());
    parser.setDebug(model.isDebug());
    parser.setThreadName(getThreadName());
    int qtaToks = parser.parse(pdf2html);
    if (qtaToks <= 0) {
      return;
    }
    // salvo l'HTML con le evidenziazioni in giallo
    if (model.isSaveHTML()) {
      pdf2html.saveHtml("_3");
    }
    // printParsedValues();
    insertIntoDB(parser);
  }

  private void insertIntoDB(ParserFattura parser) {
    EServerId serverId = model.getDbconn().getServerId();
    ISql sql = model.getFatturaInserter(parser.getTipoFattura(), serverId, model.isSingleThread());
    if ( !model.isSingleThread())
      s_log.debug("{} Creato nuova DBConn", getThreadName());
    sql.init(parser, model);
    sql.setThreadName(getThreadName());
    //    if (model.isDebug())
    //      ((EExSqlServ) sql).setShowStatement(false);
    try {
      if (sql.fatturaExist()) {
        sql.deleteFattura();
      }
      sql.insertNewFattura();
      sql.insertNewLettura();
      sql.insertNewConsumo();
      setInDb(true);
      model.firePropertyChange(Consts.EVT_FattInDbInserted, null, sql);
    } catch (SQLException e) {
      s_log.error("{}, Errore import fattura tipo \"{}\" file \"{}\", err={}", //
          getThreadName(), //
          getTipoFatt().getTitolo(), //
          getFullPath().toString(), //
          e.getMessage());
    } finally {
      try {
        if ( !model.isSingleThread()) {
          sql.close();
          s_log.debug("{} close DBConn", getThreadName());
        }
      } catch (IOException e) {
        s_log.error("Errore close multithreaded dbconn, err=", e.getMessage());
      }
    }
  }

  public void update(FileFattura ff2) {
    model = ff2.model;
    setRecint(ff2.recint);
    setSelezionato(ff2.isSelezionato());
    setInDb(ff2.isInDb());
    setInError(ff2.isInError());
    setFileExist(Files.exists(fullPath, LinkOption.NOFOLLOW_LINKS));
    if (0 != ff2.getIdFattura())
      setIdFattura(ff2.getIdFattura());
    if (null != ff2.getTipoFatt())
      setTipoFatt(ff2.getTipoFatt());
    setFullPath(ff2.getFullPath());
    if (0 != ff2.getAnnoComp())
      setAnnoComp(ff2.getAnnoComp());
    if (null != ff2.getDtEmiss())
      setDtEmiss(ff2.getDtEmiss());
    if (null != ff2.getDtIniz())
      setDtIniz(ff2.getDtIniz());
    if (null != ff2.getDtFine())
      setDtFine(ff2.getDtFine());
    if (null != ff2.getTotFattura())
      setTotFattura(ff2.getTotFattura());
  }

  public Path renameFattura() {
    Path pth = null;
    if (null == tipoFatt || null == dtIniz || null == dtFine)
      return pth;
    Path pthOld = getFullPath();
    if ( !isFileExist()) {
      s_log.error("No rename!, file not exists \"{}\"", pthOld.getFileName().toString());
      return pthOld;
    }
    String szNewName = String.format("%s_%s_%s.pdf", tipoFatt.getTitolo().toString(), //
        ParseData.formatDate(dtIniz), //
        ParseData.formatDate(dtFine));
    String szOldName = pthOld.getFileName().toString();
    if (szNewName.equalsIgnoreCase(szOldName))
      return pthOld;
    pth = Paths.get(pthOld.getParent().toString(), szNewName);
    try {
      if ( !Files.exists(pthOld, LinkOption.NOFOLLOW_LINKS)) {
        s_log.error("No rename!, file not exists \"{}\"", pthOld.getFileName().toString());
        return pthOld;
      }
      Files.move(pthOld, pth, StandardCopyOption.REPLACE_EXISTING);
      s_log.info("Rinominato file fattura \"{}\" in \"{}\" ", pthOld.getFileName().toString(), pth.getFileName().toString());
      setFullPath(pth);
      setChanged(true);
    } catch (IOException e) {
      s_log.error("Fallito rename \"{}\" in \"{}\", err={} ", pthOld.getFileName().toString(), pth.getFileName().toString(),
          e.getMessage());
    }
    return pth;
  }

  @Override
  public boolean equals(Object obj) {
    if (null == obj || ! (obj instanceof FileFattura))
      return false;
    FileFattura ff2 = (FileFattura) obj;

    if (null == fullPath || null == ff2 || null == ff2.getFullPath())
      return false;
    String szF1 = getFullPath().getFileName().toString().toLowerCase();
    String szF2 = ff2.getFullPath().getFileName().toString().toLowerCase();
    return szF1.equals(szF2);
  }

  public boolean isEqualsExtended(Object obj) {
    if ( !equals(obj))
      return false;
    FileFattura ff2 = (FileFattura) obj;
    boolean bRet = idFattura == ff2.idFattura;
    if (bRet)
      bRet &= tipoFatt == ff2.tipoFatt;
    if (bRet)
      bRet &= annoComp == ff2.annoComp;
    if (bRet)
      bRet &= Utils.isValueEq(dtEmiss, ff2.dtEmiss);
    if (bRet)
      bRet &= Utils.isValueEq(dtIniz, ff2.dtIniz);
    if (bRet)
      bRet &= Utils.isValueEq(dtFine, ff2.dtFine);
    if (bRet)
      bRet &= Utils.isValueEq(totFattura, ff2.totFattura);
    return bRet;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    String sz = String.format("%04d)%s", idFatt, shortName());
    // fileExist = Files.exists(fullPath, LinkOption.NOFOLLOW_LINKS);

    sb.append(sz).append(fileExist ? " (exist!)" : " ** NOT exists! **");
    sb.append(inDb ? " (in DB!)" : " **no DB! **").append("\n");
    sb.append(String.format("\t%-16s %d\n", "idFattura", idFattura));
    sb.append(String.format("\t%-16s %s\n", "tipoFatt", null != tipoFatt ? tipoFatt.toString() : "?"));
    sb.append(String.format("\t%-16s %d\n", "AnnoComp", annoComp));
    sb.append(String.format("\t%-16s %s\n", "DtEmiss", ParseData.formatDate(dtEmiss)));
    sb.append(String.format("\t%-16s %s\n", "DtIniz", ParseData.formatDate(dtIniz)));
    sb.append(String.format("\t%-16s %s\n", "DtFine", ParseData.formatDate(dtFine)));
    sb.append(String.format("\t%-16s %s\n", "Tot.Fatt.", Utils.formatDouble(totFattura)));
    return sb.toString();
  }

  @Override
  public FileFattura call() throws Exception {
    threadName = Thread.currentThread().getName();
    szThId = String.format("%-25s, %s", threadName, getFullPath().getFileName().toString());
    TimerMeter tt = new TimerMeter(szThId);
    s_log.debug("{} - START Parsing", szThId);
    try {
      switch (tipoBatch) {
        case indovina:
          indovinaFatturaDaPDF();
          break;
        case importa:
          importaFileFattura();
          break;
        default:
          break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    s_log.debug("{} - End of Parsing", tt.stop());
    return this;
  }

  public String shortName() {
    String sz = String.format("%3d %5s %s", //
        getIdIntesta(), //
        inDb ? "inDB" : "", //
        null != fullPath ? getFullPath().toString() : "??"//
    );
    return sz;
  }

  public void random() {
    //    private RecIntesta    recint;
    //    private boolean       selected;
    //    private boolean       changed;
    //    private boolean       inDb;
    //    private boolean       fileExist;
    //    private int           idFattura;
    //    private ETipoFatt     tipoFatt;
    //    private int           annoComp;
    //    private LocalDateTime dtEmiss;
    //    private LocalDateTime dtIniz;
    //    private LocalDateTime dtFine;
    //    private Double        totFattura;
    //    private Path          fullPath;
    setSelezionato(random.nextBoolean());
    setChanged(random.nextBoolean());
    setInDb(random.nextBoolean());
    setFileExist(random.nextBoolean());
    setIdFattura(random.nextInt(100, 200));
    setTipoFatt(ETipoFatt.values()[random.nextInt(3)]);
    setDtEmiss(LocalDateTime.now().minusDays(random.nextInt(365)));
    setAnnoComp(dtEmiss.getYear());
    setDtIniz(LocalDateTime.now().minusDays(random.nextInt(365)));
    setDtFine(dtIniz.minusDays(random.nextInt(60)));
    setTotFattura(Math.round(random.nextDouble() * 10000 * 100.0) / 100.0);
    setFullPath(Paths.get(String.format("FT-%04d/%d", random.nextInt(9999) + 1, 2024)));
  }

}
