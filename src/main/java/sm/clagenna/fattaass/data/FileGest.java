package sm.clagenna.fattaass.data;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.concurrent.Task;
import javafx.scene.control.Alert.AlertType;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.fattaass.enums.ETipoFatt;
import sm.clagenna.fattaass.javafx.MainFattAass;
import sm.clagenna.fattaass.sys.ex.ReadFattException;
import sm.clagenna.fattaass.sys.ex.ReadFattLog4jRowException;
import sm.clagenna.fattaass.sys.ex.ReadFattValoreException;
import sm.clagenna.stdcla.sql.Dataset;
import sm.clagenna.stdcla.sql.DtsRow;
import sm.clagenna.stdcla.utils.concurrency.CallableFutureService;
import sm.clagenna.stdcla.utils.sys.TimerMeter;

public class FileGest {
  private static final Logger s_log = LogManager.getLogger(FileGest.class);

  private FattAassModel               model;
  private RecIntesta                  recint;
  @Getter @Setter
  private boolean                     senzaThread;
  private int                         qtaThreads;
  private CallableFutureService       callblesrv;
  private List<Callable<FileFattura>> liTasks;
  @Getter
  private List<Future<FileFattura>>   listFutures;
  private List<FileFattura>           liFilesFattura;
  private List<FileFattura>           liDBFattura;
  @Getter
  private List<FileFattura>           liFatture;
  @Getter
  private Map<String, FileFattura>    mapFatture;

  public FileGest(FattAassModel p_model) {
    model = p_model;
    init();
  }

  private void init() {
    recint = model.getRecIntesta();
    qtaThreads = model.getProps().getIntProperty(Consts.PROP_QtaThreads, 1);
    if (qtaThreads <= 1)
      setSenzaThread(true);
    s_log.info("Qta Threads = {}", qtaThreads);
  }

  private void clear() throws ReadFattException {
    recint = model.getRecIntesta();
    if (null == recint)
      throw new ReadFattValoreException("Manca l'intestatario delle fatture");
    liFatture = null;
    liTasks = null;
    listFutures = null;
    liFilesFattura = null;
    liDBFattura = null;
  }

  public void importaFattureInDB(List<FileFattura> liIn) throws ReadFattException {
    // clear();
    if (isSenzaThread())
      elaboraSenzaThread(liIn);
    else
      elaboraInBackgroundTask(liIn); // backg-04
  }

  /**
   * Esegue la scansione del contenuto del direttorio indicato da
   * {@link RecIntesta} di {@link #recint} nella seguente sequenza:
   * <ol>
   * <li>list del contenuto del direttorio</li>
   * <li>Parsing (background) dei PDF per le info principali</li>
   * <li>creazione di {@link #liFatture} dai futures</li>
   * <li>eventuale rinomina del file PDF in base al TipoFattura, periodo Inizio,
   * Periodo fine preio</li>
   * </ol>
   *
   * @return
   */
  public List<FileFattura> indovinaFilesAndDB() throws ReadFattException {
    clear();
    creaElencoFilesPDFDaDir();
    creaElencoFilesPDFDaDB();
    mergeListFattureDaDBconDaDir();
    if (isSenzaThread())
      elaboraSenzaThread(liFatture);
    else
      elaboraInBackgroundTask(liFatture);
    //    creaTaskParsePDF();
    //    parsePdfInBackGround();
    //    riCreaListFilesFatturaDaFutures();
    //    renameFiles();
    riCreaListFilesFatturaDaFutures();
    renameFiles(liFatture);
    s_log.debug("Fine Parsing {} tasks", (null != listFutures ? listFutures.size() : 0));
    return liFatture;
  }

  private void elaboraSenzaThread(List<FileFattura> liIn) throws ReadFattLog4jRowException {
    listFutures = new ArrayList<>();
    for (FileFattura ff : liIn) {
      if ( !ff.isFileExist() || ff.isInDb() && !model.isOverwriteFatt() || ff.isInError()) {
        s_log.warn("Non elaboro:\"{}\"", ff.shortName());
        continue;
      }
      try {
        ff.setTipoBatch(model.getTipoBatch());
        ff.call();
        listFutures.add(new MyFutureNoThread(ff));
      } catch (Exception e) {
        s_log.error("Errore parsing \"{}\"", ff.getFullPath().toString(), e.getMessage());
      }
    }
    s_log.warn("Elaborati (No Thread)  \"{}\" files fattura", listFutures.size());
  }

