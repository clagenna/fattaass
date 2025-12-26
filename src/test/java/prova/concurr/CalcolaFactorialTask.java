package prova.concurr;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import lombok.Getter;
import lombok.Setter;
import sm.clagenna.stdcla.utils.concurrency.CallableFutureService;
import sm.clagenna.stdcla.utils.sys.TimerMeter;

/**
 * Calcolo di fattoriale a <b>fette</b><br/>
 * ogni istanza di questa classe calcola il fattoriale che va dal valore
 * <code>from</code> a <code>to</code>
 *
 */
public class CalcolaFactorialTask {
  private static DateTimeFormatter s_fmt;
  private static DecimalFormat     ifmt;

  @Getter @Setter
  private int                        waitt;
  @Getter @Setter
  private BigInteger                 factNo;
  @Getter @Setter
  private BigInteger                 factResult;
  /** qta di fette di elaborazione di subFattoriale */
  @Getter @Setter
  private int                        qtaSlices;
  private boolean                    bConErr;
  private CallableFutureService      callblesrv;
  private List<BigInteger>           liSlices;
  private List<Callable<BigInteger>> liTasks;
  private List<Future<BigInteger>>   liFutureBigInt;

  static {
    s_fmt = DateTimeFormatter.ofPattern("HH:mm:ss_SSS").withZone(ZoneId.systemDefault());
    ifmt = new DecimalFormat("#,###");
  }

  public CalcolaFactorialTask() {
    init();
  }

  public static void main(String[] args) {
    long no = 2_765;
    CalcolaFactorialTask tsk = new CalcolaFactorialTask();
    BigInteger res = tsk.calcolaFattoriale(no);
    String sz = ifmt.format(res);
    System.out.printf("\n%d !  = %s\n", no, sz);
  }

  private void init() {
    setQtaSlices(10);
    callblesrv = new CallableFutureService(qtaSlices);
    bConErr = false;
    waitt = 6000;
  }

  public BigInteger calcolaFattoriale(long numFact) {
    return calcolaFattoriale(BigInteger.valueOf(numFact));
  }

  public BigInteger calcolaFattoriale(BigInteger numFact) {
    BigInteger fatt = null;
    liSlices = calcSlices(numFact);
    liTasks = creaFactTasks();
    calcAMano(numFact);
    try {
      logMsg("inizio lavori sotto banco");
      liFutureBigInt = callblesrv.submitTasks(liTasks);
      aspettaLaFine();
      logMsg("Fine dei lavori sotto banco");
      fatt = moltiplicaFutures();
    } finally {
      callblesrv.shutdown();
    }
    return fatt;
  }

  private void calcAMano(BigInteger numFact) {
    TimerMeter tm = new TimerMeter("Calc a mano");
    setWaitt(0);
    BigInteger res = BigInteger.ONE;
    try {
      for (Callable<BigInteger> tsk : liTasks) {
        FactorialTask fac = (FactorialTask) tsk;
        BigInteger ff = fac.calcFactOfSlice();
        res = res.multiply(ff);
      }
      String sz = ifmt.format(res);
      System.out.printf("%s\n%d !  = %s\n", tm.stop(), numFact, sz);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private List<BigInteger> calcSlices(BigInteger numFact) {
    List<BigInteger> liRet = new ArrayList<BigInteger>();
    BigInteger biNext = BigInteger.ZERO;
    BigInteger slice = numFact.divide(BigInteger.valueOf(qtaSlices));
    if (slice.floatValue() <= 1)
      slice = BigInteger.valueOf(2L);
    // senza l'ONE iniziale !
    while (true) {
      biNext = biNext.add(slice);
      if (slice.floatValue() <= 1 || biNext.compareTo(numFact) >= 0)
        break;
      liRet.add(biNext);
    }
    if ( !liRet.contains(numFact))
      liRet.add(numFact);
    return liRet;
  }

  private List<Callable<BigInteger>> creaFactTasks() {
    liTasks = new ArrayList<>();
    BigInteger biPrev = BigInteger.ONE;
    int k = 1;
    for (BigInteger biCurr : liSlices) {
      FactorialTask tsk = new FactorialTask(biPrev, biCurr);
      if (bConErr) {
        // introduco un errore
        if (++k >= 7) {
          FactorialTask tskErr = new FactorialTask(BigInteger.valueOf(23), BigInteger.valueOf(22));
          liTasks.add(tskErr);
        }
      }
      biPrev = biCurr;
      tsk.setWaitt(getWaitt());
      liTasks.add(tsk);
    }
    return liTasks;
  }

  private void aspettaLaFine() {
    boolean bAllDone = true;
    TimerMeter tt = new TimerMeter("Wait end Threads");
    for (;;) {
      bAllDone = true;
      for (Future<BigInteger> bi : liFutureBigInt) {
        bAllDone &= bi.isDone();
      }
      if (bAllDone)
        break;
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      logMsg(tt.stop());
    }
  }

  private BigInteger moltiplicaFutures() {
    BigInteger lret = BigInteger.ONE;
    try {
      for (Future<BigInteger> fl : liFutureBigInt) {
        if (fl.state() != Future.State.SUCCESS)
          logMsg(String.format("FactTask.productFutures(%s)", fl.toString()));
        // se è avvenuto un exception, get() riproduce l'exception !
        BigInteger val = fl.get();
        lret = lret.multiply(val);
      }
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      lret = null;
    }
    return lret;
  }

  private void logMsg(String sz) {
    Instant tt = Instant.now();
    String sz2 = s_fmt.format(tt);
    System.out.println(String.format("%s - %s", sz2, sz));
  }

}
