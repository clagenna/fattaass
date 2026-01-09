package sm.clagenna.fattaass.data;

public final class Consts {

  public static final String EVT_SETLASTDIR        = "SetLastDir";
  public static final String EVT_CHANGE_INTESTA    = "ChangeIntesta";
  // public static final String EVT_SIZEDTS           = "Evento1";
  public static final String EVT_File00Start       = "FileListStart";
  public static final String EVT_File01ListDaDir   = "FileListDaDir";
  public static final String EVT_File02ListDaPDF   = "FileListDaPDF";
  public static final String EVT_File03NameChanged = "FileNameChanged";
  public static final String EVT_File04ListInDB    = "FileListInDB";
  public static final String EVT_File05ListReady   = "FileListReady";
  public static final String EVT_FattToInsert      = "FattToInsert";
  public static final String EVT_FattInDbInserted  = "FattInDbInserted";
  public static final String EVT_StartBckProcess   = "StartBckGround";
  public static final String EVT_EndBckProcess     = "EndBckGround";
  public static final String EVT_ENDALLPROCESS     = "EndOfAll";
  public static final String EVT_CloseApp          = "CloseApp";

  public static final String cssAlignR          = "-fx-alignment: center-right;";
  public static final String cssAlignL          = "-fx-alignment: center-left;";
  public static final String cssClass_OldPdf    = "oldPdf";
  public static final String cssClass_NewPdf    = "newPdf";
  public static final String cssClass_NoExPdf   = "noExPdf";
  public static final String cssClass_InError   = "inError";
  public static final String cssClass_NewPdfSel = "newPdfselPdf";

  public static final String PROP_LastIntesta = "idIntesta";
  public static final String PROP_QtaThreads  = "sys.qtaThreads";

  public static final String CSZ_MAIN_PROPS = "FattAass.properties";
  // public static final String CSZ_MAIN_ICON       = "sm.clagenna.fattaass.javafx.FattAass-100.png";
  public static final String CSZ_MAIN_ICON       = "FattAass-100.png";
  public static final String CSZ_REVIEW_ICON     = "ResView.png";
  public static final String CSZ_FXMLNAME        = "FattAassJavaFX.fxml";
  public static final String CSZ_FXMLIntesta     = "Intesta.fxml";
  public static final String CSZ_SKINDEFAULT     = "FattAass";
  public static final String CSZ_MAIN_SPLITPOS   = "main.splitPos";
  public static final String CSZ_tblog_COL_time  = "tbllog.Time";
  public static final String CSZ_tblog_COL_Level = "tbllog.Level";
  public static final String CSZ_tblog_COL_mesg  = "tbllog.Mesg";

  public static final String CSZ_tbfatt_Anno     = "tbfatt.col_Anno";
  public static final String CSZ_tbfatt_Tpfatt   = "tbfatt.col_Tpfatt";
  public static final String CSZ_tbfatt_DtEmiss  = "tbfatt.col_DtEmiss";
  public static final String CSZ_tbfatt_DtIniz   = "tbfatt.col_DtIniz";
  public static final String CSZ_tbfatt_DtFine   = "tbfatt.col_Dtfine";
  public static final String CSZ_tbfatt_TotFatt  = "tbfatt.col_TotFatt";
  public static final String CSZ_tbfatt_FullPath = "tbfatt.col_FulPath";
  public static final String CSZ_tbfatt_Status   = "tbfatt.col_Status";
  //  public static final String CSZ_SPLITPOS     = "splitpos";
  public static final String CSZ_LOG_LEVEL = "logLevel";

  public static final String TGV_annoComp = "annoComp";
  //  public static final String FLD_DataEmiss            = "DataEmiss";
  public static final String TGV_fattNrAnno           = "fattNrAnno";
  public static final String TGV_fattNrNumero         = "fattNrNumero";
  public static final String TGV_periodFattDtIniz     = "periodFattDtIniz";
  public static final String TGV_periodFattDtFine     = "periodFattDtFine";
  public static final String TGV_periodConsEffDtIniz  = "periodConsEffDtIniz";
  public static final String TGV_periodConsEffDtFine  = "periodConsEffDtFine";
  public static final String TGV_periodConsStimDtIniz = "periodConsStimDtIniz";
  public static final String TGV_periodConsStimDtFine = "periodConsStimDtFine";
  // public static final String FLD_CredPrecKwh          = "CredPrecKwh";
  // public static final String FLD_CredAttKwh           = "CredAttKwh";
  //  public static final String FLD_addizFER         = "addizFER";
  public static final String TGV_impostaQuiet = "impostaQuiet";
  // public static final String FLD_TotPagare    = "TotPagare";

