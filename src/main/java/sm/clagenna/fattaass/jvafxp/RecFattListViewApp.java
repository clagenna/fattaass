package sm.clagenna.fattaass.jvafxp;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Data
class RecFatt {
    private int idEEFatt;
    private int idIntesta;
    private LocalDateTime dtEmiss;
    private String fattNo;
    private Double impValue;
    
    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format("Fatt. %s - €%.2f - %s [ID:%d]", 
            fattNo, impValue, dtEmiss.format(fmt), idEEFatt);
    }
}

public class RecFattListViewApp extends Application {
    
    private ObservableList<RecFatt> fattureList;
    private ListView<RecFatt> listView;
    private Timer timer;
    private Random random = new Random();
    private int nextId = 1;
    private Label statusLabel;
    
    @Override
    public void start(Stage stage) {
        // Inizializza la ObservableList
        fattureList = FXCollections.observableArrayList();
        
        // Crea la ListView
        listView = new ListView<>(fattureList);
        listView.setPrefHeight(400);
        
        // Personalizza la cella per una visualizzazione migliore
        listView.setCellFactory(lv -> new ListCell<RecFatt>() {
            @Override
            protected void updateItem(RecFatt item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    // Colora in base al valore
                    if (item.getImpValue() > 5000) {
                        setStyle("-fx-background-color: #ffe6e6;");
                    } else if (item.getImpValue() > 2000) {
                        setStyle("-fx-background-color: #fff9e6;");
                    } else {
                        setStyle("-fx-background-color: #e6ffe6;");
                    }
                }
            }
        });
        
        // Pulsanti di controllo
        Button btnAggiungi = new Button("Aggiungi");
        btnAggiungi.setOnAction(e -> aggiungiRandomFattura());
        
        Button btnRimuovi = new Button("Rimuovi Casuale");
        btnRimuovi.setOnAction(e -> rimuoviRandomFattura());
        
        Button btnModifica = new Button("Modifica Casuale");
        btnModifica.setOnAction(e -> modificaRandomFattura());
        
        Button btnAvviaAuto = new Button("Avvia Auto-Update");
        btnAvviaAuto.setOnAction(e -> avviaAggiornamentoAutomatico());
        
        Button btnStopAuto = new Button("Stop Auto-Update");
        btnStopAuto.setOnAction(e -> stopAggiornamentoAutomatico());
        
        Button btnSvuota = new Button("Svuota Lista");
        btnSvuota.setOnAction(e -> fattureList.clear());
        
        // Status label
        statusLabel = new Label("Pronto. Elementi: 0");
        statusLabel.setStyle("-fx-font-weight: bold;");
        
        // Layout pulsanti
        HBox buttonBox = new HBox(10, btnAggiungi, btnRimuovi, btnModifica, 
                                   btnAvviaAuto, btnStopAuto, btnSvuota);
        buttonBox.setPadding(new Insets(10));
        
        // Layout principale
        BorderPane root = new BorderPane();
        root.setCenter(listView);
        root.setBottom(new BorderPane(buttonBox, statusLabel, null, null, null));
        BorderPane.setMargin(statusLabel, new Insets(5, 10, 5, 10));
        
        // Aggiungi alcuni dati iniziali
        for (int i = 0; i < 50; i++) {
            aggiungiRandomFattura();
        }
        
        Scene scene = new Scene(root, 800, 500);
        stage.setTitle("Gestione Fatture - ListView Dinamica");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            if (timer != null) timer.cancel();
        });
        stage.show();
    }
    
    private void aggiungiRandomFattura() {
        RecFatt fatt = new RecFatt();
        fatt.setIdEEFatt(nextId++);
        fatt.setIdIntesta(random.nextInt(100) + 1);
        fatt.setDtEmiss(LocalDateTime.now().minusDays(random.nextInt(365)));
        fatt.setFattNo(String.format("FT-%04d/%d", 
            random.nextInt(9999) + 1, 2024));
        fatt.setImpValue(Math.round(random.nextDouble() * 10000 * 100.0) / 100.0);
        
        fattureList.add(fatt);
        aggiornaStatus("Aggiunta fattura: " + fatt.getFattNo());
    }
    
    private void rimuoviRandomFattura() {
        if (!fattureList.isEmpty()) {
            int idx = random.nextInt(fattureList.size());
            RecFatt removed = fattureList.remove(idx);
            aggiornaStatus("Rimossa fattura: " + removed.getFattNo());
        }
    }
    
    private void modificaRandomFattura() {
        if (!fattureList.isEmpty()) {
            int idx = random.nextInt(fattureList.size());
            RecFatt fatt = fattureList.get(idx);
            fatt.setImpValue(Math.round(random.nextDouble() * 10000 * 100.0) / 100.0);
            fatt.setDtEmiss(LocalDateTime.now());
            // Forza l'aggiornamento della vista
            fattureList.set(idx, fatt);
            aggiornaStatus("Modificata fattura: " + fatt.getFattNo());
        }
    }
    
    private void avviaAggiornamentoAutomatico() {
        if (timer != null) {
            timer.cancel();
        }
        
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    int azione = random.nextInt(3);
                    switch (azione) {
                        case 0: aggiungiRandomFattura(); break;
                        case 1: rimuoviRandomFattura(); break;
                        case 2: modificaRandomFattura(); break;
                    }
                });
            }
        }, 1000, 2000); // Ogni 2 secondi
        
        aggiornaStatus("Auto-update avviato");
    }
    
    private void stopAggiornamentoAutomatico() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            aggiornaStatus("Auto-update fermato");
        }
    }
    
    private void aggiornaStatus(String msg) {
        statusLabel.setText(msg + " | Elementi: " + fattureList.size());
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}