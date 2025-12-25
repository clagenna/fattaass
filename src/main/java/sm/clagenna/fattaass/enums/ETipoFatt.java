package sm.clagenna.fattaass.enums;

public enum ETipoFatt {
  GAS("GAS"), //
  EnergiaElettrica("EE"), //
  Acqua("H2O"),
  // Analisi("SANG")
  ;

  private String titolo;

  private ETipoFatt(String tit) {
    titolo = tit;
  }

  public String getTitolo() {
    return titolo;
  }

  /**
   * Torna il ETipoFatt in base alla stringa fornita che deve essere uguale ad
   * un dei {@link #getTitolo()}
   * 
   * @param p_sz
   * @return
   */
  public static ETipoFatt parse(String p_sz) {
    ETipoFatt ret = null;
    if (p_sz == null || p_sz.length() < 2)
      return ret;
    for (ETipoFatt t : ETipoFatt.values()) {
      if (t.titolo.equals(p_sz)) {
        ret = t;
        break;
      }
    }
    return ret;
  }
}