  public static final String TGV_accontoBollPrec = "accontoBollPrec";
  public static final String TGV_addizFER        = "addizFER";
  public static final String TGV_assicurazione   = "assicurazione";
  public static final String TGV_Consumofatt     = "Consumofatt";
  public static final String TGV_Contatore       = "Contatore";

  public static final String TGV_CredPrecAnno    = "CredPrecAnno";
  public static final String TGV_CredPrecKwh     = "CredPrecKwh";
  public static final String TGV_CredPrec2Anno   = "CredPrec2Anno";
  public static final String TGV_CredPrec2Kwh    = "CredPrec2Kwh";
  public static final String TGV_CredAttualeAnno = "CredAttAnno";
  public static final String TGV_CredAttKwh      = "CredAttKwh";

  public static final String TGV_DataEmiss           = "DataEmiss";
  public static final String TGV_dtFERiniz           = "dtFERiniz";
  public static final String TGV_dtFERfine           = "dtFERfine";
  public static final String TGV_dtMisureStraordiniz = "dtScontoDDiniz";
  public static final String TGV_dtMisureStraordfine = "dtScontoDDfine";
  public static final String TGV_MisureStraord       = "ScontoDD";
  public static final String TGV_dtScad              = "dtScad";
  public static final String TGV_FattNr              = "FattNr";
  public static final String TGV_LettAttuale         = "LettAttuale";
  public static final String TGV_LettCoeffK          = "LettCoeffK";
  public static final String TGV_LettConsumo         = "LettConsumo";
  public static final String TGV_LettData            = "LettData";
  public static final String TGV_LettDtAttuale       = "LettDtAttuale";
  public static final String TGV_LettDtPrec          = "LettDtPrec";
  public static final String TGV_LettImp             = "LettImp";
  public static final String TGV_LettPrec            = "LettPrec";
  public static final String TGV_Stimato             = "stimato";
  public static final String TGV_lettPrezzoU         = "lettPrezzoU";
  public static final String TGV_LettProvv           = "LettProvv";
  public static final String TGV_LettQta             = "LettQta";
  public static final String TGV_lettQtaMc           = "lettQtaMc";
  public static final String TGV_LettUMis            = "LettUMis";
  public static final String TGV_PeriodFattDtFine    = "PeriodFattDtFine";
  public static final String TGV_PeriodFattDtIniz    = "PeriodFattDtIniz";
  public static final String TGV_PeriodCongDtFine    = "PeriodCongDtFine";
  public static final String TGV_PeriodCongDtIniz    = "PeriodCongDtIniz";
  public static final String TGV_PeriodEffDtFine     = "PeriodEffDtFine";
  public static final String TGV_PeriodEffDtIniz     = "PeriodEffDtIniz";
  public static final String TGV_PeriodAccontoDtFine = "PeriodAccontoDtFine";
  public static final String TGV_PeriodAccontoDtIniz = "PeriodAccontoDtIniz";
  public static final String TGV_periodoA            = "periodoA";
  public static final String TGV_periodoDa           = "periodoDa";
  public static final String TGV_PotConsumo          = "PotConsumo";
  public static final String TGV_PotConsumo2         = "PotConsumo2";
  public static final String TGV_PotCostUnit         = "PotCostUnit";
  public static final String TGV_PotDtA              = "PotDtA";
  public static final String TGV_PotDtDa             = "PotDtDa";
  public static final String TGV_potImpUnit          = "potImpUnit";
  public static final String TGV_PotTotale           = "PotTotale";
  public static final String TGV_RestituzAccPrec     = "RestituzAccPrec";
  public static final String TGV_seqAddizFER         = "seqAddizFER";
  public static final String TGV_TipoCausale         = "TipoCausale";
  public static final String TGV_TipoEnergia         = "TipoEnergia";
  public static final String TGV_TipoLett            = "TipoLett";
  public static final String TGV_tipoPotImpegn       = "tipoPotImpegn";
  public static final String TGV_tipoScaglione       = "tipoScaglione";
  public static final String TGV_TotPagare           = "TotPagare";
  public static final String TGV_valKappa            = "valKappa";

