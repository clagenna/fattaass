package sm.clagenna.fattaass.sql;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.fattaass.data.Consts;
import sm.clagenna.fattaass.data.FattAassModel;
import sm.clagenna.fattaass.data.IParserFatture;
import sm.clagenna.fattaass.data.IRecFattura;
import sm.clagenna.fattaass.data.RecIntesta;
import sm.clagenna.fattaass.enums.ETipoFatt;
import sm.clagenna.fattaass.sys.ex.ReadFattException;
import sm.clagenna.stdcla.sql.DBConn;

public abstract class BaseSqlServ implements ISql, Closeable {

  @Getter @Setter
  private FattAassModel     model;
  @Getter @Setter
  private IParserFatture    parsePdf;
  @Getter @Setter
  private IRecFattura       fattura;
  @Getter @Setter
  private DBConn            dbconn;
  @Getter @Setter
  private boolean           showStatement;
  @Getter
  private List<Integer>     listIdFattura;
  Optional<Integer>         idFattura;
  Optional<LocalDateTime>   dtFattura;
  private PreparedStatement m_stmt_cerca_fattura;
  @Getter @Setter
  private ETipoFatt         tipoFattura;
  @Getter @Setter
  private RecIntesta        recIntesta;
  @Getter @Setter
  private String            threadName;

  public BaseSqlServ() {
    threadName = "";
  }

  public BaseSqlServ(IParserFatture p_fact, FattAassModel p_mod) {
    threadName = "";
    init(p_fact, p_mod);
  }

  @Override
  public void init(IParserFatture p_fact, FattAassModel p_mod) {
    model = p_mod;
    if ( !model.isSingleThread()) {
      dbconn = model.newDBConn();
    } else
      dbconn = model.getDbconn();
    setParsePdf(p_fact);
    fattura = p_fact.getFattura();
    setTipoFattura(fattura.getTipoFattura());
    Connection conn = dbconn.getConn();
    String szT = fattura.getTipoFattura().getTitolo();
    String szQry = String.format(Consts.QRY_find_Fattura, szT, szT, szT);
    try {
      m_stmt_cerca_fattura = conn.prepareStatement(szQry);
    } catch (SQLException e) {
      getLog().error("{} Error prep stmt: %s", threadName, szQry, e);
    }
  }

  public Integer getIdFattura() {
    if (null == listIdFattura || listIdFattura.size() != 1)
      return null;
    return listIdFattura.get(0);
  }

  @Override
  public abstract Logger getLog();

  protected boolean existFattDaCancellare() {
    if (null == listIdFattura || listIdFattura.size() == 0)
      return false;
    return true;
  }

  @Override
  public boolean fatturaExist() throws SQLException {
    LocalDateTime dtEmiss = null;
    try {
      dtEmiss = parsePdf.getDate(Consts.TGV_DataEmiss);
    } catch (ReadFattException e) {
      getLog().error("{}, Su fattura Exist Non trovo {}", threadName, Consts.TGV_DataEmiss);
      throw new SQLException("No " + Consts.TGV_DataEmiss);
    }
    RecIntesta reci = model.getRecIntesta();
    int k = 1;
    dbconn.setStmtInt(m_stmt_cerca_fattura, k++, reci.getIdIntestaInt()); // idIntesta
    // questi sono in OR per cui dovrei beccarla con idFattura oppure dataEmiss
    dbconn.setStmtInt(m_stmt_cerca_fattura, k++, fattura.getIdFattura()); // idFattura
    dbconn.setStmtDate(m_stmt_cerca_fattura, k++, dtEmiss); // datEmiss
    // ----------------------------------------------------
    clearIdFattura();
    try (ResultSet res = m_stmt_cerca_fattura.executeQuery()) {
      while (res.next()) {
        int iiId = res.getInt(1);
        fattura.setIdFattura(iiId);
        addIdFattura(iiId);
      }
      if (isShowStatement())
        getLog().debug(toString(m_stmt_cerca_fattura));
    }
    return existFattDaCancellare();
  }

