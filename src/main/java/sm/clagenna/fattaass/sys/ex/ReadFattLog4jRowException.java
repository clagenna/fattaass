package sm.clagenna.fattaass.sys.ex;

public class ReadFattLog4jRowException extends ReadFattException {

  /** long */
  private static final long serialVersionUID = 2271943661219302651L;

  public ReadFattLog4jRowException() {
    //
  }

  public ReadFattLog4jRowException(String p_msg) {
    super(p_msg);
  }

  public ReadFattLog4jRowException(String p_msg, Throwable p_ex) {
    super(p_msg, p_ex);
  }

}
