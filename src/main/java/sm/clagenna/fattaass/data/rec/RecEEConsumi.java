package sm.clagenna.fattaass.data.rec;

import java.time.LocalDateTime;

import lombok.Data;
import sm.clagenna.fattaass.enums.ETipoEEConsumo;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

@Data
public class RecEEConsumi {
  private int            idEEFattura;
  private ETipoEEConsumo tipoSpesa;  // (Energia 1°,2°,3°, Potenza Impegnata, rifiuti)
  private LocalDateTime  dtIniz;
  private LocalDateTime  dtFine;
  private boolean        stimato;
  private double         prezzoUnit;
  private double         quantita;
  private double         importo;

  public RecEEConsumi() {
    //
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("\t%-16s %d\n", "idEEFattura", idEEFattura));
    sb.append(String.format("\t%-16s %s\n", "Tipo. Consumo", null != tipoSpesa ? tipoSpesa.toString() : "??"));
    sb.append(String.format("\t%-16s %s\n", "dt. iniz.", ParseData.formatDate(dtIniz)));
    sb.append(String.format("\t%-16s %s\n", "dt. fine", ParseData.formatDate(dtFine)));
    sb.append(String.format("\t%-16s %s\n", "Stima", stimato ? "Stimato" : "Reale"));
    sb.append(String.format("\t%-16s %s\n", "Prezzo Un.", Utils.formatDouble(prezzoUnit)));
    sb.append(String.format("\t%-16s %s\n", "Quantita.", Utils.formatDouble(quantita)));
    sb.append(String.format("\t%-16s %s\n", "Importo.", Utils.formatDouble(importo)));
    return sb.toString();
  }

}
