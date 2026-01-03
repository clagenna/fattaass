package sm.clagenna.fattaass.data;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.Utils;

public class RecIntesta {
  private static final Logger s_log = LogManager.getLogger(RecIntesta.class);

  private SimpleStringProperty idIntesta;
  private SimpleStringProperty nomeIntesta;
  private SimpleStringProperty dirFatture;
  @Getter @Setter
  private boolean              changed;

  public RecIntesta(int p_id, String p_no, String p_dir) {
    idIntesta = new SimpleStringProperty(String.valueOf(p_id));
    nomeIntesta = new SimpleStringProperty(p_no);
    dirFatture = new SimpleStringProperty(p_dir);
    setChanged(false);
  }

  public RecIntesta(String p_id, String p_no, String p_dir) {
    idIntesta = new SimpleStringProperty(p_id);
    nomeIntesta = new SimpleStringProperty(p_no);
    dirFatture = new SimpleStringProperty(p_dir);
    setChanged(false);
  }

  public RecIntesta(DtsRow rec) {
    Object vv = rec.get(Consts.SqlCol_idIntesta);
    idIntesta = new SimpleStringProperty(String.valueOf(vv));
    vv = rec.get(Consts.SqlCol_NomeIntesta);
    nomeIntesta = new SimpleStringProperty((String) vv);
    vv = rec.get(Consts.SqlCol_dirfatture);
    dirFatture = new SimpleStringProperty((String) vv);
    setChanged(false);
  }

  public String getIdIntesta() {
    return idIntesta.get();
  }

  public Integer getIdIntestaInt() {
    String sz = idIntesta.get();
    if (sz == null)
      return null;
    Integer ii = Integer.parseInt(sz);
    return ii;
  }

  public void setIdIntestaInt(int p_maxId) {
    String sz = String.valueOf(p_maxId);
    if (Utils.isChanged(sz, getIdIntesta()))
      setChanged(true);
    idIntesta.set(sz);
  }

  public void setIdIntesta(String v) {
    if (Utils.isChanged(v, getIdIntesta())) {
      idIntesta.set(v);
      setChanged(true);
    }
  }

  public String getNomeIntesta() {
    return nomeIntesta.get();
  }

  public void setNomeIntesta(String v) {
    if (Utils.isChanged(v, getNomeIntesta())) {
      nomeIntesta.set(v);
      setChanged(true);
    }
  }

  public String getDirFatture() {
    return dirFatture.get();
  }

  public void setDirFatture(String v) {
    if (Utils.isChanged(v, getDirFatture())) {
      dirFatture.set(v);
      setChanged(true);
    }
  }

  public boolean updateOnDB(DBConn p_co) {
    boolean bRet = false;
    return bRet;
  }

  @Override
  public String toString() {
    return getNomeIntesta();
  }

  @Override
  public boolean equals(Object obj) {
    boolean bRet = false;
    if (null == obj)
      return bRet;
    if (obj instanceof RecIntesta altro)
      bRet = altro.getNomeIntesta().equals(getNomeIntesta());
    return bRet;
  }

  public Path getFullPath(String nomeFile) {
    Path ret = null;
    if (dirFatture.isEmpty().getValue()) {
      s_log.error("Non ho il dirFatture !");
    }
    ret = Paths.get(dirFatture.get(), nomeFile);
    return ret;
  }
}
