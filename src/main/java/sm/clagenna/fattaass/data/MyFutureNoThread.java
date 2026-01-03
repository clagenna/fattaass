package sm.clagenna.fattaass.data;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MyFutureNoThread implements Future<FileFattura> {
  private FileFattura fattura;

  public MyFutureNoThread(FileFattura ff) {
    fattura = ff;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return true;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public FileFattura get() throws InterruptedException, ExecutionException {
    return fattura;
  }

  @Override
  public FileFattura get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return fattura;
  }

}
