package sm.clagenna.fattaass.sql;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.fattaass.data.Consts;
import sm.clagenna.fattaass.data.FattAassModel;
import sm.clagenna.fattaass.data.IParserFatture;
import sm.clagenna.fattaass.data.ParserEEFattura;
import sm.clagenna.fattaass.data.rec.RecEEConsumi;
import sm.clagenna.fattaass.data.rec.RecEEFattura;
import sm.clagenna.fattaass.data.rec.RecEELettura;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.ParseData;

public class EExSQLite extends BaseSQLite implements ISql {
  private static final Logger s_log = LogManager.getLogger(EExSQLite.class);

  private PreparedStatement m_stmt_ins_Fattura;
  private PreparedStatement m_stmt_ins_Lettura;
  private PreparedStatement m_stmt_ins_Consumo;

  public EExSQLite() {
    //
  }

  public EExSQLite(IParserFatture p_fact, FattAassModel p_mod) {
    init(p_fact, p_mod);
  }

  @Override
  public Logger getLog() {
    return s_log;
  }

  @Override
  public void insertNewFattura() throws SQLException {
    ParserEEFattura parseEE = (ParserEEFattura) getParsePdf();
    DBConn dbconn = getDbconn();
    Integer iidIntesta = getModel().getRecIntesta().getIdIntestaInt();
    RecEEFattura fattEE = parseEE.getFattura();
    try {
      if (null == m_stmt_ins_Fattura) {
        m_stmt_ins_Fattura = dbconn.getConn().prepareStatement(Consts.QRY_ins_EEFattura);
      }
    } catch (SQLException e) {
      s_log.error("Error prep stmt: %s", Consts.QRY_ins_EEFattura, e);
    }
    int k = 1;
    dbconn.setStmtInt(m_stmt_ins_Fattura, k++, iidIntesta);
    dbconn.setStmtInt(m_stmt_ins_Fattura, k++, fattEE.getAnnoComp());
    dbconn.setStmtDatetime(m_stmt_ins_Fattura, k++, fattEE.getDataEmiss());
    dbconn.setStmtInt(m_stmt_ins_Fattura, k++, fattEE.getFattNrAnno());
    dbconn.setStmtString(m_stmt_ins_Fattura, k++, fattEE.getFattNrNumero());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattEE.getPeriodFattDtIniz());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattEE.getPeriodFattDtFine());
    dbconn.setStmtInt(m_stmt_ins_Fattura, k++, fattEE.getCredPrecKwh());
    dbconn.setStmtInt(m_stmt_ins_Fattura, k++, fattEE.getCredAttKwh());
    dbconn.setStmtImporto(m_stmt_ins_Fattura, k++, fattEE.getAddizFER());
    dbconn.setStmtImporto(m_stmt_ins_Fattura, k++, fattEE.getImpostaQuiet());
    dbconn.setStmtImporto(m_stmt_ins_Fattura, k++, fattEE.getTotPagare());
    dbconn.setStmtString(m_stmt_ins_Fattura, k++, fattEE.getNomeFile());
    if (isShowStatement())
      s_log.info(toString(m_stmt_ins_Fattura));
    m_stmt_ins_Fattura.executeUpdate();
    int idFatt = dbconn.getLastIdentity();
    fattEE.setIdEEFattura(idFatt);
    s_log.info("Inserito Fattura EE {}", ParseData.formatDate(fattEE.getDataEmiss()));
  }

