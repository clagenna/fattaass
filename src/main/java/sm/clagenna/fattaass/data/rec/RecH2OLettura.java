package sm.clagenna.fattaass.data.rec;

import java.time.LocalDateTime;

import lombok.Data;
import sm.clagenna.fattaass.enums.ETipoLettProvvenienza;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

@Data
public class RecH2OLettura {
  private int                   idLettura;
  private int                   idH2OFattura;
  private int                   lettQtaMc;
  private LocalDateTime         LettData;
  private ETipoLettProvvenienza TipoLett;
  private String                matricola;
  private double                coeffK;
  private double                consumo;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("\t%-16s %d\n", "idLettura", idLettura));
    sb.append(String.format("\t%-16s %d\n", "idH2OFattura", idH2OFattura));
    sb.append(String.format("\t%-16s %d\n", "lettQtaMc", lettQtaMc));
    sb.append(String.format("\t%-16s %s\n", "LettData", ParseData.formatDate(LettData)));
    sb.append(String.format("\t%-16s %s\n", "TipoLett", TipoLett.getSigla()));
    sb.append(String.format("\t%-16s %s\n", "matricola", matricola));
    sb.append(String.format("\t%-16s %s\n", "coeffK", Utils.formatDouble(coeffK)));
    sb.append(String.format("\t%-16s %s\n", "Consumo", Utils.formatDouble(consumo)));
    return sb.toString();
  }
}