  public static final String SqlCol_idIntesta   = "idIntesta";
  public static final String SqlCol_NomeIntesta = "NomeIntesta";
  public static final String SqlCol_DtIniz      = "DtIniz";
  public static final String SqlCol_DtFine      = "DtFine";
  public static final String SqlCol_dirfatture  = "dirfatture";

  public static final String SqlCol_tipoFatt         = "tipofatt";
  public static final String SqlCol_idFattura        = "idFattura";
  public static final String SqlCol_idEEFattura      = "idEEFattura";
  public static final String SqlCol_annoComp         = "annoComp";
  public static final String SqlCol_DataEmiss        = "DataEmiss";
  public static final String SqlCol_fattNrAnno       = "fattNrAnno";
  public static final String SqlCol_fattNrNumero     = "fattNrNumero";
  public static final String SqlCol_periodFattDtIniz = "periodFattDtIniz";
  public static final String SqlCol_periodFattDtFine = "periodFattDtFine";
  public static final String SqlCol_CredPrecKwh      = "CredPrecKwh";
  public static final String SqlCol_CredAttKwh       = "CredAttKwh";
  public static final String SqlCol_addizFER         = "addizFER";
  public static final String SqlCol_impostaQuiet     = "impostaQuiet";
  public static final String SqlCol_TotPagare        = "TotPagare";
  public static final String SqlCol_nomeFile         = "nomeFile";
  public static final String SqlCol_fullPath         = "fullPath";
  public static final String SqlCol_totFattura       = "totFattura";

  public static final String SqlCol_idGASFattura        = "idGASFattura";
  public static final String SqlCol_periodEffDtIniz     = "periodEffDtIniz";
  public static final String SqlCol_periodEffDtFine     = "periodEffDtFine";
  public static final String SqlCol_periodAccontoDtIniz = "periodAccontoDtIniz";
  public static final String SqlCol_periodAccontoDtFine = "periodAccontoDtFine";
  public static final String SqlCol_accontoBollPrec     = "accontoBollPrec";

  public static final String SqlCol_idH2OFattura     = "idH2OFattura";
  public static final String SqlCol_periodCongDtIniz = "periodCongDtIniz";
  public static final String SqlCol_periodCongDtFine = "periodCongDtFine";
  public static final String SqlCol_assicurazione    = "assicurazione";
  public static final String SqlCol_RestituzAccPrec  = "RestituzAccPrec";

  // =====================================================================
  // ***************     INTESTA    **************
  // =====================================================================
  public static final String QRY_sel_intesta = """
      SELECT idIntesta
            ,NomeIntesta
            ,dirfatture
       FROM Intesta
       WHERE 1=1""";
  public static final String QRY_upd_intesta = """
      UPDATE intesta SET
       NomeIntesta=?
       ,dirfatture=?
       WHERE  idIntesta=?""";
  public static final String QRY_ins_intesta = """
      INSERT INTO Intesta
       (idIntesta
       ,NomeIntesta
       ,dirfatture)
       VALUES ( ?, ?, ? )""";

  public static String[] CSZ_arrtabs = { "Consumo", "Lettura", "Fattura" };

  // =====================================================================
  // ***************     ELETTRICITA    **************
  // =====================================================================

  public static final String QRY_ins_EEFattura = """
      INSERT INTO EEFattura
                 (idIntesta
                 ,annoComp
                 ,DataEmiss
                 ,fattNrAnno
                 ,fattNrNumero
                 ,periodFattDtIniz
                 ,periodFattDtFine
                 ,CredPrecKwh
                 ,CredAttKwh
                 ,addizFER
                 ,impostaQuiet
                 ,TotPagare
                 ,nomeFile)
           VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);
      """;

  public static final String QRY_ins_EELettura = """
      INSERT INTO EELettura
                 (idEEFattura
                 ,LettDtPrec
                 ,LettPrec
                 ,TipoLettura
                 ,LettDtAttuale
                 ,LettAttuale
                 ,LettConsumo)
           VALUES (?,?,?,?,?,?,?)
      """;
  public static final String QRY_ins_EEConsumo = """
      INSERT INTO EEConsumo
                (idEEFattura
                ,tipoSpesa
                ,dtIniz
                ,dtFine
                ,stimato
                ,prezzoUnit
                ,quantita
                ,importo)
        VALUES (?,?,?,?,?,?,?,?)
      """;

