package sm.clagenna.fattaass.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.fattaass.data.Consts;
import sm.clagenna.fattaass.data.FattAassModel;
import sm.clagenna.fattaass.data.IParserFatture;
import sm.clagenna.fattaass.data.ParserGASFattura;
import sm.clagenna.fattaass.data.rec.RecGasConsumo;
import sm.clagenna.fattaass.data.rec.RecGasFattura;
import sm.clagenna.fattaass.data.rec.RecGasLettura;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.ParseData;

public class GASxSqlServ extends BaseSqlServ implements ISql {
  private static final Logger s_log = LogManager.getLogger(GASxSqlServ.class);

  private PreparedStatement m_stmt_ins_Fattura;
  private PreparedStatement m_stmt_ins_Lettura;
  private PreparedStatement m_stmt_ins_Consumo;

  public GASxSqlServ() {
    //
  }

  public GASxSqlServ(IParserFatture p_fact, FattAassModel p_mod) {
    init(p_fact, p_mod);
  }

  @Override
  public Logger getLog() {
    return s_log;
  }

  @Override
  public void insertNewFattura() throws SQLException {
    ParserGASFattura parseGAS = (ParserGASFattura) getParsePdf();
    DBConn dbconn = getDbconn();
    Integer iidIntesta = getModel().getRecIntesta().getIdIntestaInt();
    RecGasFattura fattGAS = parseGAS.getFattura();
    try {
      if (null == m_stmt_ins_Fattura) {
        m_stmt_ins_Fattura = dbconn.getConn().prepareStatement(Consts.QRY_ins_GASFattura);
      }
    } catch (SQLException e) {
      s_log.error("Error prep stmt: %s", Consts.QRY_ins_GASFattura, e);
    }
    int k = 1;
    dbconn.setStmtInt(m_stmt_ins_Fattura, k++, iidIntesta);
    dbconn.setStmtInt(m_stmt_ins_Fattura, k++, fattGAS.getAnnoComp());
    dbconn.setStmtDatetime(m_stmt_ins_Fattura, k++, fattGAS.getDataEmiss());
    dbconn.setStmtInt(m_stmt_ins_Fattura, k++, fattGAS.getFattNrAnno());
    dbconn.setStmtString(m_stmt_ins_Fattura, k++, fattGAS.getFattNrNumero());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattGAS.getPeriodFattDtIniz());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattGAS.getPeriodFattDtFine());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattGAS.getPeriodEffDtIniz());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattGAS.getPeriodEffDtFine());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattGAS.getPeriodAccontoDtIniz());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattGAS.getPeriodAccontoDtFine());
    dbconn.setStmtImporto(m_stmt_ins_Fattura, k++, fattGAS.getRimborsoPrec());
    dbconn.setStmtImporto(m_stmt_ins_Fattura, k++, fattGAS.getMisureStraord());
    dbconn.setStmtImporto(m_stmt_ins_Fattura, k++, fattGAS.getAddizFER());
    dbconn.setStmtImporto(m_stmt_ins_Fattura, k++, fattGAS.getImpostaQuiet());
    dbconn.setStmtImporto(m_stmt_ins_Fattura, k++, fattGAS.getTotPagare());
    dbconn.setStmtString(m_stmt_ins_Fattura, k++, fileName(fattGAS.getNomeFile()));
    if (isShowStatement())
      s_log.info(toString(m_stmt_ins_Fattura));
    m_stmt_ins_Fattura.executeUpdate();
    int idFatt = dbconn.getLastIdentity();
    fattGAS.setIdGASFattura(idFatt);
    s_log.info("Inserito Fattura GAS {}", ParseData.formatDate(fattGAS.getDataEmiss()));

  }

  @Override
  public void insertNewLettura() throws SQLException {
    ParserGASFattura parseGAS = (ParserGASFattura) getParsePdf();
    List<RecGasLettura> li = parseGAS.getLiLetture();
    DBConn dbconn = getDbconn();
    RecGasFattura fattGAS = parseGAS.getFattura();
    if (null == li || li.size() == 0) {
      s_log.error("Nessuna riga di lettura GAS per Fattura del {}", ParseData.formatDate(fattGAS.getDataEmiss()));
      return;
    }
    try {
      if (null == m_stmt_ins_Lettura) {
        m_stmt_ins_Lettura = dbconn.getConn().prepareStatement(Consts.QRY_ins_GASLettura);
      }
    } catch (SQLException e) {
      s_log.error("Error prep stmt: %s", Consts.QRY_ins_GASLettura, e);
    }
    Integer iid = fattGAS.getIdGASFattura();
    int qtaLett = 0;
    for (RecGasLettura lett : li) {
      int k = 1;
      qtaLett++;
      dbconn.setStmtInt(m_stmt_ins_Lettura, k++, iid); // idGASFattura
      dbconn.setStmtInt(m_stmt_ins_Lettura, k++, lett.getLettQtaMc());
      dbconn.setStmtDatetime(m_stmt_ins_Lettura, k++, lett.getLettData());
      dbconn.setStmtString(m_stmt_ins_Lettura, k++, lett.getTipoLett().getSigla());
      dbconn.setStmtString(m_stmt_ins_Lettura, k++, lett.getMatricola());
      dbconn.setStmtDouble(m_stmt_ins_Lettura, k++, lett.getCoeffC());
      dbconn.setStmtDouble(m_stmt_ins_Lettura, k++, lett.getConsumo());
      if (isShowStatement())
        s_log.info(toString(m_stmt_ins_Lettura));
      m_stmt_ins_Lettura.executeUpdate();
    }
    s_log.info("Inserito {} righe di lettura GAS per Fattura del {}", qtaLett, ParseData.formatDate(fattGAS.getDataEmiss()));

  }

  @Override
  public void insertNewConsumo() throws SQLException {
    ParserGASFattura parseGAS = (ParserGASFattura) getParsePdf();
    List<RecGasConsumo> li = parseGAS.getLiConsumi();
    DBConn dbconn = getDbconn();
    RecGasFattura fattGAS = parseGAS.getFattura();
    if (null == li || li.size() == 0) {
      s_log.error("Nessuna riga di consumo GAS per Fattura del {}", ParseData.formatDate(fattGAS.getDataEmiss()));
      return;
    }
    try {
      if (null == m_stmt_ins_Consumo) {
        m_stmt_ins_Consumo = dbconn.getConn().prepareStatement(Consts.QRY_ins_GASConsumo);
      }
    } catch (SQLException e) {
      s_log.error("Error prep stmt: %s", Consts.QRY_ins_GASConsumo, e);
    }
    Integer iid = fattGAS.getIdGASFattura();
    int qtaCons = 0;
    for (RecGasConsumo cons : li) {
      int k = 1;
      qtaCons++;
      dbconn.setStmtInt(m_stmt_ins_Consumo, k++, iid); // idGASFattura
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
    s_log.info("Inserito {} righe di consumo GAS per Fattura del {}", qtaCons, ParseData.formatDate(fattGAS.getDataEmiss()));

  }

}