  //  @Override
  //  public boolean letturaExist() throws SQLException {
  //    //
  //    return false;
  //  }

  //  @Override
  //  public boolean consumoExist() throws SQLException {
  //    //
  //    return false;
  //  }

  /**
   * Cancella le fatture che rispondono agli id presenti nel elenco
   * {@link #listIdFattura} previamente preparato da {@link #fatturaExist()}.
   * Cancello previamente i record da "XXConsumo", "XXLettura" e finalmente
   * "XXFattura"
   */
  @Override
  public void deleteFattura() throws SQLException {
    String tpf = fattura.getTipoFattura().getTitolo();
    if (null == listIdFattura || listIdFattura.size() == 0) {
      getLog().warn("{},Nessuna fattura {} da cancellare", threadName, tpf);
      return;
    }

    String qry = null;
    // cancello i record da "XXConsumo", "XXLettura" e finalmente "XXFattura"
    for (Integer iidFatt : listIdFattura) {
      for (String tab : Consts.CSZ_arrtabs) {
        String nomeTab = tpf + tab;
        qry = String.format(Consts.QRY_del_Fattura, nomeTab, tpf);
        int k = 1;
        try (PreparedStatement stmt = dbconn.getConn().prepareStatement(qry)) {
          dbconn.setStmtInt(stmt, k++, iidFatt);
          if (isShowStatement())
            getLog().info("{},{}", threadName, toString(stmt));
          int qtaDel = stmt.executeUpdate();
          getLog().debug("{}, Cancellato {} righe da {}", threadName, qtaDel, nomeTab);
        } catch (SQLException e) {
          getLog().error("{}, Errore per stmt {}", threadName, qry, e);
        }
      }
    }
  }

  //  @Override
  //  public void insertNewFattura() throws SQLException {
  //    //
  //
  //  }

  //  @Override
  //  public void insertNewLettura() throws SQLException {
  //    //
  //
  //  }

  //  @Override
  //  public void insertNewConsumo() throws SQLException {
  //    //
  //
  //  }

  @Override
  public void close() throws IOException {
    if (null != dbconn)
      dbconn.close();
    dbconn = null;
  }

  public void clearIdFattura() {
    //    if (null != listIdFattura)
    //      listIdFattura.clear();
    listIdFattura = null;
  }

  public void addIdFattura(Integer p_id) {
    if (listIdFattura == null)
      listIdFattura = new ArrayList<>();
    if ( !listIdFattura.contains(p_id))
      listIdFattura.add(p_id);
  }

  public String fileName(String szFil) {
    String szPadre = getModel().getRecIntesta().getDirFatture();
    String szFileNam = szFil.toLowerCase();
    if (szFileNam.startsWith(szPadre.toLowerCase()))
      szFileNam = szFil.substring(szPadre.length());
    if ( !Character.isLetter(szFileNam.charAt(0)))
      szFileNam = szFileNam.substring(1);
    return szFileNam;
  }

  /**
   * Comunque questa andrebbe sostituita con la libreria di
   * <a target="_blank" href="https://github.com/p6spy/p6spy">vedi P6Spy</a>
   * 
   * @param stmt
   * @return
   */
  public String toString(PreparedStatement stmt) {
    String qry = stmt.toString();
    String[] arr = qry.split("parameters=");
    String[] pars = null;
    qry = arr[0];
    if (arr.length > 1) {
      pars = arr[1].replace("[", "").replace("]", "").split(",");
    }
    if (null != pars) {
      for (String par : pars) {
        int n = qry.indexOf("?");
        if (n < 0)
          break;
        String sz1 = qry.substring(0, n);
        String sz2 = qry.substring(n + 2);
        qry = String.format("%s%s %s", sz1, par, sz2);
      }
    }
    return qry;
  }

}
