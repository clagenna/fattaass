package sm.clagenna.fattaass.sql;

import java.nio.file.Path;
import java.sql.SQLException;

import org.apache.logging.log4j.Logger;

import sm.clagenna.stdcla.pdf.IParseHtmlValues;
import sm.clagenna.stdcla.sql.DBConn;

public class GASxSQLite extends BaseSQLite implements ISql {

  @Override
  public void init(IParseHtmlValues p_fact, DBConn p_con, Path p_path) {
    // 

  }

  @Override
  public Logger getLog() {
    // 
    return null;
  }

  @Override
  public boolean fatturaExist() throws SQLException {
    // 
    return false;
  }

//  @Override
//  public boolean letturaExist() throws SQLException {
//    // 
//    return false;
//  }

//  @Override
//  public boolean consumoExist() throws SQLException {
//    // 
//    return false;
//  }

  @Override
  public void deleteFattura() throws SQLException {
    // 

  }

  @Override
  public void insertNewFattura() throws SQLException {
    // 

  }

  @Override
  public void insertNewLettura() throws SQLException {
    // 

  }

  @Override
  public void insertNewConsumo() throws SQLException {
    // 

  }

}
