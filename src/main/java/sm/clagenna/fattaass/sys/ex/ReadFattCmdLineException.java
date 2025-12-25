package sm.clagenna.fattaass.sys.ex;

public class ReadFattCmdLineException extends ReadFattException {

  /** serialVersionUID */
  private static final long serialVersionUID = 9030097627176336515L;

  public ReadFattCmdLineException() {
    //
  }

  public ReadFattCmdLineException(String p_msg) {
    super(p_msg);
  }

  public ReadFattCmdLineException(String p_msg, Throwable p_ex) {
    super(p_msg, p_ex);
  }

}
