package prova.pdf;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.junit.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sm.clagenna.fattaass.data.Consts;
import sm.clagenna.fattaass.data.FattAassModel;
import sm.clagenna.fattaass.data.FileFattura;
import sm.clagenna.fattaass.data.FileGest;
import sm.clagenna.fattaass.data.RecIntesta;
import sm.clagenna.fattaass.data.FileFattura.ETipoBatch;
import sm.clagenna.fattaass.sys.ex.ReadFattException;

public class P04ScanFiles implements PropertyChangeListener {
  private FattAassModel          model;
  private RecIntesta             intesta;
  private List<FileFattura>      listFilesFattura;
  private ObservableList<Object> fattureObs;
  private Integer                totFatt2Insert;
  private Integer                FattCounter;

  @Test
  public void doTheJob() throws ReadFattException {
    init();
    scanDirDB();
  }

  /**
   * Lanciato dal'evento Consts.EVT_CHANGE_INTESTA
   */
  private void scanDirDB() {
    FileGest fige = new FileGest(model);
    fattureObs = FXCollections.observableArrayList();
    try {
      listFilesFattura = fige.indovinaFilesAndDB();
      fattureObs.addAll(listFilesFattura);
      fige.importaFattureInDB(listFilesFattura);
    } catch (ReadFattException e) {
      e.printStackTrace();
      return;
    }
    // listFilesFattura.stream().forEach(System.out::println);
    System.out.println("FINE ---> P04ScanFiles.scanDirDB()");
  }

  private void init() {
    System.setProperty("org.slf4j.simpleLogger.log.org.mabb.fontverter.opentype.TtfInstructions", "warn");
    model = new FattAassModel();
    model.initApp(null);
    model.addPropertyChangeListener(this);
    model.setOverwriteFatt(true);
    model.setTipoBatch(ETipoBatch.indovina);
    intesta = model.getRecIntesta(3);
    model.setRecIntesta(intesta);
    System.out.println("Partenza elaborazione\n------------------------------");

  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String szevt = evt.getPropertyName();
    System.out.printf("------> ProvaScanFiles.propertyChange(%s)\n", szevt);
    switch (szevt) {
      case Consts.EVT_CloseApp:
        System.exit(1257);
        break;

      case Consts.EVT_CHANGE_INTESTA:
        // scanDirDB();
        RecIntesta rec = (RecIntesta) evt.getNewValue();
        System.out.printf("Cambiato Intestatario con: %s\n", rec.getNomeIntesta());
        break;
        
      case Consts.EVT_FattToInsert:
        totFatt2Insert = (Integer) evt.getNewValue();
        FattCounter = 0;
        break;
        
      case Consts.EVT_FattInDbInserted:
        progressio();
        break;
    }
  }

  private void progressio() {
    if (0 == totFatt2Insert)
      return;
    String szBarra = "---+".repeat(totFatt2Insert);
    FattCounter++;
    int perc = (int) ( ( ((double) FattCounter) / ((double) totFatt2Insert)) * 100f);
    String szPerc = String.format("%d ", perc);
    String szFatto = "####".repeat(FattCounter);
    String szOut1 = szPerc + szFatto.substring(szPerc.length());
    String szOut2 = szOut1 + szBarra.substring(szOut1.length());
    System.out.println(szOut2);
  }

}
