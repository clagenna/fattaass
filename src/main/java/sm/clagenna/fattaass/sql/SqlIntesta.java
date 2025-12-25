package sm.clagenna.fattaass.sql;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.fattaass.data.Consts;
import sm.clagenna.fattaass.data.FattAassModel;
import sm.clagenna.fattaass.data.RecIntesta;
import sm.clagenna.stdcla.sql.DBConn;

public class SqlIntesta {

  private static final Logger s_log = LogManager.getLogger(SqlIntesta.class);

  private DBConn        connSql;
  private FattAassModel model;

  public SqlIntesta(FattAassModel p_mod) {
    model = p_mod;
    connSql = model.getDbconn();
    readAllIntesta();
  }

  private void readAllIntesta() {
    Map<String, RecIntesta> mpi;
    mpi = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    Connection conn = connSql.getConn();
    try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(Consts.QRY_sel_intesta)) {
      while (rs.next()) {
        int id = rs.getInt(1);
        String no = rs.getString(2);
        Path pth = Paths.get(rs.getString(3));
        RecIntesta rec = new RecIntesta(id, no, pth.toString());
        mpi.put(no, rec);
      }
      model.setMapIntesta(mpi);
    } catch (SQLException e) {
      s_log.error("Query dbo.intesta; err={}", e.getMessage(), e);
    }
  }

  public int addNewRec(RecIntesta p_newRec) {
    Map<String, RecIntesta> mpi = model.getMapIntesta();
    if (null == mpi || mpi.size() == 0) {
      mpi = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      model.setMapIntesta(mpi);
    }
    int nRet = 0;
    OptionalInt omaxid = mpi.values() //
        .stream() //
        .mapToInt(r -> r.getIdIntestaInt()) //
        .max();
    int maxId = 1;
    if ( !omaxid.isEmpty())
      maxId = omaxid.getAsInt() + 1;
    p_newRec.setIdIntestaInt(maxId);
    mpi.put(p_newRec.getIdIntesta(), p_newRec);
    Connection conn = connSql.getConn();
    int k = 1;
    try (PreparedStatement stmt = conn.prepareStatement(Consts.QRY_ins_intesta)) {
      connSql.setStmtInt(stmt, k++, p_newRec.getIdIntestaInt());
      connSql.setStmtString(stmt, k++, p_newRec.getNomeIntesta());
      connSql.setStmtString(stmt, k++, p_newRec.getDirFatture());
      nRet = stmt.executeUpdate();
      s_log.debug("ret code={} for Insert Intesta", k);
    } catch (SQLException e) {
      s_log.error("Errore insert di {}, err={}", p_newRec.getNomeIntesta(), e.getMessage(), e);
    }
    return nRet;
  }

  public int saveUpdatesRecIntesta() {
    int nRet = 0;
    Connection conn = connSql.getConn();
    Collection<RecIntesta> li = model.getMapIntesta().values();
    for (RecIntesta rec : li) {
      if ( !rec.isChanged())
        continue;
      int k = 1;
      try (PreparedStatement stmt = conn.prepareStatement(Consts.QRY_upd_intesta)) {
        connSql.setStmtString(stmt, k++, rec.getNomeIntesta());
        connSql.setStmtString(stmt, k++, rec.getDirFatture());
        connSql.setStmtInt(stmt, k++, rec.getIdIntestaInt());
        int ret = stmt.executeUpdate();
        nRet += ret;
        s_log.debug("ret code={} for updates", ret);
        if (ret == 1)
          rec.setChanged(false);
      } catch (SQLException e) {
        s_log.error("Errore update di {}, err={}", rec.getNomeIntesta(), e.getMessage(), e);
      }
    }
    return nRet;
  }

}
