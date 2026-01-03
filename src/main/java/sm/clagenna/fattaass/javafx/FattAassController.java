package sm.clagenna.fattaass.javafx;

import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.fattaass.data.Consts;
import sm.clagenna.fattaass.data.FattAassModel;
import sm.clagenna.fattaass.data.FileFattura;
import sm.clagenna.fattaass.data.RecIntesta;
import sm.clagenna.fattaass.enums.ETipoFatt;
import sm.clagenna.fattaass.sql.SqlIntesta;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ILog4jReader;
import sm.clagenna.stdcla.utils.Log4jRow;
import sm.clagenna.stdcla.utils.MioAppender;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class FattAassController implements Initializable, ILog4jReader, IStartApp, PropertyChangeListener {
  private static final Logger       s_log = LogManager.getLogger(FattAassController.class);
  @Getter
  private static FattAassController controller;
  // AI Gemini: "how can i colorize javafx tableview row but not selected one"
  // Define the custom state
  private PseudoClass cssInError = PseudoClass.getPseudoClass(Consts.cssClass_InError);
  private PseudoClass cssNewPdf  = PseudoClass.getPseudoClass(Consts.cssClass_NewPdf);
  private PseudoClass cssNoExPdf = PseudoClass.getPseudoClass(Consts.cssClass_NoExPdf);

  @FXML
  private ComboBox<RecIntesta> cbIntesta;
  @FXML
  private TextField            txDirFatt;
  @FXML
  private Button               btCercaDir;
  @FXML
  private Button               btConvPDF;
  @FXML
  private Button               btShowResults;
  @FXML
  private CheckBox             ckGenHtml;
  @FXML
  private CheckBox             ckGenTXT;
  @FXML
  private CheckBox             ckGenCSV;
  @FXML
  private CheckBox             ckOverwrite;
  @FXML
  private CheckBox             ckLanciaExcel;
  @FXML
  private MenuItem             mnuRescanDirs;
  @FXML
  private MenuItem             mnuExit;
  @FXML
  private MenuItem             mnuEditIntesta;
  @FXML
  private MenuItem             mnuHelpFattAass;

  @FXML
  private TableView<FileFattura>           tblFatt;
  @FXML
  private TableColumn<FileFattura, String> colTpFatt;
  @FXML
  private TableColumn<FileFattura, Number> colAnnoComp;
  @FXML
  private TableColumn<FileFattura, String> colDtEmiss;
  @FXML
  private TableColumn<FileFattura, String> colDtIniz;
  @FXML
  private TableColumn<FileFattura, String> colDtFine;
  @FXML
  private TableColumn<FileFattura, String> colTotFattura;
  @FXML
  private TableColumn<FileFattura, String> colFullPath;
  @FXML
  private TableColumn<FileFattura, String> colStatus;

  @FXML
  private SplitPane spltPane;

  @FXML
  private TableView<Log4jRow>           tblLogs;
  @FXML
  private TableColumn<Log4jRow, String> colTime;
  @FXML
  private TableColumn<Log4jRow, String> colLev;
  @FXML
  private TableColumn<Log4jRow, String> colMsg;
  @FXML
  private Button                        btClearMsg;
  @FXML
  private ComboBox<Level>               cbLevelMin;
  @FXML
  private Label                         lbProgressione;
  @FXML
  private ProgressBar                   progressBar;

  @Getter @Setter
  private MainFattAass   mainApp;
  private AppProperties  props;
  private FattAassModel  model;
  private Level          levelMin;
  private List<Log4jRow> m_liMsgs;
  private Scene          myScene;
  @SuppressWarnings("unused")
  private ViewRecIntesta cntrlIntesta;
  private boolean        m_cbIntestaAdding;
  private double         qtaFattToInsert;
  private double         qtaFattInserted;
  private int            qtaFiles;
  // private boolean        bAllPainted;

  public FattAassController() {
    //
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    MioAppender.setLogReader(this);
    controller = this;
    mainApp = MainFattAass.getInst();
    props = mainApp.getProps();
    levelMin = Level.INFO;
    initApp(props);
  }

  public Stage getStage() {
    Stage stg = mainApp.getPrimaryStage();
    return stg;
  }

  public Scene getScene() {
    if (null == myScene) {
      myScene = mainApp.getPrimaryStage().getScene();
    }
    return myScene;
  }

  @Override
  public void initApp(AppProperties props) {
    getStage().setTitle("Fatture AASS");
    // vedi:
    // https://stackoverflow.com/questions/27160951/javafx-open-another-fxml-in-the-another-window-with-button
    getStage().onCloseRequestProperty().setValue(e -> Platform.exit());
    model = FattAassModel.getInst();
    model.addPropertyChangeListener(this);
    String szPos = props.getProperty(Consts.CSZ_MAIN_SPLITPOS);
    if (szPos != null) {
      double dbl = Double.valueOf(szPos);
      spltPane.setDividerPositions(dbl);
    }
    ckLanciaExcel.selectedProperty().addListener((ck, ol, nw) -> model.setLanciaExcel(ckLanciaExcel.isSelected()));
    aggiornaCbIntesta();
    initTblCols();
    loadTblValues();
    initCtxMenu();
    initTblLogs();
    JFXUtils.readPosStage(getStage(), props, "main");
  }

  /**
   * <pre>
   * private TableColumn<FileFattura, Number> colAnnoComp;
   * private TableColumn<FileFattura, String> colTpFatt;
   * private TableColumn<FileFattura, String> colDtEmiss;
   * private TableColumn<FileFattura, String> colDtIniz;
   * private TableColumn<FileFattura, String> colDtFine;
   * private TableColumn<FileFattura, Number> colTotFattura;
   * private TableColumn<FileFattura, String> colFullPath;
   * private TableColumn<FileFattura, String> colStatus;
   * </pre>
   */
  private void initTblCols() {
    tblFatt.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    tblFatt.getColumns().clear();
    // ---------------------------------------------------------------------
    // Colorazione delle righe della tabella
    // ---------------------------------------------------------------------

    tblFatt.setRowFactory(tv -> new TableRow<FileFattura>() {
      @Override
      protected void updateItem(FileFattura item, boolean empty) {
        super.updateItem(item, empty);
        //        if (bAllPainted)
        //          return;
        // Reset state for empty/cleared rows
        pseudoClassStateChanged(cssInError, false);
        pseudoClassStateChanged(cssNoExPdf, false);
        pseudoClassStateChanged(cssNewPdf, false);
        if (item != null && !empty) {
          if (item.isInError()) {
            // System.out.printf("ERR: %s\n", item.toString());
            pseudoClassStateChanged(cssInError, true);
          } else if ( !item.isFileExist()) {
            // System.out.printf("!Exist; %s\n", item.toString());
            pseudoClassStateChanged(cssNoExPdf, true);
          } else if ( !item.isInDb()) {
            // System.out.printf("!DB; %s\n", item.toString());
            pseudoClassStateChanged(cssNewPdf, true);
          }
        }
      }
    });
    // ---------------------------------------------------------------------

    colTpFatt = new TableColumn<>("Tipo");
    colTpFatt.setCellValueFactory(parm -> {
      ETipoFatt vv = parm.getValue().getTipoFatt();
      var newval = null == vv ? "" : vv.toString();
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colTpFatt.setStyle(Consts.cssAlignL);
    tblFatt.getColumns().add(colTpFatt);

    colAnnoComp = new TableColumn<>("Anno");
    colAnnoComp.setCellValueFactory(parm -> {
      Number vv = parm.getValue().getAnnoComp();
      Integer newval = Integer.valueOf(0);
      if (null != vv && vv.doubleValue() != 0) {
        newval = vv.intValue();
      }
      var cel = new SimpleIntegerProperty();
      cel.set(newval);
      return cel;
    });
    colAnnoComp.setStyle(Consts.cssAlignR);
    tblFatt.getColumns().add(colAnnoComp);

    colDtEmiss = new TableColumn<>("Emissione");
    colDtEmiss.setCellValueFactory(param -> {
      // var vv = param.getValue().getDtEmiss();
      var rec = param.getValue();
      var newval = ParseData.formatDate(rec.getDtEmiss());
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colDtEmiss.setStyle(Consts.cssAlignR);
    tblFatt.getColumns().add(colDtEmiss);

    colDtIniz = new TableColumn<>("Inizio");
    colDtIniz.setCellValueFactory(param -> {
      var vv = param.getValue().getDtIniz();
      var newval = ParseData.formatDate(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colDtIniz.setStyle(Consts.cssAlignR);
    tblFatt.getColumns().add(colDtIniz);

    colDtFine = new TableColumn<>("Fine");
    colDtFine.setCellValueFactory(param -> {
      var vv = param.getValue().getDtFine();
      var newval = ParseData.formatDate(vv);
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colDtFine.setStyle(Consts.cssAlignR);
    tblFatt.getColumns().add(colDtFine);

    colTotFattura = new TableColumn<>("Importo");
    colTotFattura.setCellValueFactory(parm -> {
      Number vv = parm.getValue().getTotFattura();
      String newval = "";
      if (null != vv) {
        newval = Utils.formatDouble(vv.doubleValue());
      }
      var cel = new SimpleStringProperty();
      cel.set(newval);
      return cel;
    });
    colTotFattura.setStyle(Consts.cssAlignR);
    tblFatt.getColumns().add(colTotFattura);

    colFullPath = new TableColumn<>("File");
    colFullPath.setCellValueFactory(param -> {
      String sz = param.getValue().getFullPath().toString();
      // if ( !param.getValue().isFileExist())
      // sz += " (*not exist! **)";
      // if ( !param.getValue().isInDb())
      // sz += " (*No DB **)";
      var cel = new SimpleStringProperty();
      cel.set(sz);
      return cel;
    });
    tblFatt.getColumns().add(colFullPath);

    colStatus = new TableColumn<>("Status");
    colStatus.setCellValueFactory(param -> {
      String sz = "";
      FileFattura fatt = param.getValue();
      if (fatt.isInError()) {
        sz = "In Error!";
      } else {
        if ( !fatt.isFileExist()) {
          sz += "No File!,";
        }
        sz += fatt.isInDb() ? "DB present" : "Not in DB";
      }
      var cel = new SimpleStringProperty();
      cel.set(sz);
      return cel;
    });
    tblFatt.getColumns().add(colStatus);

    // Impostazione delle dimensioni delle colonne
    double vv = props.getDoubleProperty(Consts.CSZ_tbfatt_Anno);
    if (vv > 0) {
      colAnnoComp.setPrefWidth(vv);
    }
    vv = props.getDoubleProperty(Consts.CSZ_tbfatt_Tpfatt);
    if (vv > 0) {
      colTpFatt.setPrefWidth(vv);
    }
    vv = props.getDoubleProperty(Consts.CSZ_tbfatt_DtEmiss);
    if (vv > 0) {
      colDtEmiss.setPrefWidth(vv);
    }
    vv = props.getDoubleProperty(Consts.CSZ_tbfatt_DtIniz);
    if (vv > 0) {
      colDtIniz.setPrefWidth(vv);
    }
    vv = props.getDoubleProperty(Consts.CSZ_tbfatt_DtFine);
    if (vv > 0) {
      colDtFine.setPrefWidth(vv);
    }
    vv = props.getDoubleProperty(Consts.CSZ_tbfatt_TotFatt);
    if (vv > 0) {
      colTotFattura.setPrefWidth(vv);
    }
    vv = props.getDoubleProperty(Consts.CSZ_tbfatt_FullPath);
    if (vv > 0) {
      colFullPath.setPrefWidth(vv);
    }
    vv = props.getDoubleProperty(Consts.CSZ_tbfatt_Status);
    if (vv > 0) {
      colStatus.setPrefWidth(vv);
    }
  }

  private void loadTblValues() {
    tblFatt.getItems().clear();
    ObservableList<FileFattura> filesObsFatt = model.getFattureObs();
    if (null == filesObsFatt || filesObsFatt.size() == 0) {
      return;
    }
    // ObservableList<FileFattura> liFiles =
    // FXCollections.observableList(filesObsFatt);
    tblFatt.getItems().addAll(filesObsFatt);
  }

  private void initCtxMenu() {
    ContextMenu ctx = new ContextMenu();
    MenuItem mnu1 = new MenuItem("Import Fattura");
    mnu1.setOnAction((ActionEvent ev) -> batchImportFatture()); // backg-00
    ctx.getItems().add(mnu1);

    MenuItem mnu2 = new MenuItem("Vedi PDF");
    mnu2.setOnAction((ActionEvent ev) -> {
      showPdfDoc();
    });
    ctx.getItems().add(mnu2);
    tblFatt.setContextMenu(ctx);
  }

  private void batchImportFatture() {
    // FileFattura fatt = tblFatt.getSelectionModel().getSelectedItem();
    model.setSaveHTML(ckGenHtml.isSelected());
    model.setSaveTXT(ckGenTXT.isSelected());
    model.setSaveCSV(ckGenCSV.isSelected());
    model.setOverwriteFatt(ckOverwrite.isSelected());
    
    getScene().setCursor(Cursor.WAIT);
    List<FileFattura> li = new ArrayList<>();
    for (FileFattura ff : tblFatt.getSelectionModel().getSelectedItems()) {
      li.add(ff);
    }
    if (li.size() == 0) {
      mainApp.messageDialog(AlertType.WARNING, "Non hai selezionato nessuna riga di fattura!");
      return;
    }
    model.importaLeFatture(li); // backg-02
  }

  private void mergeAllFutures() {
    model.mergeAllFutures();
    loadTblValues();
  }

  private void showPdfDoc() {
    FileFattura fatt = tblFatt.getSelectionModel().getSelectedItem();
    Path fi = fatt.getFullPath();
    // System.out.println("Ctx menu: path="+it);
    try {
      if (Desktop.isDesktopSupported()) {
        s_log.info("Apro lettore PDF per {}", fi.toString());
        Desktop.getDesktop().open(fi.toFile());
      } else {
        s_log.error("Desktop not supported");
      }
    } catch (IOException e) {
      s_log.error("Desktop PDF launch error:" + e.getMessage(), e);
    }
  }

  private void initTblLogs() {
    tblLogs.setPlaceholder(new Label("Nessun messaggio da mostrare" + ""));
    tblLogs.setFixedCellSize(21.0);
    tblLogs.setRowFactory(row -> new TableRow<Log4jRow>() {
      @Override
      public void updateItem(Log4jRow item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
          setStyle("");
          return;
        }
        String cssSty = "-fx-background-color: ";
        Level tip = item.getLevel();
        StandardLevel lev = tip.getStandardLevel();
        switch (lev) {
          case TRACE:
            cssSty += "beige";
            break;
          case DEBUG:
            cssSty += "silver";
            break;
          case INFO:
            cssSty = "";
            break;
          case WARN:
            cssSty += "coral";
            break;
          case ERROR:
            cssSty += "hotpink";
            break;
          case FATAL:
            cssSty += "deeppink";
            break;
          default:
            cssSty = "";
            break;
        }
        setStyle(cssSty);
      }
    });

    colTime.setMaxWidth(80.);
    colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
    double vv = props.getDoubleProperty(Consts.CSZ_tblog_COL_time);
    if (vv > 0) {
      colTime.setPrefWidth(vv);
    }
    colLev.setMaxWidth(60.0);
    colLev.setCellValueFactory(new PropertyValueFactory<>("level"));
    vv = props.getDoubleProperty(Consts.CSZ_tblog_COL_Level);
    if (vv > 0) {
      colLev.setPrefWidth(vv);
    }
    colMsg.setCellValueFactory(new PropertyValueFactory<>("message"));
    vv = props.getDoubleProperty(Consts.CSZ_tblog_COL_mesg);
    if (vv > 0) {
      colMsg.setPrefWidth(vv);
    }
    cbLevelMin.getItems().addAll(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL);
    cbLevelMin.getSelectionModel().select(levelMin);
    // questa
    tblLogs.getItems()
        .addListener((ListChangeListener<Log4jRow>) s -> Platform.runLater(() -> tblLogs.scrollTo(s.getList().size() - 1)));
    // -------- combo level -------
    if (props != null) {
      String sz = props.getProperty(Consts.CSZ_LOG_LEVEL);
      if (sz != null) {
        levelMin = Level.toLevel(sz);
      }
    }
  }

  @FXML
  void btCercaClick(ActionEvent event) {

  }

  @FXML
  void btClearMsgClick(ActionEvent event) {
    // System.out.println("ReadFattHTMLController.btClearMsgClick()");
    tblLogs.getItems().clear();
    if (m_liMsgs != null) {
      m_liMsgs.clear();
    }
    m_liMsgs = null;
  }

  @FXML
  void btDatiClick(ActionEvent event) {
    // System.out.println("FattAassController.btDatiClick()");
    Stage stage = getStage();

    URL url = getClass().getResource(ResultView.CSZ_FXMLNAME);
    if (url == null) {
      url = getClass().getClassLoader().getResource(ResultView.CSZ_FXMLNAME);
    }
    Parent radice;
    ResultView cntrlResView = null;
    FXMLLoader fxmlLoad = new FXMLLoader(url);
    try {
      // radice = FXMLLoader.load(url);
      radice = fxmlLoad.load();
      cntrlResView = fxmlLoad.getController();
    } catch (IOException e) {
      s_log.error("Errore caricamento FXML {}", ResultView.CSZ_FXMLNAME, e);
      return;
    }
    // Node nod = radice;
    // do {
    // controller = (ResultView) nod.getProperties().get("refToCntrl");
    // nod = nod.getParent();
    // } while (controller == null && nod != null);

    Stage stageResults = new Stage();
    Scene scene = new Scene(radice, 600, 440);
    stageResults.setScene(scene);
    stageResults.setWidth(800);
    stageResults.setHeight(600);
    stageResults.setX(0);
    stageResults.setY(0);
    stageResults.initOwner(stage);
    stageResults.initModality(Modality.NONE);
    stageResults.setTitle("Analizza i dati del DB delle fatture AASS");
    URL icoURL = getClass().getResource(Consts.CSZ_REVIEW_ICON);
    if (null == icoURL) {
      icoURL = getClass().getClassLoader().getResource(Consts.CSZ_REVIEW_ICON);
    }
    if (null != icoURL) {
      Image ico = new Image(icoURL.toExternalForm());
      stageResults.getIcons().add(ico);
    } else {
      s_log.error("Non trovo icona {}", Consts.CSZ_REVIEW_ICON);
    }
    // verifica che nel FXML ci sia la dichiarazione:
    // <userData> <fx:reference source="controller" /> </userData>
    if (cntrlResView != null) {
      cntrlResView.setMyScene(scene);
      cntrlResView.initApp(props);
    }
    stageResults.show();
  }

  @FXML
  void btImportClick(ActionEvent event) {
    try {
      batchImportFatture();
    } finally {
      getScene().setCursor(Cursor.DEFAULT);
    }
  }

  @FXML
  void cbIntestaClick(ActionEvent event) {
    if (m_cbIntestaAdding) {
      return;
    }
    RecIntesta lrecIntesta = cbIntesta.getSelectionModel().getSelectedItem();
    s_log.info("Selezionato intestatario: {}", lrecIntesta.getNomeIntesta());
    model.setRecIntesta(lrecIntesta);
  }

  @FXML
  void cbLevelMinSel(ActionEvent event) {
    levelMin = cbLevelMin.getSelectionModel().getSelectedItem();
    // System.out.println("ReadFattHTMLController.cbLevelMinSel():" +
    // levelMin.name());
    tblLogs.getItems().clear();
    if (m_liMsgs == null || m_liMsgs.size() == 0) {
      return;
    }
    // List<Log4jRow> li = m_liMsgs.stream().filter(s ->
    // s.getLevel().isInRange(Level.FATAL, levelMin )).toList(); //
    // !s.getLevel().isLessSpecificThan(levelMin)).toList();
    List<Log4jRow> li = m_liMsgs.stream().filter(s -> s.getLevel().intLevel() <= levelMin.intLevel()).toList();
    tblLogs.getItems().addAll(li);
  }

  @FXML
  void mnuEditIntestaClick(ActionEvent event) {

  }

  @FXML
  void mnuExitClick(ActionEvent event) {
    Platform.exit();
  }

  @FXML
  void mnuRescanDirsClick(ActionEvent event) {
    // System.out.println("FattAassController.mnuRescanDirs()");
    getScene().setCursor(Cursor.WAIT);
    model.setSaveHTML(ckGenHtml.isSelected());
    model.setSaveCSV(ckGenCSV.isSelected());
    model.setSaveTXT(ckGenTXT.isSelected());
    model.setOverwriteFatt(ckOverwrite.isSelected());
    // Platform.runLater(() -> {
    model.rescanDirAndDb();
    loadTblValues();
    // });
  }

  @FXML
  void onEnterDirPDF(ActionEvent event) {
    System.out.println("FattAassController.onEnterDirPDF()");
  }

  @FXML
  void premutoTasto(KeyEvent event) {
    System.out.println("FattAassController.premutoTasto()");
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    var sz = evt.getPropertyName();
    // var val = evt.getNewValue();
    // @SuppressWarnings("unused") double currProgressNo = 0;

    switch (sz) {
      // ------------------------------------------------------------------------
      case Consts.EVT_CHANGE_INTESTA:
        RecIntesta lRecInt = model.getRecIntesta();
        txDirFatt.setText(lRecInt.getDirFatture().toString());
        Platform.runLater(() -> mnuRescanDirsClick(null));
        break;

      // ------------------------------------------------------------------------
      // case Consts.EVT_File00Start:
      // Platform.runLater(() -> getStage().getScene().setCursor(Cursor.WAIT));
      // break;

      // ------------------------------------------------------------------------
      // case Consts.EVT_File03NameChanged:
      // Platform.runLater(() -> getStage().getScene().setCursor(Cursor.DEFAULT));
      // break;

      case Consts.EVT_File00Start:
        List<?> li = (List<?>) evt.getNewValue();
        if (li instanceof List<?> liFiDir) {
          qtaFiles = liFiDir.size();
          String szProgr = "Progr. 0%%";
          lbProgressione.setText(szProgr);
          progressBar.setProgress(0);
          //          bAllPainted = false;
        }
        break;

      case Consts.EVT_File01ListDaDir:
        int qta = (int) evt.getNewValue();
        Platform.runLater(() -> {
          double dbl = qta / qtaFiles * 100f;
          String szProgr = String.format("Progr. %d%%", (int) dbl);
          lbProgressione.setText(szProgr);
          progressBar.setProgress(dbl);
          // getScene().setCursor(Cursor.DEFAULT);
        });
        break;

      // ------------------------------------------------------------------------
      case Consts.EVT_File05ListReady:
        Platform.runLater(() -> {
          lbProgressione.setText("Done 100%");
          progressBar.setProgress(1f);
          //          bAllPainted = true;
        });
        break;

      // ------------------------------------------------------------------------
      case Consts.EVT_FattToInsert:
        qtaFattToInsert = ((Integer) evt.getNewValue()).doubleValue();
        qtaFattInserted = 0f;
        lbProgressione.setText("Progr. 0%");
        s_log.debug("Progression 0%");
        progressBar.setProgress(0);
        break;

      // ------------------------------------------------------------------------
      case Consts.EVT_FattInDbInserted:
        if (0 == qtaFattToInsert) {
          break;
        }
        Platform.runLater(() -> {
          qtaFattInserted++;
          double dbl = qtaFattInserted / qtaFattToInsert * 100f;
          String szProgr = String.format("Progr. %d%%", (int) dbl);

          lbProgressione.setText(szProgr);
          progressBar.setProgress(dbl);
          s_log.debug("Progression {}", szProgr);
        });
        break;

      // ------------------------------------------------------------------------
      case Consts.EVT_CloseApp:
        closeApp(props);
        break;

      // ------------------------------------------------------------------------
      case Consts.EVT_ENDALLPROCESS:
        Platform.runLater(() -> {
          lbProgressione.setText("Done 100%");
          progressBar.setProgress(1f);
          getScene().setCursor(Cursor.DEFAULT);
          mergeAllFutures();
        });
        break;
    }
  }

  public SqlIntesta aggiornaCbIntesta() {
    SqlIntesta sqintesta = null;
    if (m_cbIntestaAdding) {
      return sqintesta;
    }
    try {
      m_cbIntestaAdding = true;
      List<RecIntesta> li = new ArrayList<>(model.getMapIntesta().values());
      cbIntesta.getItems().clear();
      cbIntesta.getItems().addAll(li);
    } finally {
      m_cbIntestaAdding = false;
    }
    return sqintesta;
  }

  @Override
  public void changeSkin() {
    MainFattAass mainApp = MainFattAass.getInst();
    URL url = mainApp.getUrlCSS();
    Scene myScene = getStage().getScene();
    if (null == url || null == myScene) {
      return;
    }
    myScene.getStylesheets().clear();
    myScene.getStylesheets().add(url.toExternalForm());
    // if (null != cntrResultView)
    // cntrResultView.changeSkin();
  }

  @Override
  public void closeApp(AppProperties p_props) {
    double vv = colAnnoComp.getWidth();
    p_props.setProperty(Consts.CSZ_tbfatt_Anno, Integer.valueOf((int) vv));
    vv = colTpFatt.getWidth();
    p_props.setProperty(Consts.CSZ_tbfatt_Tpfatt, Integer.valueOf((int) vv));
    vv = colDtEmiss.getWidth();
    p_props.setProperty(Consts.CSZ_tbfatt_DtEmiss, Integer.valueOf((int) vv));
    vv = colDtIniz.getWidth();
    p_props.setProperty(Consts.CSZ_tbfatt_DtIniz, Integer.valueOf((int) vv));
    vv = colDtFine.getWidth();
    p_props.setProperty(Consts.CSZ_tbfatt_DtFine, Integer.valueOf((int) vv));
    vv = colTotFattura.getWidth();
    p_props.setProperty(Consts.CSZ_tbfatt_TotFatt, Integer.valueOf((int) vv));
    vv = colFullPath.getWidth();
    p_props.setProperty(Consts.CSZ_tbfatt_FullPath, Integer.valueOf((int) vv));
    vv = colStatus.getWidth();
    p_props.setProperty(Consts.CSZ_tbfatt_Status, Integer.valueOf((int) vv));

    p_props.setProperty(Consts.CSZ_LOG_LEVEL, levelMin.toString());
    double[] pos = spltPane.getDividerPositions();
    String szPos = String.format("%.4f", pos[0]).replace(",", ".");
    p_props.setProperty(Consts.CSZ_MAIN_SPLITPOS, szPos);

    vv = colTime.getWidth();
    p_props.setProperty(Consts.CSZ_tblog_COL_time, Integer.valueOf((int) vv));
    vv = colLev.getWidth();
    p_props.setProperty(Consts.CSZ_tblog_COL_Level, Integer.valueOf((int) vv));
    vv = colMsg.getWidth();
    p_props.setProperty(Consts.CSZ_tblog_COL_mesg, Integer.valueOf((int) vv));

    JFXUtils.savePosStage(getStage(), props, "main");
  }

  @Override
  public void addLog(String[] p_arr) {
    // [0] - class emitting
    // [1] - timestamp
    // [2] - Log Level
    // [3] - message
    // System.out.println("addLog=" + String.join("\t", p_arr));
    Log4jRow riga = null;
    try {
      riga = new Log4jRow(p_arr);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (riga != null) {
      addRiga(riga);
    }
  }

  private void addRiga(Log4jRow rig) {
    if (m_liMsgs == null) {
      m_liMsgs = new ArrayList<>();
    }
    m_liMsgs.add(rig);
    // if ( rig.getLevel().isInRange( Level.FATAL, levelMin )) //
    // isLessSpecificThan(levelMin))
    if (rig.getLevel().intLevel() <= levelMin.intLevel()) {
      tblLogs.getItems().add(rig);
    }
  }

}
