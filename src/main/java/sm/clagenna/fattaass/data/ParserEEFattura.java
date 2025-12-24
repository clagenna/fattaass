package sm.clagenna.fattaass.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;
import sm.clagenna.fattaass.data.rec.RecEEConsumi;
import sm.clagenna.fattaass.data.rec.RecEEFattura;
import sm.clagenna.fattaass.data.rec.RecEELettura;
import sm.clagenna.fattaass.enums.ETipoEEConsumo;
import sm.clagenna.fattaass.enums.ETipoLettProvvenienza;
import sm.clagenna.fattaass.sys.ex.ReadFattPDFException;
import sm.clagenna.stdcla.pdf.ETipiDato;
import sm.clagenna.stdcla.pdf.FromPdf2Html;
import sm.clagenna.stdcla.utils.ParseData;

public class ParserEEFattura extends ParserFattura implements IParserFatture {
  static final Logger s_log = LogManager.getLogger(ParserEEFattura.class);

  @Getter
  private RecEEFattura       fattura;
  @Getter
  private List<RecEELettura> liLetture;
  @Getter
  private List<RecEEConsumi> liConsumi;
  private boolean            stimati;

  public ParserEEFattura(FattAassModel p_model) {
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
    fattura = new RecEEFattura();
    liLetture = new ArrayList<>();
    liConsumi = new ArrayList<>();
    startIndex(100);
    try {
      parseLetture();
    } catch (ReadFattPDFException e) {
      s_log.error("Errore parsing delle letture EE, err={}", e.getMessage());
      return -1;
    }
    parseCreditoEnergia();
    try {
      parseConsumi();
    } catch (ReadFattPDFException e) {
      s_log.error("Errore parsing dei consumi EE, err={}", e.getMessage());
      return -1;
    }
    parseAddizFER();
    parseImpostaQuietanza();
    fillFattura();
    endParsing();
    return mapTgv.size() + liLetture.size() + liConsumi.size();
  }

  /**
   * <pre>
   * Energia Attiva Tipo Lettura
   * 1/07/2020
   * 22.053
   * LETTURA
   * REALE
   * 1/08/2020
   * 22.376
   * 23,00
   * 1,00
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

      if (parseWithTokens("Azienda autonoma di stato")) {
        bStop = true;
        commitIndex();
        // backTrack dopo l'ultima riga OK
        startIndex(lastGoodIndx);
        break;
      }
      RecEELettura recLett = new RecEELettura();
      // -----  Data Lettura Prec. --------
      recLett.setDtLettPrec(ParseData.toLocalDateTime(hvtpd.getValData()));

      // -----  Lettura Prec. --------
      if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float))
        continue;
      recLett.setLettPrec(hvtpd.getIntero());

      // -----  Tipo Provenienza Lettura [LETTURA REALE] --------
      if ( !isMyToken(ETipiDato.Stringa))
        continue;
      String szTpLett = hvtpd.getTxt();
      if ( !isMyToken(ETipiDato.Stringa))
        continue;
      szTpLett += " " + hv.getTxt();
      ETipoLettProvvenienza tpLett = ETipoLettProvvenienza.parse(szTpLett);
      if (null == tpLett) {
        s_log.error("Non interpreto Tipo Lettura:{}", szTpLett);
        continue;
      }
      recLett.setTipoLettura(tpLett);

      // -----  Dt Lettura Attuale  --------
      if ( !isMyToken(ETipiDato.Data))
        continue;
      recLett.setDtLettAttuale(ParseData.toLocalDateTime(hv.getValData()));

      // -----  Lettura Attuale  --------
      if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float))
        continue;
      recLett.setLettAttuale(hv.getvDbl().intValue());

      // -----  Consumo  --------
      if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float))
        continue;
      recLett.setConsumo(hv.getvDbl());

      // -----  Coeff K --------
      if ( !isMyToken(ETipiDato.Float))
        continue;
      recLett.setCoeffK(hv.getValDouble());

      // ------ fine con successo di scansione della riga -----------------
      liLetture.add(recLett);
      commitIndex();
      // l'ultima posizione di lettura Ok
      lastGoodIndx = getCurrIndx();
    }
    return true;
  }

  //
  //  private boolean parseConsumi() {
  //    return parseConsumiEnergia() //
  //        || parseConsumiPUN() //
  //        || parseConsumiSpread() //
  //        || parseConsumiPotImpegn() //
  //        || parseConsumiRaccRifiuti();
  //  }

  /**
   * <pre>
   * Credito
   * precedente
   * anno
   * 2019:
   * 0
   * Credito
   * precedente
   * anno
   * 2020:
   * 0
   * kWh
   * Credito
   * attuale
   * anno
   * 2019:
   * 0
   * </pre>
   */
  private boolean parseCreditoEnergia() {
    boolean bStop = false;
    int maxIndx = getCurrIndx() + 200;

    for (; getCurrIndx() < maxIndx && !bStop /* && currPag == getPagNo() */; nextIndex()) {
      checkPoint();
      if (parseWithTokens("Credito", "precedente", "anno", ETipiDato.Intero)) {
        int credPrecAnno = hvtpd.getIntero();
        mapTgv.put(Consts.TGV_CredPrecAnno, credPrecAnno);
        nextToken();
        mapTgv.put(Consts.TGV_CredPrecKwh, hv.getIntero());
        fattura.setCredPrecKwh(hv.getIntero());
        commitIndex();
        continue;
      }

      if (parseWithTokens("Credito", "precedente", "anno", ETipiDato.Intero)) {
        String credPrec2Anno = hvtpd.getTxt();
        mapTgv.put(Consts.TGV_CredPrec2Anno, credPrec2Anno);
        nextToken();
        mapTgv.put(Consts.TGV_CredPrec2Kwh, hv.getIntero());
        commitIndex();
        continue;
      }

      if (parseWithTokens("Credito", "attuale", "anno", ETipiDato.Intero)) {
        String credPrecAnno = hvtpd.getTxt();
        mapTgv.put(Consts.TGV_CredAttualeAnno, credPrecAnno);
        fattura.setCredAttKwh(hv.getIntero());
        nextToken();
        mapTgv.put(Consts.TGV_CredPrecAnno, hv.getIntero());
        commitIndex();
        continue;
      }
      // se sono arrivato ai consumi, fermo tutto
      if (parseWithTokens("Servizio", "energia", "Elettrica")) {
        bStop = true;
        commitIndex();
        startIndex(getCurrIndx() - 4);
        break;
      }

    }
    return true;
  }