  public void elaboraInBackgroundTask(List<FileFattura> liIn) throws ReadFattLog4jRowException {
    creaTaskParsePDF(liIn);
    // parsePdfInBackGround(); // backg-05
    lancioTaskLavoroBackground();
  }

  /**
   * Il "main" Thread lancia un thread figlio (veloce) per far partire tutti gli
   * N thread figli "Pool-A-thread-B" per poi aspettarli senza bloccare il
   * "main"
   * 
   * @param liIn
   */
  private void lancioTaskLavoroBackground() {
    s_log.debug("Start TASK Padre per lancio Figli");
    Task<Void> task = new Task<>() {
      protected Void call() throws Exception {
        parsePdfInBackGround();
        return null;
      }

      @Override
      protected void succeeded() {
        s_log.info("Esecuzione Task Padre Ok");
        super.succeeded();
      }

      @Override
      protected void failed() {
        s_log.error("Esecuzione Task Padre Fallita !!!");
        super.failed();
      }
    };
    new Thread(task).start();
    s_log.debug("Fine TASK Padre per lancio Figli");
  }

  /**
   * Crea un {@link CallableFutureService} per {@link #qtaThreads} per eseguire
   * il parsing dei files PDF in background. Il ritorno saranno un elenco di
   * Future in {@link #listFutures}
   *
   * @throws ReadFattLog4jRowException
   */
  private void parsePdfInBackGround() throws ReadFattLog4jRowException {
    TimerMeter tt = new TimerMeter("START parse ALL PDFs x Fattura");
    if (null == liTasks || liTasks.size() == 0) {
      model.firePropertyChange(Consts.EVT_ENDALLPROCESS, null, tt);
      return;
    }
    callblesrv = new CallableFutureService(qtaThreads);
    s_log.debug("Start lavoro \"sotto banco\" interpretazione di {} File PDF", liTasks.size());
    try {
      listFutures = callblesrv.submitTasks(liTasks); // backg-06
      aspettaLaFine();
    } catch (Exception e) {
      throw new ReadFattLog4jRowException(e.getMessage(), e);
    } finally {
      //      callblesrv.shutdown();
      //      callblesrv = null;
    }
    s_log.debug("futures={}, {}", null != listFutures ? listFutures.size() : 0, tt.stop());
  }

  private void aspettaLaFine() {
    boolean bAllDone = true;
    TimerMeter tt = new TimerMeter("Wait ALL end Threads");
    while (true) {
      bAllDone = true;
      for (Future<FileFattura> bi : listFutures) {
        try {
          bAllDone &= bi.isDone();
          //          if ( !bi.isDone()) {
          //            var th = bi.get();
          //            s_log.debug("Thread wait:\"{}\" file=\"{}\"\n", th.getThreadName(), th.getFullPath().toString());
          //          }
        } catch (Exception e) {
          s_log.error("Error waitng tasks, msg={}", e.getMessage(), e);
        }
      }
      if (bAllDone) {
        callblesrv.shutdown();
        callblesrv = null;
        model.firePropertyChange(Consts.EVT_ENDALLPROCESS, null, tt);
        break;
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        s_log.error("Errore back grround task, err={}", e.getMessage());
      }
      s_log.debug("Stop Wait All {} threads, tt={}", null != listFutures ? listFutures.size() : 0, tt.stop());
    }
  }

