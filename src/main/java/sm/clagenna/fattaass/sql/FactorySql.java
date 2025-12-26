package sm.clagenna.fattaass.sql;

import sm.clagenna.fattaass.enums.ETipoFatt;
import sm.clagenna.stdcla.sql.EServerId;

public class FactorySql {

  public FactorySql() {
    //
  }

  public static ISql getFatturaInserter(ETipoFatt ptp, EServerId ids) {
    ISql ret = null;
    switch (ids) {
      case HSqlDB:
        break;
        
      case SQLite:
      case SQLite3:
        switch (ptp) {
          case Acqua:
            ret = new H2OxSQLite();
            break;
          case EnergiaElettrica:
            ret = new EExSQLite();
            break;
          case GAS:
            ret = new GASxSQLite();
            break;
          default:
            break;
        }
        break;
        
      case SqlServer:
        switch (ptp) {
          case Acqua:
            ret = new H2OxSqlServ();
            break;
          case EnergiaElettrica:
            ret = new EExSqlServ();
            break;
          case GAS:
            ret = new GASxSqlServ();
            break;
          default:
            break;
        }
        break;
        
      default:
        break;

    }
    //EServerId tpServ = DBCo
    return ret;
  }

}
