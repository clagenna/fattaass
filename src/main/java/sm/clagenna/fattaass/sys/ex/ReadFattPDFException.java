package sm.clagenna.fattaass.sys.ex;

public class ReadFattPDFException extends ReadFattException {

  /** serialVersionUID */
  private static final long serialVersionUID = -6487157978167415649L;

  public ReadFattPDFException() {
    //
  }

  public ReadFattPDFException(String p_msg) {
    super(p_msg);
  }

  public ReadFattPDFException(String p_msg, Throwable p_ex) {
    super(p_msg, p_ex);
  }

}
