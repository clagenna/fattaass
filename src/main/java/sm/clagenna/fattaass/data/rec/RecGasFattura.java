package sm.clagenna.fattaass.data.rec;

import java.time.LocalDateTime;

import lombok.Data;
import sm.clagenna.fattaass.data.IRecFattura;
import sm.clagenna.fattaass.enums.ETipoFatt;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

@Data
public class RecGasFattura implements IRecFattura {
  private int           idGASFattura;
  private int           idIntesta;
  private int           annoComp;
  private LocalDateTime dataEmiss;
  private int           fattNrAnno;
  private String        fattNrNumero;
  private LocalDateTime periodFattDtIniz;
  private LocalDateTime periodFattDtFine;
  private LocalDateTime periodEffDtIniz;
  private LocalDateTime periodEffDtFine;
  private LocalDateTime periodAccontoDtIniz;
  private LocalDateTime periodAccontoDtFine;
  private double        rimborsoPrec;
  private double        misureStraord;
  private double        addizFER;
  private double        impostaQuiet;
  private double        totPagare;
  private String        nomeFile;

  public RecGasFattura() {
    //
  }

  @Override
  public int getIdFattura() {
    return idGASFattura;
  }

  @Override
  public void setIdFattura(int ii) {
    idGASFattura = ii;
  }

  @Override
  public ETipoFatt getTipoFattura() {
    return ETipoFatt.GAS;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("\t%-20s %d\n", "idGASFattura", idGASFattura));
    sb.append(String.format("\t%-20s %d\n", "idIntesta", idIntesta));
    sb.append(String.format("\t%-20s %d\n", "annoComp", annoComp));
    sb.append(String.format("\t%-20s %s\n", "DataEmiss", ParseData.formatDate(dataEmiss)));
    sb.append(String.format("\t%-20s %d\n", "fattNrAnno", fattNrAnno));
    sb.append(String.format("\t%-20s %s\n", "fattNrNumero", fattNrNumero));
    sb.append(String.format("\t%-20s %s\n", "periodFattDtIniz", ParseData.formatDate(periodFattDtIniz)));
    sb.append(String.format("\t%-20s %s\n", "periodFattDtFine", ParseData.formatDate(periodFattDtFine)));
    sb.append(String.format("\t%-20s %s\n", "periodEffDtIniz", ParseData.formatDate(periodEffDtIniz)));
    sb.append(String.format("\t%-20s %s\n", "periodEffDtFine", ParseData.formatDate(periodEffDtFine)));
    sb.append(String.format("\t%-20s %s\n", "periodAccontoDtIniz", ParseData.formatDate(periodAccontoDtIniz)));
    sb.append(String.format("\t%-20s %s\n", "periodAccontoDtFine", ParseData.formatDate(periodAccontoDtFine)));
    // sb.append(String.format("\t%-20s %s\n", "accontoBollPrec", Utils.formatDouble(accontoBollPrec)));
    sb.append(String.format("\t%-20s %s\n", "addizFER", Utils.formatDouble(addizFER)));
    sb.append(String.format("\t%-20s %s\n", "Rimb.Prec", Utils.formatDouble(rimborsoPrec)));
    // sb.append(String.format("\t%-20s %s\n", "impostaQuiet", Utils.formatDouble(impostaQuiet)));
    sb.append(String.format("\t%-20s %s\n", "TotPagare", Utils.formatDouble(totPagare)));
    sb.append(String.format("\t%-20s %s\n", "nomeFile", nomeFile));
    return sb.toString();
  }

}
