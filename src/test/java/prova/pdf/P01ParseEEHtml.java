package prova.pdf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import sm.clagenna.fattaass.data.Consts;
import sm.clagenna.fattaass.data.FattAassModel;
import sm.clagenna.fattaass.data.ParserEEFattura;
import sm.clagenna.fattaass.sql.EExSqlServ;
import sm.clagenna.fattaass.sql.ISql;
import sm.clagenna.stdcla.pdf.FromPdf2Html;
import sm.clagenna.stdcla.sql.EServerId;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.sys.ex.AppPropsException;

public class P01ParseEEHtml {

  private Path            pthPdf;
  private FromPdf2Html    pdf2html;
  private ParserEEFattura parser;
  private AppProperties   props;
  private FattAassModel   model;
  private EServerId       serverId;

  private boolean bDebug    = true;
  private boolean bSaveHTML = true;
  private boolean bSaveCSV  = true;
  private boolean bSaveTXT  = true;

  @Test
  public void doIt() throws AppPropsException, SQLException {
    redirectJUL2Log4j();
    openProperties();
    init();
    pthPdf = Paths.get("data/202510_0000128757.pdf");
    pthPdf = Paths.get("F:/varie/AASS/Alessandro/2022-04-20 Luce Condominio.pdf");
    scanFile(pthPdf);
  }

  // @ Test
  public void doIt2() throws AppPropsException, SQLException, IOException {
    redirectJUL2Log4j();
    openProperties();
    init();
    Path pthStrtDir = Paths.get("F:\\varie\\AASS\\Alessandro");
    List<Path> li = Files.list(pthStrtDir) //
        .filter(s -> s.getFileName().toString().endsWith(".pdf")) //
        .collect(Collectors.toList());
    for (Path pth : li) {
      try {
        scanFile(pth);
      } catch (UnsupportedOperationException | AppPropsException | SQLException e) {
        System.err.print("Il file errato:" + pth.toString());
      }
    }
    scanFile(pthPdf);
  }

  private void scanFile(Path pthPdf) throws AppPropsException, SQLException {

    pdf2html = new FromPdf2Html();

    // pdf2html.setParserHtml(parser);
    pdf2html.setDebug(bDebug);
    pdf2html.setSaveHTML(bSaveHTML);
    pdf2html.setSaveCSV(bSaveCSV);
    pdf2html.setSaveTXT(bSaveTXT);
    pdf2html.parsePDF(pthPdf);

    parser = new ParserEEFattura(model);
    parser.setEvidenziaTokens(bSaveHTML);
    parser.setDebug(bDebug);
    int qtaToks = parser.parse(pdf2html);
    if (qtaToks <= 0) {
      return;
    }
    pdf2html.setSaveHTML(true);
    pdf2html.saveHtml("_3");

    printParsedValues();
    ISql sql = model.getFatturaInserter(parser.getTipoFattura(), serverId, model.isSingleThread());
    sql.init(parser, model);
    ((EExSqlServ) sql).setShowStatement(true);
    //    sql.setParsePdf(parser);
    //    sql.setModel(model);
    if (sql.fatturaExist()) {
      sql.deleteFattura();
    }
    sql.insertNewFattura();
    sql.insertNewLettura();
    sql.insertNewConsumo();

  }

  private void printParsedValues() {
    System.out.println("\n------------- Tagged Values ------------------");
    parser.printTGV();
    System.out.println("\n------------- FATTURA ------------------");
    System.out.println(parser.getFattura().toString());
    System.out.println("\n------------- Letture ------------------");
    parser.getLiLetture().stream().forEach(System.out::println);
    System.out.println("\n------------- Consumi ------------------");
    parser.getLiConsumi().stream().forEach(System.out::println);
  }

  private void openProperties() throws AppPropsException {
    AppProperties.setSingleton(false);
    props = new AppProperties();
    props.leggiPropertyFile(new File(Consts.CSZ_MAIN_PROPS), false, false);
    serverId = EServerId.parse(props.getProperty(AppProperties.CSZ_PROP_DB_Type));
  }

  private void init() throws AppPropsException {
    model = new FattAassModel();
    model.initApp(props);
  }

  //  private void initDB() {
  //    // DBConnFactory.setSingleton(false);
  //    String szDbType = props.getProperty(AppProperties.CSZ_PROP_DB_Type);
  //    try {
  //      // connSQL = new DBConnSQL();
  //      DBConnFactory conFact = new DBConnFactory();
  //      connSQL = conFact.get(szDbType);
  //      connSQL.readProperties(props);
  //
  //      TimerMeter tm1 = new TimerMeter("Open DB");
  //      connSQL.doConn();
  //      System.out.printf("ProvaParseHTML.openDb(time=%s)\n", tm1.stop());
  //
  //    } catch (Exception e) {
  //      System.out.printf("Errore apertura DB, error=%s\n", e.getMessage());
  //    }
  //  }

  private void redirectJUL2Log4j() {
    // Remove existing handlers attached to JUL root logger
    //    java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
    //    java.util.logging.Handler[] handlers = rootLogger.getHandlers();
    //    for (java.util.logging.Handler handler : handlers) {
    //      rootLogger.removeHandler(handler);
    //    }
    // Install Log4j2 bridge
    // org.apache.logging.log4j.jul.Manager.getLogManager().reset();
    //    LogManager.getLogManager().reset();
    //
    //    Logger log = org.slf4j.LoggerFactory.getLogger(TtfInstructionParser.class);
    //    String szLog = "level INFO prima";
    //    System.out.println(szLog);
    //    log.info(szLog);
    System.setProperty("org.slf4j.simpleLogger.log.org.mabb.fontverter.opentype.TtfInstructions", "warn");
    //    String szTTf = TtfInstructionParser.class.getName();
    //    int n = szTTf.lastIndexOf(".");
    //    szTTf = szTTf.substring(n);
    //    System.setProperty(szLog, "warn");
    //    szLog = "level INFO Dopo set ";
    //    System.out.println(szLog);
    //    log.info(szLog);

    //    org.apache.logging.log4j.core.config.Configurator.setLevel(TtfInstructionParser.class.getName(), org.apache.logging.log4j.Level.OFF);
    //    log.info("Seconde level INFO");

    //    Map<String, Level> map = new HashMap<>();
    //    String sz = TtfInstructionParser.class.getName();
    //    System.out.println("Class name:" + sz);
    //    map.put(sz, Level.ERROR);
    //    org.apache.logging.log4j.core.config.Configurator.setLevel(map);

    //    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    //    Configuration config = ctx.getConfiguration();
    //    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
    //    loggerConfig.setLevel(level);
    //    ctx.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.

    //    final ch.qos.logback.classic.Logger logger2 = (ch.qos.logback.classic.Logger) logger;
    //
    //    ch.qos.logback.classic.Logger logbackLogger =
    //        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(loggerName);
    //    org.slf4j.
    // log, org.apache.logging.log4j.Level.OFF);
    //    log.info("Lancio un log per TtfInstructionParser");
  }
}
