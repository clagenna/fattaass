package sm.clagenna.fattaass.data.rec;

import java.time.LocalDateTime;

import lombok.Data;
import sm.clagenna.fattaass.enums.ETipoGASConsumo;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

@Data
public class RecGasConsumo {
  private int             idConsumo;
  private int             idGASFattura;
  private ETipoGASConsumo tipoSpesa;
  private LocalDateTime   dtIniz;
  private LocalDateTime   dtFine;
  private boolean         stimato;
  private double          prezzoUnit;
  private double          quantita;
  private double          importo;

  public RecGasConsumo() {
    //
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("\t%-16s %d\n", "idConsumo", idConsumo));
    sb.append(String.format("\t%-16s %d\n", "idGASFattura", idGASFattura));
    sb.append(String.format("\t%-16s %s\n", "tipoSpesa", tipoSpesa));
    sb.append(String.format("\t%-16s %s\n", "dtIniz", ParseData.formatDate(dtIniz)));
    sb.append(String.format("\t%-16s %s\n", "dtFine", ParseData.formatDate(dtFine)));
    sb.append(String.format("\t%-16s %s\n", "stimato", Boolean.valueOf(stimato).toString()));
    sb.append(String.format("\t%-16s %s\n", "prezzoUnit", Utils.formatDouble(prezzoUnit)));
    sb.append(String.format("\t%-16s %s\n", "quantita", Utils.formatDouble(quantita)));
    sb.append(String.format("\t%-16s %s\n", "importo", Utils.formatDouble(importo)));
    return sb.toString();
  }
}
