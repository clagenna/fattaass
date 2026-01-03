package sm.clagenna.fattaass.enums;

public enum ETipoGASConsumo {
  MatPrima1S("G1", "Gas I scaglione"), //
  MatPrima2S("G2", "Gas II scaglione"), //
  MatPrima3S("G3", "Gas III scaglione"), //
  MatPrima4S("G4", "Gas IV scaglione"), //
  EnergiaPUN("PU", "Energia PUN"), //
  EnergiaSpread1S("S1", "Spread I scaglione"), //
  EnergiaSpread2S("S2", "Spread II scaglione"), //
  EnergiaSpread3S("S3", "Spread III scaglione"), //
  QuotaFissa("F", "Quota Fissa"), //
  // dicitura: "GAS Art.5 D.D. n.93/2023" 
  RimbArt_5_DD("R5DD", "Art.5");

  private String sigla;
  private String dicitura;

  public String getSigla() {
    return sigla;
  }

  public String getDicitura() {
    return dicitura;
  }

  private ETipoGASConsumo(String p_si, String p_dic) {
    sigla = p_si;
    dicitura = p_dic;
  }

  public static ETipoGASConsumo parse(String p_dic) {
    ETipoGASConsumo ret = null;
    if (p_dic.toLowerCase().contains("spread")) {
      if (p_dic.contains(" I "))
        return EnergiaSpread1S;
      if (p_dic.contains(" II "))
        return EnergiaSpread2S;
      if (p_dic.contains(" III "))
        return EnergiaSpread3S;
    }
    if (p_dic.contains(" I "))
      return MatPrima1S;
    if (p_dic.contains(" II "))
      return MatPrima2S;
    if (p_dic.contains(" III "))
      return MatPrima3S;
    if (p_dic.contains(" IV "))
      return MatPrima4S;
    for (ETipoGASConsumo vv : ETipoGASConsumo.values()) {
      if (p_dic.toLowerCase().contains(vv.dicitura.toLowerCase())) {
        ret = vv;
        break;
      }
    }
    return ret;
  }

}
