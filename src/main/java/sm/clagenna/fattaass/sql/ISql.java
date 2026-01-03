package sm.clagenna.fattaass.sql;

import java.io.Closeable;
import java.sql.SQLException;

import org.apache.logging.log4j.Logger;

import sm.clagenna.fattaass.data.FattAassModel;
import sm.clagenna.fattaass.data.IParserFatture;
import sm.clagenna.fattaass.data.RecIntesta;
import sm.clagenna.fattaass.enums.ETipoFatt;

public interface ISql extends Closeable {

  void init(IParserFatture p_fact, FattAassModel p_con);

  Logger getLog();

  void setThreadName(String thid);

  void setTipoFattura(ETipoFatt p_tipoFatt);

  void setParsePdf(IParserFatture prsFatt);

  void setModel(FattAassModel mod);

  void setRecIntesta(RecIntesta reci);

  boolean fatturaExist() throws SQLException;

  void deleteFattura() throws SQLException;

  void insertNewFattura() throws SQLException;

  //  boolean letturaExist() throws SQLException;

  void insertNewLettura() throws SQLException;

  //  boolean consumoExist() throws SQLException;

  void insertNewConsumo() throws SQLException;

}
