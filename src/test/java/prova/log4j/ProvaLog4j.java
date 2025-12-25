package prova.log4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class ProvaLog4j {
  private static final Logger s_log = LogManager.getLogger(ProvaLog4j.class);

  @Test
  public void doIt() {
    s_log.info("Vedi se sdoppia");
  }
}
