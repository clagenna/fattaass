package sm.clagenna.fattaass.data;

import sm.clagenna.fattaass.enums.ETipoFatt;

public interface IRecFattura {
  int getIdFattura();

  void setIdFattura(int ii);

  ETipoFatt getTipoFattura();

  String getNomeFile();
}
