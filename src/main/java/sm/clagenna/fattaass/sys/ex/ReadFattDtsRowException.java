package sm.clagenna.fattaass.sys.ex;

public class ReadFattDtsRowException extends ReadFattException {

  /** serialVersionUID */
  private static final long serialVersionUID = -1737808932237384812L;

  public ReadFattDtsRowException() {
    //
  }

  public ReadFattDtsRowException(String p_msg) {
    super(p_msg);
  }

  public ReadFattDtsRowException(String p_msg, Throwable p_ex) {
    super(p_msg, p_ex);
  }

}
