package sm.clagenna.fattaass.sys.ex;

public class ReadFattSQLException extends ReadFattException {

  /** serialVersionUID */
  private static final long serialVersionUID = -6487157978167415649L;

  public ReadFattSQLException() {
    //
  }

  public ReadFattSQLException(String p_msg) {
    super(p_msg);
  }

  public ReadFattSQLException(String p_msg, Throwable p_ex) {
    super(p_msg, p_ex);
  }

}
