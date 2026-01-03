package sm.clagenna.fattaass.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.fattaass.data.FileFattura.ETipoBatch;
import sm.clagenna.fattaass.enums.ETipoFatt;
import sm.clagenna.fattaass.sql.FactorySql;
import sm.clagenna.fattaass.sql.ISql;
import sm.clagenna.fattaass.sql.SqlIntesta;
import sm.clagenna.fattaass.sys.ex.ReadFattException;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.DBConnFactory;
import sm.clagenna.stdcla.sql.EServerId;
import sm.clagenna.stdcla.utils.AppProperties;

public class FattAassModel implements PropertyChangeListener {
  private static final Logger     s_log = LogManager.getLogger(FattAassModel.class);
  @Getter
  private static FattAassModel    inst;
  @Getter @Setter
  private AppProperties           props;
  @Getter @Setter
  private Map<String, RecIntesta> mapIntesta;
  @Getter
  private RecIntesta              recIntesta;
  @Getter
  private FileGest                fileGest;
  @Getter
  private EServerId               serverId;
  @Getter
  private DBConn                  dbconn;
  @Getter @Setter
  private int                     qtaThreads;
  @Getter @Setter
  FileFattura.ETipoBatch          tipoBatch;

  private PropertyChangeSupport       propsChange;
  @Getter
  private DBConnFactory               dbFactory;
  @SuppressWarnings("unused")
  private SqlIntesta                  sqlIntesta;
  private Map<String, ISql>           mapSQL;
  @Getter @Setter
  private ObservableList<FileFattura> fattureObs;
  //  @Getter @Setter
  //  private List<FileFattura>           listFilesFattura;
  @Getter @Setter
  private boolean genRandomFatt;
  @Getter @Setter
  private boolean debug;
  @Getter @Setter
  private boolean saveHTML;
  @Getter @Setter
  private boolean saveCSV;
  @Getter @Setter
  private boolean saveTXT;
  @Getter @Setter
  private boolean overwriteFatt;
  @Getter @Setter
  private boolean lanciaExcel;

  public FattAassModel() {
    //
  }

  public boolean isSingleThread() {
    return qtaThreads <= 1;
  }

  public void initApp(AppProperties p_prop) {
    if (isGenRandomFatt()) {
      for (int i = 0; i < 5; i++)
        randomFileFatt();
    }
    props = p_prop;
    if (null != inst)
      throw new UnsupportedOperationException("Il Model è Singleton!");
    inst = this;
    mapSQL = new Hashtable<>();
    propsChange = new PropertyChangeSupport(this);
    try {
      if (null == props) {
        props = new AppProperties();
        props.leggiPropertyFile(Consts.CSZ_MAIN_PROPS, false, false);
      }
    } catch (Exception e) {
      String szMsg = "Errore lettura Properties, err=" + e.getMessage();
      s_log.error(szMsg);
      Platform.exit();
      return;
    }
    qtaThreads = props.getIntProperty(Consts.PROP_QtaThreads, 1);
    try {
      // Open Data Base
      String szDBType = props.getProperty(AppProperties.CSZ_PROP_DB_Type);
      serverId = EServerId.parse(szDBType);
      dbFactory = new DBConnFactory();
      dbconn = dbFactory.get(szDBType);
      dbconn.readProperties(props);
      dbconn.doConn();
    } catch (Exception e) {
      String szMsg = "Errore apertura model, err=" + e.getMessage();
      s_log.error(szMsg);
      //      m_mainApp.messageDialog(AlertType.ERROR, szMsg);
      //      Platform.exit();
      firePropertyChange(Consts.EVT_CloseApp, szMsg, e);
      return;
    }
    sqlIntesta = new SqlIntesta(this);
    recIntesta = null;
    int codIntest = props.getIntProperty(Consts.PROP_LastIntesta, -1);
    if (codIntest < 1)
      codIntest = 1;
    if (codIntest >= 0)
      recIntesta = getRecIntesta(codIntest);
  }

  public ISql getFatturaInserter(ETipoFatt ptp, EServerId ids, boolean bSingleThrd) {
    ISql isql = null;
    if ( !bSingleThrd) {
      isql = FactorySql.getFatturaInserter(ptp, ids);
      return isql;
    }
    String szSqlInserter = String.format("%s_%s", ids, ptp.getTitolo());
    if (mapSQL.containsKey(szSqlInserter))
      return mapSQL.get(szSqlInserter);
    isql = FactorySql.getFatturaInserter(ptp, ids);
    mapSQL.put(szSqlInserter, isql);
    return isql;
  }

  public RecIntesta getRecIntesta(int p_i) {
    RecIntesta rec = null;
    List<RecIntesta> li = mapIntesta.values() //
        .stream() //
        .filter(s -> s.getIdIntestaInt() == p_i) //
        .collect(Collectors.toList());
    if (li != null && li.size() > 0)
      rec = li.get(0);
    return rec;
  }

