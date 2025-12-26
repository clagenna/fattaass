package sm.clagenna.fattaass.data;

import java.time.LocalDateTime;

import sm.clagenna.fattaass.sys.ex.ReadFattException;
import sm.clagenna.stdcla.pdf.FromPdf2Html;
import sm.clagenna.stdcla.pdf.IParseHtmlValues;

public interface IParserFatture extends IParseHtmlValues {

  IRecFattura getFattura();

  @Override
  int parse(FromPdf2Html pdf2html);

  LocalDateTime getDate(String tgNam) throws ReadFattException;

  LocalDateTime getDateOrNull(String tgNam);

  Integer getInteger(String tgNam) throws ReadFattException;

  int getIntegerOrNull(String tgNam);

  String getString(String tgNam) throws ReadFattException;

  String getStringOrNull(String tgNam);

  Double getDouble(String tgNam) throws ReadFattException;

  double getDoubleOrNull(String tgNam);

}
