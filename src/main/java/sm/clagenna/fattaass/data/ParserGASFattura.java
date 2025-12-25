package sm.clagenna.fattaass.data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import sm.clagenna.fattaass.data.rec.RecGasConsumo;
import sm.clagenna.fattaass.data.rec.RecGasFattura;
import sm.clagenna.fattaass.data.rec.RecGasLettura;
import sm.clagenna.fattaass.enums.ETipoGASConsumo;
import sm.clagenna.fattaass.enums.ETipoLettProvvenienza;
import sm.clagenna.fattaass.sys.ex.ReadFattPDFException;
import sm.clagenna.stdcla.pdf.ETipiDato;
import sm.clagenna.stdcla.pdf.FromPdf2Html;
import sm.clagenna.stdcla.utils.ParseData;

public class ParserGASFattura extends ParserFattura implements IParserFatture {
  private static final Logger s_log = LogManager.getLogger(ParserGASFattura.class);

  @Getter
  private RecGasFattura       fattura;
  @Getter
  private List<RecGasLettura> liLetture;
  @Getter
  private List<RecGasConsumo> liConsumi;
  private boolean             stimati;

  public ParserGASFattura(FattAassModel p_model) {
    super(p_model);
  }

  @Override
  public Logger getLog() {
    return s_log;
  }

  @Override
  public int parse(FromPdf2Html pdf2html) {
    int qtaToks = super.parse(pdf2html);
    if (qtaToks <= 0)
      return 0;
    stimati = false;
    fattura = new RecGasFattura();
    liLetture = new ArrayList<>();
    liConsumi = new ArrayList<>();
    startIndex(100);
    try {
      parseLetture();
    } catch (ReadFattPDFException e) {
      s_log.error("Errore parsing delle letture GAS, err={}", e.getMessage());
      return -1;
    }
    parsePeriodiVari();
    try {
      parseConsumi();
    } catch (ReadFattPDFException e) {
      s_log.error("Errore parsing dei consumi GAS, err={}", e.getMessage());
      fillFattura();
      return -1;
    }
    parseMisureStraordinarie();
    parseAddizFER();
    parseImpostaQuiet();
    fillFattura();
    endParsing();
    return mapTgv.size() + liLetture.size() + liConsumi.size();
  }

  private boolean parsePeriodiVari() {
    int maxIndx = getCurrIndx() + 200;
    boolean bStop = false;

    // Consumi Effettivi: dal 01/07/2025 al 31/08/2025
    for (; getCurrIndx() < maxIndx && !bStop; nextIndex()) {
      checkPoint();

      boolean bRet = parseWithTokens("Consumi", "effettivi", "dal", ETipiDato.Data);
      if ( !bRet)
        bRet = parseWithTokens("Consumi", "effettivi:", "dal", ETipiDato.Data);
      if (bRet) {
        LocalDateTime periodEffDa = ParseData.toLocalDateTime(hvtpd.getValData());
        mapTgv.put(Consts.TGV_PeriodEffDtIniz, periodEffDa);
        bRet = parseWithTokens("al", ETipiDato.Data);
        if (bRet) {
          LocalDateTime periodEffAl = ParseData.toLocalDateTime(hv.getValData());
          mapTgv.put(Consts.TGV_PeriodEffDtFine, periodEffAl);
          commitIndex();
          continue;
        }
      }

      bRet = parseWithTokens("Consumi", "stimati", "dal", ETipiDato.Data);
      if ( !bRet)
        bRet = parseWithTokens("Consumi", "stimati:", "dal", ETipiDato.Data);
      if (bRet) {
        LocalDateTime periodStimDa = ParseData.toLocalDateTime(hvtpd.getValData());
        mapTgv.put(Consts.TGV_PeriodAccontoDtIniz, periodStimDa);
        bRet = parseWithTokens("al", ETipiDato.Data);
        if (bRet) {
          LocalDateTime periodStimAl = ParseData.toLocalDateTime(hv.getValData());
          mapTgv.put(Consts.TGV_PeriodAccontoDtFine, periodStimAl);
          commitIndex();
          continue;
        }
      }

      if (parseWithTokens("Servizi fornitura gas metano")) {
        bStop = true;
        commitIndex(); // evidenzio, se serve
        // backTrack dopo l'ultima riga OK
        startIndex(getCurrIndx() - 5);
        break;
      }
    }
    return true;
  }

