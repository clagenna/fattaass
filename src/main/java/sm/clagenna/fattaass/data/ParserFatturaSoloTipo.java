package sm.clagenna.fattaass.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParserFatturaSoloTipo extends ParserFattura {

  private static final Logger s_log = LogManager.getLogger(ParserFatturaSoloTipo.class);

  public ParserFatturaSoloTipo(FattAassModel p_mod) {
    super(p_mod);
    setTipoFattura(null);
  }

  public ParserFatturaSoloTipo() {
    setTipoFattura(null);
  }

  @Override
  public Logger getLog() {
    return s_log;
  }

  @Override
  public IRecFattura getFattura() {
    return null;
  }

  public void evidenziaTokens(boolean bv) {
    super.evidenziaTokens = bv;
  }

}
