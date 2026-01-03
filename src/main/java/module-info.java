/**
 * 
 */
module fattaass {
  exports sm.clagenna.fattaass.data;
  exports sm.clagenna.fattaass.sys.ex;
  exports sm.clagenna.fattaass.enums;
  exports sm.clagenna.fattaass.sql;
  opens sm.clagenna.fattaass.javafx to javafx.graphics, javafx.fxml;
  opens sm.clagenna.fattaass.jvafxp to javafx.graphics, javafx.fxml;

  requires FontVerter;
  requires transitive java.desktop;
  requires java.logging;
  requires java.sql;
  requires java.xml;
  requires javafx.base;
  requires javafx.graphics;
  requires javafx.web;
  requires javafx.fxml;
  requires lombok;
  requires net.sf.cssbox.pdf2dom;
  requires org.apache.logging.log4j;
  requires org.apache.logging.log4j.core;
  requires org.slf4j;
  requires org.apache.pdfbox;
  requires transitive stdc_pdf;
  requires transitive stdc_sql;
  requires transitive stdc_utils;
  requires stdc_javafx;
  requires javafx.controls;

}