  private boolean parseConsumi() throws ReadFattPDFException {
    int iniz = cercaInizio("SERVIZI", "ENERGIA", "ELETTRICA");
    if (iniz < 3)
      throw new ReadFattPDFException("Non ho l'indice per parsare i consumi");
    startIndex(iniz);
    boolean stopScan = false;
    int qtaFails = 0;
    for (; getCurrIndx() < liVals.size() && qtaFails < 4 && !stopScan; nextIndex()) {
      checkPoint();
      RecEEConsumi recCons = new RecEEConsumi();
      recCons.setStimato(stimati);

      // ----------------------------------------
      //  RESTITUZIONE ACCONTI BOLLETTE PRECEDENTI       -225,96
      if (parseWithTokens("RESTITUZIONE", "ACCONTI", "BOLLETTE", "PRECEDENTI", ETipiDato.Float)) {
        mapTgv.put(Consts.TGV_accontoBollPrec, hvtpd.getValDouble());
        fattura.setRestitBollPrec(hv.getValDouble());
        commitIndex();
        continue;
      }

      // ----------------------------------------
      //  CONSUMI EFFETTIVI
      if (parseWithTokens("CONSUMI", "EFFETTIVI")) {
        stimati = false;
        commitIndex();
        continue;
      }
      // ----------------------------------------
      //  CONSUMI EFFETTIVI
      if (parseWithTokens("CONSUMI", "STIMATI")) {
        stimati = true;
        commitIndex();
        continue;
      }
      // ----------------------------------------
      // Energia PUN
      if (parseWithTokens("Corrispettivo", "energia", "pun")) {
        recCons.setTipoSpesa(ETipoEEConsumo.EnergiaPUN);
        if ( !consumiRestoColonne(recCons))
          continue;
        commitIndex();
        continue;
      }

      // ----------------------------------------
      // Energia Spread
      if (parseWithTokens("Corrispettivo", "energia", "spread", ETipiDato.Stringa, "scaglione")) {
        String sz = hvtpd.getTxt();
        ETipoEEConsumo tip = ETipoEEConsumo.parse("spread", sz);
        if (null == tip) {
          s_log.error("Non interpreto Tipo consumo EE:{}", sz);
          continue;
        }
        recCons.setTipoSpesa(tip);

        if ( !consumiRestoColonne(recCons))
          continue;
        commitIndex();
        continue;
      }
      // ----------------------------------------
      // Energia 1/2.. Scaglione
      if (parseWithTokens("Corrispettivo", "energia", ETipiDato.Stringa, "scaglione")) {
        String sz = hvtpd.getTxt();
        ETipoEEConsumo tip = ETipoEEConsumo.parse("energia", sz);
        recCons.setTipoSpesa(tip);
        if ( !consumiRestoColonne(recCons))
          continue;
        commitIndex();
        continue;
      }

      // ----------------------------------------
      // Potenza Impegnata
      if (parseWithTokens("Corrispettivo", "potenza", "impegnata")) {
        recCons.setTipoSpesa(ETipoEEConsumo.PotenzaImpegnata);
        if ( !consumiRestoColonne(recCons))
          continue;
        commitIndex();
        continue;
      }

      // ----------------------------------------
      // Raccolta Rifiuti
      if (parseWithTokens("Tariffa", "raccolta", "rifiuti")) {
        recCons.setTipoSpesa(ETipoEEConsumo.RaccRifiuti);
        if ( !consumiRestoColonne(recCons))
          continue;
        commitIndex();
        continue;
      }

      if (parseWithTokens("TOTALE", "SERVIZI", "ENERGIA", "ELETTRICA")) {
        stopScan = true;
        commitIndex();
        continue;
      }
    }
    return true;
  }

