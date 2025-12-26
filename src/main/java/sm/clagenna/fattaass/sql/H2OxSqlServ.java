package sm.clagenna.fattaass.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.fattaass.data.Consts;
import sm.clagenna.fattaass.data.FattAassModel;
import sm.clagenna.fattaass.data.IParserFatture;
import sm.clagenna.fattaass.data.ParserH2OFattura;
import sm.clagenna.fattaass.data.rec.RecH2OFattura;
import sm.clagenna.fattaass.data.rec.RecH2OLettura;
import sm.clagenna.fattaass.data.rec.RecH2Oconsumo;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.utils.ParseData;

public class H2OxSqlServ extends BaseSqlServ implements ISql {
  private static final Logger s_log = LogManager.getLogger(H2OxSqlServ.class);

  private PreparedStatement m_stmt_ins_Fattura;
  private PreparedStatement m_stmt_ins_Lettura;
  private PreparedStatement m_stmt_ins_Consumo;

  private RecH2OFattura fattH2O;

  public H2OxSqlServ() {
    //
  }

  public H2OxSqlServ(IParserFatture p_fact, FattAassModel p_mod) {
    init(p_fact, p_mod);
  }

  @Override
  public Logger getLog() {
    return s_log;
  }

  @Override
  public void insertNewFattura() throws SQLException {
    ParserH2OFattura parseH2O = (ParserH2OFattura) getParsePdf();
    DBConn dbconn = getDbconn();
    Integer iidIntesta = getModel().getRecIntesta().getIdIntestaInt();
    fattH2O = parseH2O.getFattura();
    try {
      if (null == m_stmt_ins_Fattura) {
        m_stmt_ins_Fattura = dbconn.getConn().prepareStatement(Consts.QRY_ins_H2OFattura);
      }
    } catch (SQLException e) {
      s_log.error("Error prep stmt: %s", Consts.QRY_ins_H2OFattura, e);
    }
    int k = 1;
    dbconn.setStmtInt(m_stmt_ins_Fattura, k++, iidIntesta);
    dbconn.setStmtInt(m_stmt_ins_Fattura, k++, fattH2O.getAnnoComp());
    dbconn.setStmtDatetime(m_stmt_ins_Fattura, k++, fattH2O.getDataEmiss());
    dbconn.setStmtInt(m_stmt_ins_Fattura, k++, fattH2O.getFattNrAnno());
    dbconn.setStmtString(m_stmt_ins_Fattura, k++, fattH2O.getFattNrNumero());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattH2O.getPeriodFattDtIniz());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattH2O.getPeriodFattDtFine());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattH2O.getPeriodEffDtIniz());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattH2O.getPeriodEffDtFine());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattH2O.getPeriodStimDtIniz());
    dbconn.setStmtDate(m_stmt_ins_Fattura, k++, fattH2O.getPeriodStimDtFine());
    dbconn.setStmtImporto(m_stmt_ins_Fattura, k++, fattH2O.getAssicurazione());
    dbconn.setStmtImporto(m_stmt_ins_Fattura, k++, fattH2O.getImpostaQuiet());
    dbconn.setStmtImporto(m_stmt_ins_Fattura, k++, fattH2O.getRestituzAccPrec());
    dbconn.setStmtImporto(m_stmt_ins_Fattura, k++, fattH2O.getTotPagare());
    dbconn.setStmtString(m_stmt_ins_Fattura, k++, fileName(fattH2O.getNomeFile()));
    if (isShowStatement())
      s_log.info(toString(m_stmt_ins_Fattura));
    m_stmt_ins_Fattura.executeUpdate();
    int idFatt = dbconn.getLastIdentity();
    fattH2O.setIdH2OFattura(idFatt);
    s_log.info("Inserito Fattura H2O {}", ParseData.formatDate(fattH2O.getDataEmiss()));
  }

  @Override
  public void insertNewLettura() throws SQLException {
    ParserH2OFattura parseH2O = (ParserH2OFattura) getParsePdf();
    List<RecH2OLettura> li = parseH2O.getLiLetture();
    DBConn dbconn = getDbconn();

    if (null == li || li.size() == 0) {
      s_log.error("Nessuna riga di lettura H2O per Fattura del {}", ParseData.formatDate(fattH2O.getDataEmiss()));
      return;
    }
    try {
      if (null == m_stmt_ins_Lettura) {
        m_stmt_ins_Lettura = dbconn.getConn().prepareStatement(Consts.QRY_ins_H2OLettura);
      }
    } catch (SQLException e) {
      s_log.error("Error prep stmt: %s", Consts.QRY_ins_H2OLettura, e);
    }
    Integer iid = fattH2O.getIdH2OFattura();
    int qtaLett = 0;
    for (RecH2OLettura lett : li) {
      int k = 1;
      qtaLett++;
      dbconn.setStmtInt(m_stmt_ins_Lettura, k++, iid); // idH2OFattura
      dbconn.setStmtInt(m_stmt_ins_Lettura, k++, lett.getLettQtaMc());
      dbconn.setStmtDatetime(m_stmt_ins_Lettura, k++, lett.getLettData());
      dbconn.setStmtString(m_stmt_ins_Lettura, k++, lett.getTipoLett().getSigla());
      dbconn.setStmtString(m_stmt_ins_Lettura, k++, lett.getMatricola());
      dbconn.setStmtDouble(m_stmt_ins_Lettura, k++, lett.getCoeffK());
      dbconn.setStmtDouble(m_stmt_ins_Lettura, k++, lett.getConsumo());
      if (isShowStatement())
        s_log.info(toString(m_stmt_ins_Lettura));
      m_stmt_ins_Lettura.executeUpdate();
    }
    s_log.info("Inserito {} righe di lettura H2O per Fattura del {}", qtaLett, ParseData.formatDate(fattH2O.getDataEmiss()));

  }

  @Override
  public void insertNewConsumo() throws SQLException {
    ParserH2OFattura parseH2O = (ParserH2OFattura) getParsePdf();
    List<RecH2Oconsumo> li = parseH2O.getLiConsumi();
    DBConn dbconn = getDbconn();

    if (null == li || li.size() == 0) {
      s_log.error("Nessuna riga di consumo H2O per Fattura del {}", ParseData.formatDate(fattH2O.getDataEmiss()));
      return;
    }
    try {
      if (null == m_stmt_ins_Consumo) {
        m_stmt_ins_Consumo = dbconn.getConn().prepareStatement(Consts.QRY_ins_H2OConsumo);
      }
    } catch (SQLException e) {
      s_log.error("Error prep stmt: %s", Consts.QRY_ins_H2OConsumo, e);
    }
    Integer iid = fattH2O.getIdH2OFattura();
    int qtaCons = 0;
    for (RecH2Oconsumo cons : li) {
      int k = 1;
      qtaCons++;
      dbconn.setStmtInt(m_stmt_ins_Consumo, k++, iid); // idH2OFattura
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
    s_log.info("Inserito {} righe di consumo H2O per Fattura del {}", qtaCons, ParseData.formatDate(fattH2O.getDataEmiss()));

  }

}
