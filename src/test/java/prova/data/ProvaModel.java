package prova.data;

import org.junit.Test;

import sm.clagenna.fattaass.data.FattAassModel;

public class ProvaModel {
  
  private FattAassModel model;

  public ProvaModel() {
    // 
  }
  
  @Test
  public void doIt() {
    model = new FattAassModel();
    model.initApp(null);
  }

}
