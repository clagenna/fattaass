package sm.clagenna.fattaass.data.rec;

import java.time.LocalDateTime;

import lombok.Data;
import sm.clagenna.fattaass.data.IRecFattura;
import sm.clagenna.fattaass.enums.ETipoFatt;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

@Data
public class RecEEFattura implements IRecFattura {

  private int           idEEFattura;
  private int           idIntesta;
  private int           annoComp;
  private LocalDateTime dataEmiss;
  private int           fattNrAnno;
  private String        fattNrNumero;
  private LocalDateTime periodFattDtIniz;
  private LocalDateTime periodFattDtFine;
  private int           credPrecKwh;
  private int           credAttKwh;
  private double        addizFER;
  private double        impostaQuiet;
  private double        restitBollPrec;
  private double        totPagare;
  private String        nomeFile;

  public RecEEFattura() {
    //
  }

  @Override
  public int getIdFattura() {
    return idEEFattura;
  }
  
  @Override
  public void setIdFattura(int ii) {
    setIdEEFattura(ii);
  }

  @Override
  public ETipoFatt getTipoFattura() {
    return ETipoFatt.EnergiaElettrica;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("\t%-16s %d\n", "idEEFattura", idEEFattura));
    sb.append(String.format("\t%-16s %d\n", "idIntesta", idIntesta));
    sb.append(String.format("\t%-16s %d\n", "annoComp", annoComp));
    sb.append(String.format("\t%-16s %s\n", "dataEmiss", ParseData.formatDate(dataEmiss)));
    sb.append(String.format("\t%-16s %d\n", "fattNrAnno", fattNrAnno));
    sb.append(String.format("\t%-16s %s\n", "fattNrNumero", fattNrNumero));
    sb.append(String.format("\t%-16s %s\n", "periodFattDtIniz", ParseData.formatDate(periodFattDtIniz)));
    sb.append(String.format("\t%-16s %s\n", "ParseData", ParseData.formatDate(periodFattDtFine)));
    sb.append(String.format("\t%-16s %d\n", "credPrecKwh", credPrecKwh));
    sb.append(String.format("\t%-16s %d\n", "credAttKwh", credAttKwh));
    sb.append(String.format("\t%-16s %s\n", "addizFER", Utils.formatDouble(addizFER)));
    sb.append(String.format("\t%-16s %s\n", "impostaQuiet", Utils.formatDouble(impostaQuiet)));
    sb.append(String.format("\t%-16s %s\n", "restitBollPrec", Utils.formatDouble(restitBollPrec)));
    sb.append(String.format("\t%-16s %s\n", "TotPagare", Utils.formatDouble(totPagare)));
    sb.append(String.format("\t%-16s %s\n", "nomeFile", nomeFile));
    return sb.toString();
  }
}
