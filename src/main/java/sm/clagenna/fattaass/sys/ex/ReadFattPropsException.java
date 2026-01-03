package sm.clagenna.fattaass.sys.ex;

public class ReadFattPropsException extends ReadFattException {

  /** serialVersionUID */
  private static final long serialVersionUID = 7288868203289764643L;

  public ReadFattPropsException() {
    //
  }

  public ReadFattPropsException(String p_msg) {
    super(p_msg);
  }

  public ReadFattPropsException(String p_msg, Throwable p_ex) {
    super(p_msg, p_ex);
  }

}