  /**
   * <pre>
   * 89                 mc
   * 1/07/2020          date
   * EFFETTIVA          tipoLett
   * MTSB03BF07285756   Matricola
   * 1,0000000          C
   * 9,0000000          Consumo
   * </pre>
   *
   * @return
   * @throws ReadFattPDFException
   */
  private boolean parseLetture() throws ReadFattPDFException {
    int iniz = cercaInizio("DETTAGLIO", "LETTURE");
    if (iniz < 3)
      throw new ReadFattPDFException("Non ho l'indice per parsare le letture");
    startIndex(iniz);
    boolean bStop = false;
    int lastGoodIndx = -1;
    int maxIndx = getCurrIndx() + 200;
    for (; getCurrIndx() < maxIndx && !bStop; nextIndex()) {
      checkPoint();

      if (parseWithTokens("Totale Consumi Smc")) {
        bStop = true;
        commitIndex();
        // backTrack dopo l'ultima riga OK
        startIndex(lastGoodIndx);
        break;
      }

      if (parseWithTokens("Totale Consumi mc")) {
        bStop = true;
        commitIndex();
        // backTrack dopo l'ultima riga OK
        startIndex(lastGoodIndx);
        break;
      }
      RecGasLettura recLett = new RecGasLettura();

      // -----  Metri cubici --------
      if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float))
        continue;
      recLett.setLettQtaMc(hv.getvDbl().intValue());

      // -----  Dt Lettura Attuale  --------
      if ( !isMyToken(ETipiDato.Data))
        continue;
      recLett.setLettData(ParseData.toLocalDateTime(hv.getValData()));

      // -----  Tipo Provenienza Lettura  --------
      if ( !isMyToken(ETipiDato.Stringa))
        continue;
      String szTpLett = hv.getTxt();
      ETipoLettProvvenienza tpLett = ETipoLettProvvenienza.parse(szTpLett);
      if (null == tpLett) {
        s_log.debug("Non interpreto Tipo Lettura:{}", szTpLett);
        continue;
      }
      recLett.setTipoLett(tpLett);

      // -----  Matricola Contatore  --------
      if ( !isMyToken(ETipiDato.Stringa, ETipiDato.IntN15))
        continue;
      recLett.setMatricola(hv.getTxt());

