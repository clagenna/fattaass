package sm.clagenna.fattaass.enums;

import java.util.ArrayList;
import java.util.List;

public enum ETipoEEConsumo {
  EnergiaSpread1S("S1", "Spread Sc.1"), //
  EnergiaSpread2S("S2", "Spread Sc.2"), //
  EnergiaPUN("PU", "Ener. PUN"), //
  Energia1S("E1", "Ener. Scgl.1", "Corrispettivo Energia"), //
  Energia2S("E2", "Ener. Scgl.2", "Corrispettivo Energia"), //
  Energia3S("E3", "Ener. Scgl.3", "Corrispettivo Energia"), //
  PotenzaImpegnata("P", "Potenza imp"), //
  RaccRifiuti("R", "Rifiuti");

  private String       sigla;
  private List<String> dicitura;

  public String getSigla() {
    return sigla;
  }

  public String getDicitura() {
    return dicitura.get(0);
  }

  private ETipoEEConsumo(String p_si, String... p_dic) {
    sigla = p_si;
    dicitura = new ArrayList<String>();
    for (String sz : p_dic)
      dicitura.add(sz.toLowerCase());
  }

  public static ETipoEEConsumo parse(String p_dic, String p_sca) {
    ETipoEEConsumo ret = null;
    String sz = null;
    for (ETipoEEConsumo vv : ETipoEEConsumo.values()) {
      // if (p_dic.toLowerCase().contains(vv.dicitura.toLowerCase())) {
      if (vv.containDicitura(p_dic)) {
        ret = vv;
        if ( p_dic.contains("PUN") ) {
          ret = EnergiaPUN;
          break;
        }
        switch (vv) {
          case Energia1S:
            sz = p_sca != null ? p_sca.substring(0, 1) : "*";
            switch (sz) {
              case "1":
                ret = Energia1S;
                break;
              case "2":
                ret = Energia2S;
                break;
              case "3":
                ret = Energia3S;
                break;
            }
            return ret;
          case EnergiaSpread1S:
            sz = p_sca != null ? p_sca.substring(0, 1) : "*";
            switch (sz) {
              case "1":
                ret = EnergiaSpread1S;
                break;
              case "2":
                ret = EnergiaSpread2S;
                break;
            }
            return ret;
          default:
            break;
        }
        break;
      }
    }
    return ret;
  }

  private boolean containDicitura(String p_dic) {
    boolean bRet = false;
    if (null == p_dic || p_dic.length() == 0)
      return bRet;
    String remDic = p_dic.toLowerCase();
    for (String locDic : dicitura) {
      bRet = remDic.contains(locDic) || locDic.contains(remDic);
      if (bRet)
        break;
    }
    return bRet;
  }

}
