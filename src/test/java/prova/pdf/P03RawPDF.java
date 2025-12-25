package prova.pdf;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.fit.pdfdom.PDFDomTree;
import org.junit.Test;

public class P03RawPDF {
  private ArrayList<String> outHtml;

  @Test
  public void doTheJob() throws InvalidPasswordException, IOException {
    System.setProperty("org.slf4j.simpleLogger.log.org.mabb.fontverter.opentype.TtfInstructions", "warn");
    Path pth = Paths.get("data/EE_2020-08-01_2020-12-31.pdf");
    try (PDDocument pdf = PDDocument.load(pth.toFile()); StringWriter swr = new StringWriter();) {
      new PDFDomTree().writeText(pdf, swr);
      outHtml = new ArrayList<>();
      String[] arr = swr.toString().split("\n");
      outHtml.addAll(Arrays.asList(arr));
    }
    outHtml //
        .stream() //
        .forEach(System.out::println);
  }

}
