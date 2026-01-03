package sm.clagenna.fattaass.jvafxp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Date;
import java.util.Random;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Data;
import sm.clagenna.fattaass.data.Consts;
import sm.clagenna.stdcla.utils.Utils;

@Data
class ReciFatt {
  private static String ALLCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
  private int           id;
  private String        fileName;
  private Double        impValue;
  private boolean       inDb;
  private boolean       fileExist;
  private String        strCol;
  private static Random rnd;

  static {
    rnd = new Random(new Date().getTime());
  }

  public void randomize() {
    id = rnd.nextInt(1, 100);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 16; i++) {
      sb.append(ALLCHARS.charAt(rnd.nextInt(0, ALLCHARS.length())));
    }
    fileName = sb.toString();
    impValue = new BigDecimal(rnd.nextDouble(10F, 10000F)).setScale(2, RoundingMode.HALF_UP).doubleValue();
    inDb = rnd.nextBoolean();
    fileExist = rnd.nextBoolean();
    sb = new StringBuilder();
    String vir = "";
    if (inDb) {
      sb.append("in DB");
      vir = ",";
    }
    if (fileExist) {
      sb.append(vir).append("File Exist");
      vir = ",";
    }
    strCol = sb.toString();
  }

  @Override
  public String toString() {
    String im = String.format("%.2f", impValue);
    String bools = inDb ? "DB Present" : "Not in DB";
    bools += fileExist ? "File OK!" : " File !exist";
    String sz = String.format("%d) %s, %s, %s, \t%s\n", id, fileName, im, strCol, bools);
    return sz;
  }
}

public class RecFattTableView extends Application {

  private PseudoClass cssInError = PseudoClass.getPseudoClass("inError");
  private PseudoClass cssNewPdf  = PseudoClass.getPseudoClass("newFile");
  private PseudoClass cssNoExPdf = PseudoClass.getPseudoClass("noFile");

  private TableView<ReciFatt>           tblview;
  private TableColumn<ReciFatt, Number> colId;
  private TableColumn<ReciFatt, String> colFileNam;
  private TableColumn<ReciFatt, String> colImpValue;
  private TableColumn<ReciFatt, String> colStrcol;

  private ObservableList<ReciFatt> fattList;
  private URL                      mainCSS;
  private Label                    statusLabel;
  private Stage                    stage;

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    stage = primaryStage;
    tblview = new TableView<ReciFatt>();
    tblview.setPrefHeight(600);

    initTblCols();

    // Pulsanti di controllo
    Button btnAggiungi = new Button("Aggiungi");
    // btnAggiungi.setOnAction(e -> aggiungiRandomFattura());

    Button btnRimuovi = new Button("Rimuovi Casuale");
    // btnRimuovi.setOnAction(e -> rimuoviRandomFattura());

    Button btnModifica = new Button("Modifica Casuale");
    //    btnModifica.setOnAction(e -> modificaRandomFattura());

    Button btnAvviaAuto = new Button("Avvia Auto-Update");
    //    btnAvviaAuto.setOnAction(e -> avviaAggiornamentoAutomatico());

    Button btnStopAuto = new Button("Stop Auto-Update");
    //    btnStopAuto.setOnAction(e -> stopAggiornamentoAutomatico());

    Button btnSvuota = new Button("Svuota Lista");
    btnSvuota.setOnAction(e -> fattList.clear());

    // Status label
    statusLabel = new Label("Pronto. Elementi: 0");
    statusLabel.setStyle("-fx-font-weight: bold;");

    // Layout pulsanti
    HBox buttonBox = new HBox(10, btnAggiungi, btnRimuovi, btnModifica, btnAvviaAuto, btnStopAuto, btnSvuota);
    buttonBox.setPadding(new Insets(10));

    // Layout principale
    BorderPane root = new BorderPane();
    root.setCenter(tblview);
    root.setBottom(new BorderPane(buttonBox, statusLabel, null, null, null));
    BorderPane.setMargin(statusLabel, new Insets(5, 10, 5, 10));

    // Aggiungi alcuni dati iniziali
    loadTblValues();
    Scene scene = new Scene(root, 800, 500);
    URL url = getUrlCSS();
    scene.getStylesheets().add(url.toExternalForm());

    stage.setTitle("Scroll Table View overwrites rows Color");
    stage.setScene(scene);
    stage.show();
  }

  private void loadTblValues() {
    fattList = FXCollections.observableArrayList();
    for (int i = 0; i < 50; i++) {
      var reci = new ReciFatt();
      reci.randomize();
      fattList.add(reci);
    }
    tblview.getItems().clear();
    if (null == fattList || fattList.size() == 0)
      return;
    tblview.getItems().addAll(fattList);
  }

  private void initTblCols() {
    tblview.setRowFactory(tv -> new TableRow<ReciFatt>() {
      @Override
      protected void updateItem(ReciFatt item, boolean empty) {
        super.updateItem(item, empty);
        pseudoClassStateChanged(cssInError, false);
        pseudoClassStateChanged(cssNewPdf, false);
        pseudoClassStateChanged(cssNoExPdf, false);
        if (item != null && !empty) {
          if ( item.isInDb())
            pseudoClassStateChanged(cssNewPdf, true);
          else if ( item.isFileExist())
            pseudoClassStateChanged(cssNoExPdf, true);
        }
      }
    });

    tblview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    tblview.getColumns().clear();

    colId = new TableColumn<>("Id");
    colId.setCellValueFactory(param -> {
      var vv = param.getValue();
      var cel = new SimpleIntegerProperty();
      if (null == vv)
        return cel;
      Integer ii = vv.getId();
      cel.set(ii);
      return cel;
    });
    colId.setStyle(Consts.cssAlignR);
    tblview.getColumns().add(colId);

    colFileNam = new TableColumn<>("File");
    colFileNam.setCellValueFactory(param -> {
      String sz = param.getValue().getFileName();
      var cel = new SimpleStringProperty();
      cel.set(sz);
      return cel;
    });
    tblview.getColumns().add(colFileNam);

    colImpValue = new TableColumn<>("Importo");
    colImpValue.setCellValueFactory(param -> {
      Double dbl = param.getValue().getImpValue();
      var cel = new SimpleStringProperty();
      String sz = Utils.formatDouble(dbl);
      cel.set(sz);
      return cel;
    });
    colImpValue.setStyle(Consts.cssAlignR);
    tblview.getColumns().add(colImpValue);

    colStrcol = new TableColumn<>("Str.Value");
    colStrcol.setCellValueFactory(param -> {
      String sz = param.getValue().getStrCol();
      var cel = new SimpleStringProperty();
      cel.set(sz);
      return cel;
    });
    tblview.getColumns().add(colStrcol);
  }

  public URL getUrlCSS() {
    if (null != mainCSS)
      return mainCSS;

    String skinCss = "RecFatt.css";
    mainCSS = getClass().getResource(skinCss);
    if (null == mainCSS)
      mainCSS = getClass().getClassLoader().getResource(skinCss);
    return mainCSS;
  }

}
