package sm.clagenna.fattaass.enums;

public enum ETipoLettProvvenienza {
  Reale("real", "reale"), //
  Aziendale("azie", "aziend"), //
  Effettiva("eff", "effett"), //
  Autolettura("auto", "auto"), //
  Stimata("stim", "stima"), //
  Totale("tot", "total"); //

  private String sigla;
  private String dicitura;

  private ETipoLettProvvenienza(String p_sig, String p_dicit) {
    sigla = p_sig;
    dicitura = p_dicit;
  }

  public String getSigla() {
    return sigla;
  }

  public static ETipoLettProvvenienza parse(String p_dici) {
    ETipoLettProvvenienza ret = null;
    for (ETipoLettProvvenienza val : ETipoLettProvvenienza.values()) {
      if (p_dici.toLowerCase().contains(val.dicitura.toLowerCase())) {
        ret = val;
        break;
      }
    }
    return ret;
  }

}