      // -----  Coeff C --------
      if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float))
        continue;
      recLett.setCoeffC(hv.getvDbl());

      // -----  Consumo  --------
      //      if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float))
      //        continue;
      if (isMyToken(ETipiDato.Intero, ETipiDato.Float))
        recLett.setConsumo(hv.getvDbl());
      else
        recLett.setConsumo(0f);

      // ------ fine con successo di scansione della riga -----------------
      liLetture.add(recLett);
      commitIndex();
      // l'ultima posizione di lettura Ok
      lastGoodIndx = getCurrIndx();
    }
    return true;
  }

  private boolean parseConsumi() throws ReadFattPDFException {
    int iniz = cercaInizio("SERVIZI", "FORNITURA", "GAS", "METANO");
    if (iniz < 3)
      throw new ReadFattPDFException("Non ho l'indice per parsare i consumi");
    startIndex(iniz);
    boolean stopScan = false;
    int qtaFails = 0;
    for (; getCurrIndx() < liVals.size() && qtaFails < 4 && !stopScan; nextIndex()) {
      checkPoint();
      RecGasConsumo recCons = new RecGasConsumo();
      recCons.setStimato(stimati);

      // ----------------------------------------
      //  restituzione ACCONTI BOLLETTE PRECEDENTI

      if (parseWithTokens("RESTITUZIONE", "ACCONTI", "BOLLETTE", "PRECEDENTI", ETipiDato.Float)) {
        mapTgv.put(Consts.TGV_accontoBollPrec, hvtpd.getValDouble());
        commitIndex();
        continue;
      }

      // ----------------------------------------
      //  CONSUMI EFFETTIVI

      if (parseWithTokens("CONSUMI EFFETTIVI")) {
        stimati = false;
        commitIndex();
        continue;
      }
      // ----------------------------------------
      //  CONSUMI STIMATI
      if (parseWithTokens("CONSUMI STIMATI")) {
        stimati = true;
        commitIndex();
        continue;
      }
      // ----------------------------------------
      // Materia Prima Gas 1°/2° ... Scaglione

      if (parseWithTokens("Materia", "prima", "gas", ETipiDato.Stringa, "scaglione")) {
        String sz = String.format(" %s ", hvtpd.getTxt());
        ETipoGASConsumo tip = ETipoGASConsumo.parse(sz);
        if (null == tip) {
          s_log.error("Non interpreto Tipo consumo:{}", sz);
          continue;
        }
        recCons.setTipoSpesa(tip);
        // "scaglione"
        if ( !consumiRestoColonne(recCons))
          continue;
        commitIndex();
        continue;
      }

      // ----------------------------------------
      // Spread 1°/2° ... Scaglione
      if (parseWithTokens("Spread", ETipiDato.Stringa, "scaglione")) {
        String sz = String.format("spread %s ", hvtpd.getTxt());
        ETipoGASConsumo tip = ETipoGASConsumo.parse(sz);
        if (null == tip) {
          s_log.error("Non interpreto Tipo consumo:{}", sz);
          return false;
        }
        recCons.setTipoSpesa(tip);
        if ( !consumiRestoColonne(recCons))
          continue;
        commitIndex();
        continue;
      }

      // ----------------------------------------
      // Quota Fissa

      if (parseWithTokens("Quota", "fissa")) {
        recCons.setTipoSpesa(ETipoGASConsumo.QuotaFissa);
        if ( !consumiRestoColonne(recCons))
          continue;
        commitIndex();
        continue;
      }

      if (parseWithTokens("TOTALE SERVIZI FORNITURA GAS METANO")) {
        stopScan = true;
        commitIndex();
        continue;
      }
    }
    return true;
  }

  private boolean consumiRestoColonne(RecGasConsumo recCons) {

    if ( !isMyToken(ETipiDato.Data)) // data Inizio
      return false;
    recCons.setDtIniz(ParseData.toLocalDateTime(hv.getValData()));

    if ( !isMyToken(ETipiDato.Data)) // data Fine
      return false;
    recCons.setDtFine(ParseData.toLocalDateTime(hv.getValData()));

    boolean bRet = parseWithTokens("metri cubi");
    if ( !bRet)
      bRet = isMyToken("giorni");
    if ( !bRet)
      bRet = isMyToken("Smc");
    if ( !bRet)
      return bRet;

    if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float)) // prezzo unitario
      return false;
    recCons.setPrezzoUnit(hv.getValDouble());

    if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float)) // quantita
      return false;
    recCons.setQuantita(hv.getvDbl());

    if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float)) // Totale riga
      return false;
    recCons.setImporto(hv.getvDbl());

    liConsumi.add(recCons);
    commitIndex();
    return true;
  }

  private boolean parseMisureStraordinarie() {
    int qtaFails = 0;
    checkPoint();
    int iniz = cercaInizio("misure straordinarie per il contenimento");
    if (iniz < 0) {
      backTrack();
      return false;
    }
    for (; getCurrIndx() < liVals.size() && qtaFails < 26; nextIndex()) {
      checkPoint();
      if ( !parseWithTokens("sconto", "D.D.", "6", "marzo", "2025", "n.34", "ga", ETipiDato.Data)) {
        qtaFails++;
        continue;
      }
      // -------- Misure straordinarie: data Iniz. -----------
      mapTgv.put(Consts.TGV_dtMisureStraordiniz, ParseData.toLocalDateTime(hvtpd.getValData()));

      // -------- Misure straordinarie: data Iniz. -----------
      if ( !isMyToken(ETipiDato.Data))
        continue;
      mapTgv.put(Consts.TGV_dtMisureStraordfine, ParseData.toLocalDateTime(hv.getValData()));

      // -------- Misure Straordinarie: data Iniz. -----------
      if ( !isMyToken(ETipiDato.Float))
        continue;
      mapTgv.put(Consts.TGV_MisureStraord, hv.getValDouble());
      commitIndex();
      return true;
    }
    return false;
  }

  private boolean parseAddizFER() {
    boolean bStop = false;
    checkPoint();
    int iniz = cercaInizio("oneri diversi da quelli");
    if (iniz < 0) {
      backTrack();
      return false;
    }
    for (; getCurrIndx() < liVals.size() && !bStop; nextIndex()) {
      checkPoint();
      if (parseWithTokens("TOTALE BOLLETTA")) {
        bStop = true;
        continue;
      }
      if ( !parseWithTokens("Addizionale", "incentivi", "FER", ETipiDato.Data))
        continue;

      // -------- Addizionale FER: data Iniz. -----------
      mapTgv.put(Consts.TGV_dtFERiniz, ParseData.toLocalDateTime(hvtpd.getValData()));

      // -------- Addizionale FER: data Iniz. -----------
      if ( !isMyToken(ETipiDato.Data))
        continue;
      mapTgv.put(Consts.TGV_dtFERfine, ParseData.toLocalDateTime(hv.getValData()));

      // -------- Addizionale FER: data Iniz. -----------
      if ( !isMyToken(ETipiDato.Float))
        continue;
      mapTgv.put(Consts.TGV_addizFER, hv.getValDouble());
      fattura.setAddizFER(hv.getValDouble());
      commitIndex();
      return true;
    }
    return false;
  }

  private boolean parseImpostaQuiet() {
    int qtaFails = 0;

    for (; getCurrIndx() < liVals.size() && qtaFails < 26; nextIndex()) {
      checkPoint();
      if ( !parseWithTokens("imposta", "di", "quietanza", ETipiDato.Data)) {
        qtaFails++;
        continue;
      }
      mapTgv.put(Consts.TGV_impostaQuiet, hvtpd.getValDouble());
      commitIndex();
      return true;
    }
    return false;
  }

  private void fillFattura() {
    fattura = new RecGasFattura();
    fattura.setIdIntesta(getModel().getRecIntesta().getIdIntestaInt());
    fattura.setAnnoComp(getIntegerOrNull(Consts.TGV_annoComp));
    fattura.setDataEmiss(getDateOrNull(Consts.TGV_DataEmiss));
    String sz = getStringOrNull(Consts.TGV_FattNr);
    if (null == sz) {
      s_log.error("Il No Fattura mancante");
      throw new UnsupportedOperationException("Il No Fattura mancante");
    }
    String[] arr = sz.split("/");
    if (null != arr && arr.length == 2) {
      int nrAnno = Integer.parseInt(arr[0]);
      String NrNumero = arr[1];
      fattura.setFattNrAnno(nrAnno);
      fattura.setFattNrNumero(NrNumero);
    } else
      s_log.error("Il No Fattura \"{}\" e' incongruente", sz);

    fattura.setPeriodFattDtIniz(getDateOrNull(Consts.TGV_PeriodFattDtIniz));
    fattura.setPeriodFattDtFine(getDateOrNull(Consts.TGV_PeriodFattDtFine));
    fattura.setPeriodEffDtIniz(getDateOrNull(Consts.TGV_PeriodEffDtIniz));
    fattura.setPeriodEffDtFine(getDateOrNull(Consts.TGV_PeriodEffDtFine));
    fattura.setPeriodAccontoDtIniz(getDateOrNull(Consts.TGV_PeriodAccontoDtIniz));
    fattura.setPeriodAccontoDtFine(getDateOrNull(Consts.TGV_PeriodAccontoDtFine));
    fattura.setRimborsoPrec(getDoubleOrNull(Consts.TGV_accontoBollPrec));
    fattura.setMisureStraord(getDoubleOrNull(Consts.TGV_MisureStraord));
    fattura.setAddizFER(getDoubleOrNull(Consts.TGV_addizFER));
    fattura.setImpostaQuiet(getDoubleOrNull(Consts.TGV_impostaQuiet));
    fattura.setTotPagare(getDoubleOrNull(Consts.TGV_TotPagare));
    String szBase = getModel().getRecIntesta().getDirFatture();
    String szPath = getPdf2html().getFilePDF().toString();
    String szResPath = szPath.replace(szBase, "");
    if ( !Character.isLetter(szResPath.charAt(0)))
      szResPath = szResPath.substring(1);
    fattura.setNomeFile(szResPath);
  }

}
