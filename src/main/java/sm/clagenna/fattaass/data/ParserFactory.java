package sm.clagenna.fattaass.data;

import sm.clagenna.fattaass.enums.ETipoFatt;

public class ParserFactory {

  public ParserFactory() {
    // 
  }

  public static IParserFatture get(ETipoFatt tp, FattAassModel mod) {
    IParserFatture ret = null;
    switch (tp) {
      case Acqua:
        ret = new ParserH2OFattura(mod);
        break;
      case EnergiaElettrica:
        ret = new ParserEEFattura(mod);
        break;
      case GAS:
        ret = new ParserGASFattura(mod);
        break;
      default:
        break;
    }
    return ret;
  }
}
