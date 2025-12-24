package sm.clagenna.fattaass.sys.ex;

public class ReadFattException extends Exception {

  /** serialVersionUID long */
  private static final long serialVersionUID = 7534038539443135465L;

  public ReadFattException() {
    //
  }

  public ReadFattException(String p_szMsg) {
    super(p_szMsg);
  }

  public ReadFattException(String p_msg, Throwable p_ex) {
    super(p_msg, p_ex);
  }

}
