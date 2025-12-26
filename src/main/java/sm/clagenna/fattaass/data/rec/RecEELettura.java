package sm.clagenna.fattaass.data.rec;

import java.time.LocalDateTime;

import lombok.Data;
import sm.clagenna.fattaass.enums.ETipoLettProvvenienza;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

@Data
public class RecEELettura {
  private int                   idEEFattura;
  private LocalDateTime         dtLettPrec;
  private int                   lettPrec;
  private ETipoLettProvvenienza tipoLettura;
  private LocalDateTime         dtLettAttuale;
  private int                   lettAttuale;
  private double                consumo;
  private double                coeffK;

  public RecEELettura() {
    //
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("\t%-16s %d\n", "idEEFattura", idEEFattura));
    sb.append(String.format("\t%-16s %s\n", "dt. lett. prec", ParseData.formatDate(dtLettPrec)));
    sb.append(String.format("\t%-16s %d\n", "Lett. prec", lettPrec));
    sb.append(String.format("\t%-16s %s\n", "proveninza", tipoLettura != null ? tipoLettura.toString() : "??"));
    sb.append(String.format("\t%-16s %s\n", "dt. lett. attu.", ParseData.formatDate(dtLettAttuale)));
    sb.append(String.format("\t%-16s %d\n", "Lett. attu.", lettAttuale));
    sb.append(String.format("\t%-16s %s\n", "consumo", Utils.formatDouble(consumo)));
    sb.append(String.format("\t%-16s %s\n", "Coeff. K", Utils.formatDouble(coeffK)));
    return sb.toString();
  }
}
