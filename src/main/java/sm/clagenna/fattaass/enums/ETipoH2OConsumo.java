package sm.clagenna.fattaass.enums;

public enum ETipoH2OConsumo {
  Scagl1S("S1", "I scaglione"), //
  Scagl2S("S2", "II scaglione"), //
  Scagl3S("S3", "III scaglione"), //
  Scagl4S("S4", "IV scaglione"), //
  Scagl5S("S4", "V scaglione"), //

  TariffaAmb1S("TA1", "Tariffa Ambientale I Scaglione"), //
  TariffaAmb2S("TA2", "Tariffa Ambientale II Scaglione"), //
  TariffaAmb3S("TA3", "Tariffa Ambientale III Scaglione"), //
  TariffaAmb4S("TA4", "Tariffa Ambientale IV Scaglione"), //

  QuotaFissa("F", "Quota Fissa");

  private String sigla;
  private String dicitura;

  public String getSigla() {
    return sigla;
  }

  public String getDicitura() {
    return dicitura;
  }

  private ETipoH2OConsumo(String p_si, String p_dic) {
    sigla = p_si;
    dicitura = p_dic;
  }

  /**
   * Questa deve discernere le seguenti situazioni di causale
   *
   * <pre>
   * "I Scaglione"
   * "II Scaglione"
   * "III Scaglione"
   * "IV Scaglione"
   * "Tariffa Ambientale I Scaglione"
   * "Tariffa Ambientale II Scaglione"
   * "Tariffa Ambientale III Scaglione"
   * "Tariffa Ambientale IV Scaglione"
   * "Quota Fissa"
   * </pre>
   *
   * @param p_dic
   * @return
   */
  public static ETipoH2OConsumo parse(String p_dic) {
    ETipoH2OConsumo ret = null;
    String l_dic = p_dic.toLowerCase();

    if (l_dic.contains("tariffa ambientale ")) {
      if (l_dic.contains(" iv "))
        return TariffaAmb4S;
      if (l_dic.contains(" iii "))
        return TariffaAmb3S;
      if (l_dic.contains(" ii "))
        return TariffaAmb2S;
      if (l_dic.contains(" i "))
        return TariffaAmb1S;
    }
    if (l_dic.contains(" scaglione")) {
      if (l_dic.contains("iv "))
        return Scagl4S;
      if (l_dic.contains("iii "))
        return Scagl3S;
      if (l_dic.contains("ii "))
        return Scagl2S;
      if (l_dic.contains("i "))
        return Scagl1S;
    }
    for (ETipoH2OConsumo vv : ETipoH2OConsumo.values()) {
      if (p_dic.toLowerCase().contains(vv.dicitura.toLowerCase())) {
        ret = vv;
        break;
      }
    }
    return ret;
  }

}