  private boolean consumiRestoColonne(RecEEConsumi recCons) {
    if ( !isMyToken(ETipiDato.Data)) // data Inizio
      return false;
    recCons.setDtIniz(ParseData.toLocalDateTime(hv.getValData()));

    if ( !isMyToken(ETipiDato.Data)) // data Fine
      return false;
    recCons.setDtFine(ParseData.toLocalDateTime(hv.getValData()));

    if ( !isMyToken(ETipiDato.Stringa) || !isMyToken(ETipiDato.Intero, ETipiDato.Float)) // prezzo unitario
      return false;
    recCons.setPrezzoUnit(hv.getValDouble());

    if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float)) // quantita
      return false;
    recCons.setQuantita(hv.getvDbl());

    if ( !isMyToken(ETipiDato.Intero, ETipiDato.Float)) // Totale riga
      return false;
    recCons.setImporto(hv.getvDbl());

    liConsumi.add(recCons);
    return true;
  }

  private boolean parseAddizFER() {
    int qtaFails = 0;

    for (; getCurrIndx() < liVals.size() && qtaFails < 26; nextIndex()) {
      checkPoint();
      if ( !parseWithTokens("Addizionale", "incentivi", "FER", ETipiDato.Data)) {
        qtaFails++;
        continue;
      }
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

  private boolean parseImpostaQuietanza() {
    boolean bStop = false;

    for (; getCurrIndx() < liVals.size() && !bStop; nextIndex()) {

      if (parseWithTokens("totale bolletta")) {
        commitIndex();
        bStop = true;
        continue;
      }
      if (parseWithTokens("Imposta", "di", "quietanza", ETipiDato.Float)) {
        // -------- Imposta di quietanza -----------
        mapTgv.put(Consts.TGV_impostaQuiet, hv.getValDouble());
        fattura.setImpostaQuiet(hv.getValDouble());
        commitIndex();
        return true;
      }
    }
    return false;
  }

  private void fillFattura() {
    fattura = new RecEEFattura();
    fattura.setIdIntesta(getModel().getRecIntesta().getIdIntestaInt());
    fattura.setDataEmiss(getDateOrNull(Consts.TGV_DataEmiss));
    fattura.setAnnoComp(getIntegerOrNull(Consts.TGV_annoComp));
    fattura.setPeriodFattDtIniz(getDateOrNull(Consts.TGV_PeriodFattDtIniz));
    fattura.setPeriodFattDtFine(getDateOrNull(Consts.TGV_PeriodFattDtFine));

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

    fattura.setAddizFER(getDoubleOrNull(Consts.TGV_addizFER));
    fattura.setImpostaQuiet(getDoubleOrNull(Consts.TGV_impostaQuiet));
    fattura.setRestitBollPrec(getDoubleOrNull(Consts.TGV_accontoBollPrec));
    fattura.setTotPagare(getDoubleOrNull(Consts.TGV_TotPagare));
    fattura.setNomeFile(getPdf2html().getFilePDF().toString());
  }

}
