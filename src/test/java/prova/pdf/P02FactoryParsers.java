package prova.pdf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import sm.clagenna.fattaass.data.Consts;
import sm.clagenna.fattaass.data.FattAassModel;
import sm.clagenna.fattaass.data.ParserFatturaSoloTipo;
import sm.clagenna.stdcla.pdf.FromPdf2Html;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.sys.ex.AppPropsException;

public class P02FactoryParsers {

  private AppProperties props;
  private FattAassModel model;
  private FromPdf2Html  pdf2html;

  public P02FactoryParsers() {
    //
  }

  @Test
  public void doit() throws AppPropsException, IOException {
    redirectJUL2Log4j();
    openProperties();
    init();
    pdf2html = new FromPdf2Html();

    pdf2html.setDebug(false);
    pdf2html.setSaveHTML(true);
    pdf2html.setSaveCSV(false);
    pdf2html.setSaveTXT(false);
    
    Path srcPth = Paths.get("F:\\java\\conmod\\fattaass\\data");
    List<Path> li;
    System.out.printf("Scan dir: %s\n", srcPth.toString());
    li = Files.list(srcPth) //
        .filter(f -> !Files.isDirectory(f)) //
        .filter(f -> f.toString().toLowerCase().endsWith(".pdf")) //
        .collect(Collectors.toList());

    for (Path pth : li) {
      pdf2html.parsePDF(pth);
      ParserFatturaSoloTipo parseTipoFatt = new ParserFatturaSoloTipo(model);
      parseTipoFatt.setEvidenziaTokens(true);
      parseTipoFatt.parse(pdf2html);
      pdf2html.setSaveHTML(true);
      pdf2html.saveHtml("_3");

      System.out.println("File:" + pth.toString());
      System.out.printf("Il file %s \t tipo %s\n", pth.toString(), parseTipoFatt.getTipoFattura().toString());
    }

  }

  private void openProperties() throws AppPropsException {
    AppProperties.setSingleton(false);
    props = new AppProperties();
    props.leggiPropertyFile(new File(Consts.CSZ_MAIN_PROPS), false, false);
  }

  private void init() throws AppPropsException {
    model = new FattAassModel();
    model.initApp(props);
  }

  private void redirectJUL2Log4j() {
    @SuppressWarnings("unused") final String ORG_LOG = "org.mabb.fontverter.opentype.TtfInstructions.TtfInstructionParser";

    // !!!!!!!!!!!!!!!!   QUESTA FUNZIONA  !!!!!!!!!!!!!!
    // System.setProperty("org.slf4j.simpleLogger.log.org.mabb.fontverter.opentype.TtfInstructions.TtfInstructionParser", "warn");
    System.setProperty("org.slf4j.simpleLogger.log.org.mabb.fontverter.opentype.TtfInstructions", "warn");
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    //    Logger javUtLogr = java.util.logging.Logger.getLogger(ORG_LOG);
    //    javUtLogr.setLevel(java.util.logging.Level.OFF);
    //    // ----- !!!  non funziona !!!! ---------------------
    //    org.apache.log4j.Logger jog4Logr = org.apache.log4j.Logger.getLogger(ORG_LOG);
    //    jog4Logr.setLevel(org.apache.log4j.Level.OFF);
    // ----- !!!  non funziona !!!! ---------------------

    org.slf4j.Logger slf4jLogr = org.slf4j.LoggerFactory
        .getLogger(org.mabb.fontverter.opentype.TtfInstructions.TtfInstructionParser.class);
    slf4jLogr.info("Claudio");
    System.out.println("Prova TtfInstructionParser");
    //    SimpleLoggerConfiguration slf4jConf = new org.slf4j.simple.SimpleLoggerConfiguration();

    //    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TtfInstructionParser.class);
    //    String szLog = "level INFO prima";
    //    System.out.println(szLog);
    //    log.info(szLog);
    //    String szTTf = TtfInstructionParser.class.getName();
    //    int n = szTTf.lastIndexOf(".");
    //    szTTf = szTTf.substring(0, n);
    //    System.setProperty(szTTf, "warn");
    //    szLog = "level INFO Dopo set " + szTTf;
    //    System.out.println(szLog);
    //    log.info(szLog);

    // Remove existing handlers attached to JUL root logger
    // prima
    //    java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
    //    java.util.logging.Handler[] handlers = rootLogger.getHandlers();
    //    for (java.util.logging.Handler handler : handlers) {
    //      rootLogger.removeHandler(handler);
    //    }

    // dopo ...
    //    Logger var = org.slf4j.LoggerFactory.getLogger(ORG_LOG);
    //    ch.qos.logback.classic.Logger logr =  (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ORG_LOG);
    //    logr.setLevel(ch.qos.logback.classic.Level.WARN);

    // Install Log4j2 bridge
    // org.apache.logging.log4j.jul.Manager.getLogManager().reset();
    //    LogManager.getLogManager().reset();

    //    Logger log = org.slf4j.LoggerFactory.getLogger(TtfInstructionParser.class);
    //    log.info("level INFO");
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