  public RecIntesta getRecIntesta(String pNome) {
    RecIntesta rec = null;
    List<RecIntesta> li = mapIntesta.values() //
        .stream() //
        .filter(s -> s.getNomeIntesta().equalsIgnoreCase(pNome)) //
        .collect(Collectors.toList());
    if (li != null && li.size() > 0)
      rec = li.get(0);
    return rec;
  }

  public void setRecIntesta(RecIntesta p_reci) {
    setIntesta(p_reci, true);
  }

  public void setIntesta(RecIntesta lrecIntesta, boolean bReload) {
    if (null == lrecIntesta) {
      return;
    }
    if ( !bReload) {
      if (null != recIntesta && recIntesta.equals(lrecIntesta))
        return;
    }
    recIntesta = lrecIntesta;
    firePropertyChange(Consts.EVT_CHANGE_INTESTA, null, recIntesta);
  }

  public List<FileFattura> getListFilesFattura() {
    if (null == fileGest)
      return null;
    return fileGest.getLiFatture();
  }

  public void importaLeFatture(List<FileFattura> liIn) {
    // System.out.println("FattAassModel.eseguiConversione()");
    // TimerMeter tt = new TimerMeter("parse PDF x Fattura");
    setTipoBatch(ETipoBatch.importa);
    // fileGest = new FileGest(this);  mi cancella tutte le fatture listate sin qui
    try {
      fileGest.importaFattureInDB(liIn); // backg-03
    } catch (ReadFattException e) {
      s_log.error("Errore import lista fatture, err={}", e.getMessage());
    }
  }

  public void mergeAllFutures() {
    fileGest.mergeAllFutures();
    fattureObs = FXCollections.observableArrayList();
    List<FileFattura> li = fileGest.getLiFatture();
    if (null != li)
      fattureObs.addAll(li);
  }

  public void rescanDirAndDb() {
    fattureObs = null;
    try {
      setTipoBatch(ETipoBatch.indovina);
      fileGest = new FileGest(this);
      fileGest.indovinaFilesAndDB();
      fattureObs = FXCollections.observableArrayList();
      fattureObs.addAll(fileGest.getLiFatture());
      firePropertyChange(Consts.EVT_File05ListReady, null, fattureObs);
    } catch (ReadFattException e) {
      s_log.error("Errore scan Dir/DB, err={}", e.getMessage());
    }
    return;
  }

  public DBConn newDBConn() {
    DBConn lconn = null;
    try {
      // Open Data Base
      String szDBType = props.getProperty(AppProperties.CSZ_PROP_DB_Type);
      lconn = dbFactory.get(szDBType);
      lconn.readProperties(props);
      lconn.doConn();
    } catch (Exception e) {
      String threadName = Thread.currentThread().getName();
      String szMsg = "{}, Errore apertura connessione DB , err=" + e.getMessage();
      s_log.error(szMsg, threadName);
    }
    return lconn;
  }

  public boolean isValidRecIntesta(RecIntesta p_newRec) {
    boolean bRet = false;
    bRet = !mapIntesta.containsKey(p_newRec.getNomeIntesta());
    return bRet;
  }

  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    propsChange.addPropertyChangeListener(pcl);
  }

  public void removePropertyChangeListener(PropertyChangeListener pcl) {
    propsChange.removePropertyChangeListener(pcl);
  }

  public void firePropertyChange(String voice, Object oldv, Object newv) {
    propsChange.firePropertyChange(voice, oldv, newv);
  }

  public SqlIntesta getSqlIntesta() {
    //
    return null;
  }

  public void closeApp(AppProperties prop) {
    for (PropertyChangeListener pl : propsChange.getPropertyChangeListeners())
      propsChange.removePropertyChangeListener(pl);
  }

  public FileFattura randomFileFatt() {
    FileFattura fft = new FileFattura(this);
    fft.random();
    List<FileFattura> liFilesFattura = new ArrayList<>();
    liFilesFattura.add(fft);
    if (null == fattureObs) {
      fattureObs = FXCollections.observableArrayList();
      fattureObs.addAll(liFilesFattura);
    } else
      fattureObs.add(fft);
    s_log.info("Aggiunto nuovo FileFattura: {}", fft.shortName());
    return fft;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    var sz = evt.getPropertyName();
    System.out.printf("FattAassModel.propertyChange(%s)\n", sz);
    // var val = evt.getNewValue();
    // @SuppressWarnings("unused") double currProgressNo = 0;
    //    switch (sz) {
    //      case Consts.EVT_CloseApp:
    //        closeApp(props);
    //        break;
    //
    //    }
  }
}
