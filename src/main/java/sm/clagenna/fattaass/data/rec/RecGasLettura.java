package sm.clagenna.fattaass.data.rec;

import java.time.LocalDateTime;

import lombok.Data;
import sm.clagenna.fattaass.enums.ETipoLettProvvenienza;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

@Data
public class RecGasLettura {
  private int                   idLettura;
  private int                   idGASFattura;
  private int                   lettQtaMc;
  private LocalDateTime         lettData;
  private ETipoLettProvvenienza tipoLett;
  private String                matricola;
  private double                coeffC;
  private double                consumo;

  public RecGasLettura() {
    //
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("\t%-16s %d\n", "idLettura", idLettura));
    sb.append(String.format("\t%-16s %d\n", "idGASFattura", idGASFattura));
    sb.append(String.format("\t%-16s %d\n", "lettQtaMc", lettQtaMc));
    sb.append(String.format("\t%-16s %s\n", "lettData", ParseData.formatDate(lettData)));
    sb.append(String.format("\t%-16s %s\n", "tipoLett", (null != tipoLett ? tipoLett.getSigla() : "??")));
    sb.append(String.format("\t%-16s %s\n", "Matricola", matricola));
    sb.append(String.format("\t%-16s %s\n", "coeffC", Utils.formatDouble(coeffC)));
    sb.append(String.format("\t%-16s %s\n", "consumo", Utils.formatDouble(consumo)));
    return sb.toString();
  }
}
