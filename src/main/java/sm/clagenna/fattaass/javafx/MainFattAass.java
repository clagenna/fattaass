package sm.clagenna.fattaass.javafx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import sm.clagenna.fattaass.data.Consts;
import sm.clagenna.fattaass.data.FattAassModel;
import sm.clagenna.stdcla.javafx.IStartApp;
import sm.clagenna.stdcla.javafx.JFXUtils;
import sm.clagenna.stdcla.pdf.HtmlValue;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.Utils;

public class MainFattAass extends Application implements IStartApp, PropertyChangeListener {
  private static final Logger s_log = LogManager.getLogger(MainFattAass.class);
  @Getter
  private static MainFattAass inst;

  private String             skin;
  @Getter @Setter
  private Stage              primaryStage;
  @Getter @Setter
  private URL                mainCSS;
  @Getter @Setter
  private FattAassController controller;
  @Getter @Setter
  private FattAassModel      model;
  @Getter @Setter
  private AppProperties      props;
  private List<ResultView>   liResView;

  public static void main(String[] args) {
    Application.launch(args);
  }

  public MainFattAass() {
    inst = this;
  }

  @Override
  public void start(Stage p_primaryStage) throws Exception {
    System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    System.setProperty("org.slf4j.simpleLogger.log.org.mabb.fontverter.opentype.TtfInstructions", "warn");
    setPrimaryStage(p_primaryStage);
    MainFattAass.inst = this;
    // voglio interpretare '18.673' come un INTERO !
    HtmlValue.setDoubleLocale(Locale.ITALY);
    initApp(null);
    URL url = getClass().getResource(Consts.CSZ_FXMLNAME);
    if (url == null)
      url = getClass().getClassLoader().getResource(Consts.CSZ_FXMLNAME);
    if (url == null)
      throw new FileNotFoundException(String.format("Non trovo reource %s", Consts.CSZ_FXMLNAME));
    Parent radice = FXMLLoader.load(url);
    Scene scene = new Scene(radice, 725, 550);

    // <a target="_blank" href="https://icons8.com/icon/Qd0k8d5D0tSe/invoice">Invoice</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
    // vedi https://stackoverflow.com/questions/61531317/how-do-i-determine-the-correct-path-for-fxml-files-css-files-images-and-other
    URL icoURL = getClass().getResource(Consts.CSZ_MAIN_ICON);
    if (null == icoURL)
      icoURL = getClass().getClassLoader().getResource(Consts.CSZ_MAIN_ICON);
    if (null != icoURL) {
      Image ico = new Image(icoURL.toExternalForm());
      primaryStage.getIcons().add(ico);
    } else
      s_log.error("Non trovo icona {}", Consts.CSZ_MAIN_ICON);

    url = getUrlCSS();
    scene.getStylesheets().add(url.toExternalForm());

    primaryStage.setScene(scene);
    primaryStage.show();

  }

  @Override
  public void initApp(AppProperties p_props) {
    props = p_props;
    try {
      model = new FattAassModel();
      model.setGenRandomFatt(true);
      model.initApp(props);
      if (null == props)
        props = model.getProps();
      setSkin(props.getProperty(AppProperties.CSZ_PROP_SKIN));
      if (null == skin)
        skin = Consts.CSZ_SKINDEFAULT;
      JFXUtils.readPosStage(primaryStage, props, Consts.CSZ_MAIN_ICON);
    } catch (Exception l_e) {
      s_log.error("Errore in main initApp: {}", l_e.getMessage(), l_e);
      System.exit(1957);
    }
  }

  public URL getUrlCSS() {
    if (null != mainCSS)
      return mainCSS;
    if (null == skin)
      skin = Consts.CSZ_SKINDEFAULT;
    String skinCss = String.format("%s.css", skin);
    mainCSS = getClass().getResource(skinCss);
    if (null == mainCSS)
      mainCSS = getClass().getClassLoader().getResource(skinCss);
    return mainCSS;
  }

  public int addResView(ResultView resultView) {
    if (null == liResView)
      liResView = new ArrayList<ResultView>();
    liResView.add(resultView);
    return liResView.indexOf(resultView);
  }

  public void removeResView(ResultView resultView) {
    if (liResView == null || liResView.size() == 0)
      return;
    if (liResView.contains(resultView))
      liResView.remove(resultView);
  }

  public void setSkin(String skinName) {
    if ( !Utils.isChanged(skin, skinName))
      return;
    skin = skinName;
    // props.setProperty(skinName, 0);
    mainCSS = null;
    props.setProperty(AppProperties.CSZ_PROP_SKIN, skin);
    /* URL url = */ getUrlCSS();
    controller.changeSkin();
  }

  @Override
  public void changeSkin() {
    // nothing to do

  }

  @Override
  public void stop() throws Exception {
    model.firePropertyChange(Consts.EVT_CloseApp, null, props);
    props.salvaSuProperties();
    model.closeApp(props);
    super.stop();
  }

  @Override
  public void closeApp(AppProperties prop) {
    // JFXUtils.savePosStage(primaryStage, prop, Consts.CSZ_MAIN_ICON);
  }

  public Optional<ButtonType> messageDialog(AlertType typ, String p_msg) {
    return messageDialog(typ, p_msg, ButtonType.CLOSE);
  }

  /**
   * per abilitare il display HTML ho messo un WebView embedded nel alert pero'
   * ho dovuto specificare <b>javafx.media,javafx.web</b>
   *
   * <pre>
   * --module-path "C:/Program Files/Java/javafx-sdk-20.0.2/lib"
   * --add-modules=javafx.swing,javafx.graphics,javafx.fxml,javafx.media,javafx.web
   * </pre>
   *
   * @param typ
   *          Il tipo di {@link AlertType}
   * @param p_msg
   *          Il messaggio (anche HTML) da emettere
   * @param bt
   *          Il tipo di {@link ButtonType}
   * @return
   */
  public Optional<ButtonType> messageDialog(AlertType typ, String p_msg, ButtonType bt) {
    Alert alert = new Alert(typ);
    alert.setResizable(true);
    Scene scene = primaryStage.getScene();
    double posx = scene.getWindow().getX();
    double posy = scene.getWindow().getY();
    double widt = scene.getWidth();
    double px = posx + widt / 2 - 366;
    double py = posy + 50;
    alert.setX(px);
    alert.setY(py);
    alert.setWidth(400);

    switch (typ) {
      case CONFIRMATION:
        alert.setTitle("Verifica");
        alert.setHeaderText("Scegli cosa fare");
        break;
      case INFORMATION:
        alert.setTitle("Informa");
        alert.setHeaderText("Comunicazione");
        break;

      case WARNING:
        alert.setTitle("Attenzione");
        alert.setHeaderText("Occhio !");
        break;

      case ERROR:
        alert.setTitle("Errore !");
        alert.setHeaderText("Ahi ! Ahi !");
        break;

      default:
        break;
    }
    //    alert.setContentText(p_msg);
    WebView webView = new WebView();
    webView.getEngine().loadContent(p_msg);
    webView.setPrefSize(300, 60);
    alert.getDialogPane().setContent(webView);
    Optional<ButtonType> btret = alert.showAndWait();
    return btret;
  }

  @SuppressWarnings("unused")
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    var sz = evt.getPropertyName();
    var val = evt.getNewValue();
    double currProgressNo = 0;
    switch (sz) {

      case Consts.EVT_CloseApp:
        closeApp(props);
        break;
    }
  }

}