  // =====================================================================
  // ***************     GAS    **************
  // =====================================================================
  public static final String QRY_ins_GASFattura = """
        INSERT INTO GASFattura
                (idIntesta
                ,annoComp
                ,DataEmiss
                ,fattNrAnno
                ,fattNrNumero
                ,periodFattDtIniz
                ,periodFattDtFine
                ,periodEffDtIniz
                ,periodEffDtFine
                ,periodAccontoDtIniz
                ,periodAccontoDtFine
                ,rimborsoPrec
                ,misureStraord
                ,addizFER
                ,impostaQuiet
                ,TotPagare
                ,nomeFile)
      VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
  public static final String QRY_ins_GASLettura = """
        INSERT INTO GASLettura
                (idGASFattura
                ,lettQtaMc
                ,LettData
                ,TipoLett
                ,matricola
                ,coeffC
                ,Consumofatt)
      VALUES (?, ?, ?, ?, ?, ?, ?)
      """;
  public static final String QRY_ins_GASConsumo = """
      INSERT INTO dbo.GASConsumo
                    (idGASFattura
                    ,tipoSpesa
                    ,dtIniz
                    ,dtFine
                    ,stimato
                    ,prezzoUnit
                    ,quantita
                    ,importo)
            VALUES (?,?,?,?,?,?,?,?)
          """;
  // =====================================================================
  // ***************     ACQUA    **************
  // =====================================================================
  public static final String QRY_ins_H2OFattura = """
        INSERT INTO H2OFattura
                    (idIntesta
                    ,annoComp
                    ,DataEmiss
                    ,fattNrAnno
                    ,fattNrNumero
                    ,periodFattDtIniz
                    ,periodFattDtFine
                    ,periodEffDtIniz
                    ,periodEffDtFine
                    ,periodStimDtIniz
                    ,periodStimDtFine
                    ,assicurazione
                    ,impostaQuiet
                    ,RestituzAccPrec
                    ,TotPagare
                    ,nomeFile)
      VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
  public static final String QRY_ins_H2OLettura = """
      INSERT INTO H2OLettura
                 (idH2OFattura
                 ,lettQtaMc
                 ,LettData
                 ,TipoLett
                 ,matricola
                 ,coeffK
                 ,Consumofatt)
           VALUES
                 (?,?,?,?,?,?,?)
                  """;
  public static final String QRY_ins_H2OConsumo = """
      INSERT INTO H2OConsumo
              (idH2OFattura
              ,tipoSpesa
              ,dtIniz
              ,dtFine
              ,stimato
              ,prezzoUnit
              ,quantita
              ,importo)
            VALUES
              (?,?,?,?,?,?,?,?)
          """;
  // =====================================================================
  // ***************     Fatture generiche    **************
  // =====================================================================
  public static final String QRY_find_Fattura = """
      SELECT id%sFattura FROM %sFattura
       WHERE idIntesta=?
         AND ( id%sFattura = ?
               OR dataEmiss = ?)
          """;

  public static final String QRY_del_Fattura = """
      DELETE  FROM %s WHERE id%sFattura = ?
      """;

  public static final String QRY_ListFilesFatture = """
            SELECT tipofatt
            ,idFattura
            ,idIntesta
            ,nomeIntesta
            ,annoComp
            ,DataEmiss
            ,dtIniz
            ,dtFine
            ,totFattura
            ,fullPath
        FROM aass.dbo.viewFatture
        WHERE 1=1
          AND idIntesta=%d
      ORDER BY  dtIniz
            """;
  // =====================================================================
  // ***************     Queries ResultView    **************
  // =====================================================================
  public static final String QRY_ANNOCOMP  = """
      SELECT DISTINCT annoComp FROM EEFattura
       UNION
      SELECT DISTINCT annoComp FROM GASFattura
       UNION
      SELECT DISTINCT annoComp FROM H2OFattura""";
  public static final String QRY_MESESCOMP = """
      SELECT DISTINCT FORMAT(YEAR(cs.dtIniz),'0000') + '-' + FORMAT( MONTH(cs.dtIniz), '00') AS meseComp FROM EEConsumo as cs
       UNION
      SELECT DISTINCT FORMAT(YEAR(cs.dtIniz),'0000') + '-' + FORMAT( MONTH(cs.dtIniz), '00') AS meseComp FROM GASConsumo as cs
       UNION
      SELECT DISTINCT FORMAT(YEAR(cs.dtIniz),'0000') + '-' + FORMAT( MONTH(cs.dtIniz), '00') AS meseComp FROM H2OConsumo as cs""";

}