  /**
   * Scansiona il direttorio specificato da {@link #recint} alla ricerca di file
   * PDF inizializzando {@link FileFattura} e riempendo la lista
   * {@link #liFatture}<br/>
   * Al termine emette l'evento "EVT_File01ListDaDir"
   */
  private void creaElencoFilesPDFDaDir() {
    Path pthDirFatt = Paths.get(recint.getDirFatture());
    s_log.debug("Inizio scan Files PDF in {}", pthDirFatt.toString());
    TimerMeter tt = new TimerMeter("Scan dir PDF");
    if (null == liFilesFattura)
      liFilesFattura = new ArrayList<>();
    String szGlobMatch = "glob:*:/**/{EE_,GAS_,H2O_}*.pdf";
    szGlobMatch = "glob:*:/**/*.pdf";
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher(szGlobMatch);
    try (Stream<Path> walk = Files.walk(pthDirFatt)) {
      liFilesFattura = walk.filter(p -> !Files.isDirectory(p)) //
          // not a directory
          // .map(p -> p.toString().toLowerCase()) // convert path to string
          .filter(f -> matcher.matches(f)) // check end with
          .map(s -> creaRecFileFattura(s)) //
          .collect(Collectors.toList()); // collect all matched to a List
      if (null == liFilesFattura || liFilesFattura.size() == 0) {
        s_log.warn("Nessun file Fattura trovato in:{}", recint.getDirFatture().toString());
        return;
      }
      model.firePropertyChange(Consts.EVT_File00Start, null, liFilesFattura);
    } catch (IOException e) {
      s_log.error("Errore scan dir dei file Fattura; err={}", e.getMessage());
      return;
    }
    s_log.debug(tt.stop());
  }

  /**
   * Aggiorno i dati di {@link #liDBFattura} con i dati che mi arrivano dal DB.
   *
   * @throws ReadFattLog4jRowException
   */
  private void creaElencoFilesPDFDaDB() throws ReadFattLog4jRowException {
    TimerMeter tt = new TimerMeter("Scan PDF da DB");
    Dataset dtsFilsFatt = null;
    Integer ii = recint.getIdIntestaInt();
    String szQry = String.format(Consts.QRY_ListFilesFatture, ii);
    try (Dataset dtset = new Dataset(model.getDbconn())) {
      if ( !dtset.executeQuery(szQry)) {
        s_log.error("Errore lettura DB delle fatture");
        return;
      }
      dtsFilsFatt = dtset;
      dtsFilsFatt.close();
    } catch (IOException e) {
      s_log.error("Errore lettura DB delle fatture, err={}", e.getMessage(), e);
      throw new ReadFattLog4jRowException(e.getMessage(), e);
    }
    liDBFattura = new ArrayList<>();
    for (DtsRow row : dtsFilsFatt.getRighe()) {
      FileFattura ff2 = new FileFattura(model);
      ff2.popolaDaDB(row);
      liDBFattura.add(ff2);
      model.firePropertyChange(Consts.EVT_File04ListInDB, 0, liDBFattura.size());
    }
    s_log.debug(tt.stop());
  }

  private FileFattura creaRecFileFattura(Path pth) {
    FileFattura ff = new FileFattura(model);
    ff.setFullPath(pth);
    ff.setChanged(false);
    ff.setSelezionato(false);
    ff.setInDb(false);
    ff.setInError(false);
    ff.setFileExist(true);
    return ff;
  }

  private void creaTaskParsePDF(List<FileFattura> liIn) {
    liTasks = new ArrayList<>();
    if (null == liIn || liIn.size() == 0) {
      MainFattAass.getInst().messageDialog(AlertType.WARNING, "Non hai selezionato nessuna fattura !");
      return;
    }
    for (FileFattura ff : liIn) {
      if ( !ff.isFileExist() || ff.isInDb() && !model.isOverwriteFatt() || ff.isInError())
        continue;
      ff.setTipoBatch(model.getTipoBatch());
      liTasks.add(ff);
    }
    model.firePropertyChange(Consts.EVT_FattToInsert, 0, liTasks.size());
    s_log.debug("Ci sono {} tasks per parsare i PDF", liTasks.size());
  }

