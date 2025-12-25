package sm.clagenna.fattaass.data.rec;

import java.time.LocalDateTime;

import lombok.Data;
import sm.clagenna.fattaass.enums.ETipoH2OConsumo;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

@Data
public class RecH2Oconsumo {
  private int             idConsumo;
  private int             idH2OFattura;
  private ETipoH2OConsumo tipoSpesa;
  private LocalDateTime   dtIniz;
  private LocalDateTime   dtFine;
  private boolean         stimato;
  private double          prezzoUnit;
  private double          quantita;
  private double          importo;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("\t%-16s %d\n", "idConsumo", idConsumo));
    sb.append(String.format("\t%-16s %d\n", "idH2OFattura", idH2OFattura));
    sb.append(String.format("\t%-16s %s\n", "tipoSpesa",( null != tipoSpesa ? tipoSpesa.getSigla() : "null")));
    sb.append(String.format("\t%-16s %s\n", "dtIniz", ParseData.formatDate(dtIniz)));
    sb.append(String.format("\t%-16s %s\n", "dtFine", ParseData.formatDate(dtFine)));
    sb.append(String.format("\t%-16s %s\n", "stimato", (stimato ? "1" : "0")));
    sb.append(String.format("\t%-16s %s\n", "prezzoUnit", Utils.formatDouble(prezzoUnit)));
    sb.append(String.format("\t%-16s %s\n", "quantita", Utils.formatDouble(quantita)));
    sb.append(String.format("\t%-16s %s\n", "importo", Utils.formatDouble(importo)));
    return sb.toString();
  }
}