//  @Override
//  public boolean letturaExist() throws SQLException {
//    return false;
//  }

  @Override
  public void insertNewLettura() throws SQLException {
    ParserEEFattura parseEE = (ParserEEFattura) getParsePdf();
    List<RecEELettura> li = parseEE.getLiLetture();
    DBConn dbconn = getDbconn();
    RecEEFattura fattEE = parseEE.getFattura();
    if (null == li || li.size() == 0) {
      s_log.error("Nessuna riga di lettura EE per Fattura del {}", ParseData.formatDate(fattEE.getDataEmiss()));
      return;
    }
    try {
      if (null == m_stmt_ins_Lettura) {
        m_stmt_ins_Lettura = dbconn.getConn().prepareStatement(Consts.QRY_ins_EELettura);
      }
    } catch (SQLException e) {
      s_log.error("Error prep stmt: %s", Consts.QRY_ins_EELettura, e);
    }
    Integer iid = fattEE.getIdEEFattura();
    int qtaLett = 0;
    for (RecEELettura lett : li) {
      int k = 1;
      qtaLett++;
      dbconn.setStmtInt(m_stmt_ins_Lettura, k++, iid);
      dbconn.setStmtDatetime(m_stmt_ins_Lettura, k++, lett.getDtLettPrec());
      dbconn.setStmtInt(m_stmt_ins_Lettura, k++, lett.getLettPrec());
      dbconn.setStmtString(m_stmt_ins_Lettura, k++, lett.getTipoLettura().toString());
      dbconn.setStmtDatetime(m_stmt_ins_Lettura, k++, lett.getDtLettAttuale());
      dbconn.setStmtInt(m_stmt_ins_Lettura, k++, lett.getLettAttuale());
      dbconn.setStmtDouble(m_stmt_ins_Lettura, k++, lett.getConsumo());
      if (isShowStatement())
        s_log.info(toString(m_stmt_ins_Lettura));
      m_stmt_ins_Lettura.executeUpdate();
    }
    s_log.info("Inserito {} righe di lettura EE per Fattura del {}", qtaLett, ParseData.formatDate(fattEE.getDataEmiss()));
  }

//  @Override
//  public boolean consumoExist() throws SQLException {
//    // 
//    return false;
//  }

  @Override
  public void insertNewConsumo() throws SQLException {
    ParserEEFattura parseEE = (ParserEEFattura) getParsePdf();
    List<RecEEConsumi> li = parseEE.getLiConsumi();
    DBConn dbconn = getDbconn();
    RecEEFattura fattEE = parseEE.getFattura();
    if (null == li || li.size() == 0) {
      s_log.error("Nessuna riga di consumo EE per Fattura del {}", ParseData.formatDate(fattEE.getDataEmiss()));
      return;
    }
    try {
      if (null == m_stmt_ins_Consumo) {
        m_stmt_ins_Consumo = dbconn.getConn().prepareStatement(Consts.QRY_ins_EEConsumo);
      }
    } catch (SQLException e) {
      s_log.error("Error prep stmt: %s", Consts.QRY_ins_EEConsumo, e);
    }
    Integer iid = fattEE.getIdEEFattura();
    int qtaCons = 0;
    for (RecEEConsumi cons : li) {
      int k = 1;
      qtaCons++;
      dbconn.setStmtInt(m_stmt_ins_Consumo, k++, iid);
      dbconn.setStmtString(m_stmt_ins_Consumo, k++, cons.getTipoSpesa().getSigla());
      dbconn.setStmtDatetime(m_stmt_ins_Consumo, k++, cons.getDtIniz());
      dbconn.setStmtDatetime(m_stmt_ins_Consumo, k++, cons.getDtFine());
      dbconn.setStmtInt(m_stmt_ins_Consumo, k++, cons.isStimato() ? 1 : 0);
      dbconn.setStmtDouble(m_stmt_ins_Consumo, k++, cons.getPrezzoUnit());
      dbconn.setStmtDouble(m_stmt_ins_Consumo, k++, cons.getQuantita());
      dbconn.setStmtImporto(m_stmt_ins_Consumo, k++, cons.getImporto());
      if (isShowStatement())
        s_log.info(toString(m_stmt_ins_Consumo));
      m_stmt_ins_Consumo.executeUpdate();
    }
    s_log.info("Inserito {} righe di consumo EE per Fattura del {}", qtaCons, ParseData.formatDate(fattEE.getDataEmiss()));
  }

  @Override
  public void close() throws IOException {
    super.close();
  }

}
