package prova.concurr;

import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Callable;

import lombok.Getter;
import lombok.Setter;

public class FactorialTask implements Callable<BigInteger> {
  private static DateTimeFormatter s_fmt;

  @Getter @Setter
  private BigInteger from;
  @Getter @Setter
  private BigInteger to;
  @Getter @Setter
  private long       waitt;

  static {
    s_fmt = DateTimeFormatter.ofPattern("HH:mm:ss_SSS").withZone(ZoneId.systemDefault());
  }

  public FactorialTask(BigInteger pfrom, BigInteger pTo) {
    setFrom(pfrom);
    setTo(pTo);
  }

  @Override
  public BigInteger call() throws Exception {
    logMsg(String.format("call(%s)", toStringEx()));
    BigInteger ret = null;
    ret = calcFactOfSlice();
    return ret;
  }

  /**
   * Esegue il calcolo parziale del fattoriale che va da <code>from</code> a
   * <code>to</code>
   *
   * @return
   */
  public BigInteger calcFactOfSlice() throws Exception {
    String szThrName = Thread.currentThread().getName();
    String szThId = String.format("FactSlice[%s](%d,%d)", szThrName, from, to);

    logMsg(String.format("%s ------ START", szThId));
    final boolean throwExc = true;
    if (from.compareTo(to) >= 0) {
      if (throwExc) {
        String szErr = String.format("From(%d) >= To(%d)", from, to);
        InvalidFractValues exception = new InvalidFractValues(szErr);
        throw exception;
      }
      return to;
    }
    try {
      if (waitt > 1) {
        Random rnd = new Random(new Date().getTime());
        long wt = rnd.nextLong(1_000, waitt);
        Thread.sleep(wt);
      }
    } catch (InterruptedException e) {
      logMsg(String.format("%s ---- EXCEPTION !!! (ex=%s)", szThId, e.getMessage()));
    }
    BigInteger lv = BigInteger.ONE;
    for (BigInteger strt = from; strt.compareTo(to) <= 0; strt = strt.add(BigInteger.ONE))
      lv = lv.multiply(strt);
    logMsg(String.format("%s ---- END! %, d)", szThId, lv));
    return lv;
  }

  private String toStringEx() {
    String szThrName = Thread.currentThread().getName();
    String szRet = String.format("%-25s, %d-%s", szThrName, from, to);
    return szRet;
  }

  private void logMsg(String sz) {
    Instant tt = Instant.now();
    String sz2 = s_fmt.format(tt);
    System.out.println(String.format("%s - %s", sz2, sz));
  }

}