  /**
   * Aggiorno i record fattura all'interno {@link #liFFatture} con i dati
   * ottenuti dal parsing (parziale, ergo dei soli valori delle intestazioni)
   * dei files PDF analizzati durante l'elaborazione in backGround
   * {@link #parsePdfInBackGround()}<br/>
   * Al termine la funzione:
   * <ol>
   * <li>Integra le info dei {@link FileFattura} nella lista
   * {@link #liFilesFattura} con i valori dei Future</li>
   * <li>comunica al model l'elenco aggiornato</li>
   * <li>emette l'evento EVT_File02ListDaPDF</li>
   * </ol>
   *
   * @throws ReadFattLog4jRowException
   */
  private void riCreaListFilesFatturaDaFutures() throws ReadFattLog4jRowException {
    if (null == listFutures || listFutures.size() == 0)
      return;
    if (null == liFatture || liFatture.size() == 0) {
      s_log.error("Manca completamente l'elenco Fatture!, ESCO!");
      return;
    }
    try {
      for (Future<FileFattura> fu : listFutures) {
        FileFattura fattFut = fu.get();
        int indx = liFatture.indexOf(fattFut);
        if (indx >= 0) {
          FileFattura ff = liFatture.get(indx);
          ff.update(fattFut);
        } else {
          s_log.error("Non trovo PDF Fattura dal Future {}", fattFut.shortName());
          liFatture.add(fattFut);
        }
      }
    } catch (Exception e) {
      throw new ReadFattLog4jRowException(e.getMessage(), e);
    }
  }

  /**
   * Rinomino il file PDF Fattura con i dati trovati al interno del PDF stesso
   * (TipoFatt, dtIniz, DtFine)<br/>
   * Alla fine lancio l'evento <b>EVT_File03NameChanged</b>
   */
  private int renameFiles(List<FileFattura> liIn) {
    int nChanged = 0;
    for (FileFattura ff : liIn) {
      if ( !ff.isInError() //
          && ff.isFileExist() //
          && null != ff.getTipoFatt()) {
        ff.renameFattura();
        if (ff.isChanged())
          nChanged++;
      }
    }
    return nChanged;
  }

  /**
   * Trasborda il contenuto dei file PDF trovati nel DB {@link #liDBFattura} sul
   * List {@link #liFatture} facendo un <i>merge</i>
   */
  private void mergeListFattureDaDBconDaDir() {
    liFatture = new ArrayList<>();
    if (liFilesFattura == null)
      liFilesFattura = new ArrayList<>();
    liFatture.addAll(liFilesFattura);
    if (null == liDBFattura || liDBFattura.size() == 0)
      return;
    for (FileFattura ffDB : liDBFattura) {
      int indx = liFatture.indexOf(ffDB);
      if (indx >= 0) {
        // se ho il File PDF faccio il merge con i dati da DB
        FileFattura ff = liFatture.get(indx);
        ff.update(ffDB);
      } else {
        // altrimenti lo aggiungo all'elenco
        liFatture.add(ffDB);
      }
    }
    refreshMapFatture();
  }

  public void mergeAllFutures() {
    if (null == liFatture || liFatture.size() == 0) {
      s_log.error("No list fatture merge into");
      return;
    }
    if (null == listFutures || listFutures.size() == 0) {
      s_log.error("No Futures to merge ");
      return;
    }
    try {
      for (Future<FileFattura> fut : listFutures) {
        FileFattura fattFrom = fut.get();
        int indx = liFatture.indexOf(fattFrom);
        if (indx >= 0) {
          FileFattura fattTo = liFatture.get(indx);
          if (fattFrom.hashCode() == fattTo.hashCode())
            continue;
          fattTo.update(fattFrom);
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    s_log.debug("FileGest.mergeAllFutures()  --------    F I N E     ! ! !");
    refreshMapFatture();
  }

  private void refreshMapFatture() {
    mapFatture = new HashMap<String, FileFattura>();
    // key: "EE_1_1328" -> TipoFatt, idIntesta, idFattura
    for (FileFattura ff : liFatture) {
      if (null == ff.getTipoFatt() || ff.getIdFattura() == 0)
        continue;
      String key = String.format("%s_%d_%d", // 
          ff.getTipoFatt().getTitolo(), //
          model.getRecIntesta().getIdIntestaInt(), //
          ff.getIdFattura());
      mapFatture.put(key, ff);
    }
  }

  public FileFattura findFattura(ETipoFatt tpf, int idInt, int idFatt) {
    FileFattura ff = null;
    if (null == mapFatture || mapFatture.size() == 0)
      return ff;
    String key = String.format("%s_%d_%d", // 
        tpf.getTitolo(), //
        idInt, //
        idFatt);
    ff = mapFatture.get(key);
    return ff;
  }

}
