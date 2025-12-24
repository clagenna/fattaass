package sm.clagenna.fattaass.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiTaskProgressBar extends Application {
    
    private ProgressBar progressBar;
    private Label statusLabel;
    private ListView<String> taskListView;
    private Button startButton;
    
    private AtomicInteger completedTasks = new AtomicInteger(0);
    private int totalTasks = 5;
    
    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Progress Bar con Multiple Task");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        
        statusLabel = new Label("Pronto per iniziare");
        
        taskListView = new ListView<>();
        taskListView.setPrefHeight(200);
        
        startButton = new Button("Avvia Tasks");
        startButton.setOnAction(e -> startTasks());
        
        root.getChildren().addAll(
            titleLabel,
            new Label("Progresso totale:"),
            progressBar,
            statusLabel,
            new Label("Stato dei task:"),
            taskListView,
            startButton
        );
        
        Scene scene = new Scene(root, 500, 450);
        primaryStage.setTitle("JavaFX Multi-Task Progress");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void startTasks() {
        startButton.setDisable(true);
        taskListView.getItems().clear();
        completedTasks.set(0);
        progressBar.setProgress(0);
        statusLabel.setText("Esecuzione in corso...");
        
        List<Task<Void>> tasks = new ArrayList<>();
        
        for (int i = 0; i < totalTasks; i++) {
            final int taskId = i + 1;
            final String taskName = "Task " + taskId;
            
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Platform.runLater(() -> 
                        taskListView.getItems().add(taskName + ": Iniziato")
                    );
                    
                    // Simula lavoro con durata variabile
                    Random rnd = new Random(new Date().getTime());
                    int steps = rnd.nextInt(200, 300);
                    for (int j = 0; j <= steps; j++) {
                        Thread.sleep(rnd.nextInt(100, 700));
                        updateProgress(j, steps);
                        
                        final int progress = (int)((j * 100.0) / steps);
                        Platform.runLater(() -> {
                            updateTaskStatus(taskName, progress);
                        });
                    }
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        updateTaskStatus(taskName, 100);
                        int completed = completedTasks.incrementAndGet();
                        double totalProgress = (double) completed / totalTasks;
                        progressBar.setProgress(totalProgress);
                        
                        statusLabel.setText(String.format(
                            "Completati: %d/%d task", completed, totalTasks
                        ));
                        
                        if (completed == totalTasks) {
                            statusLabel.setText("Tutti i task completati!");
                            startButton.setDisable(false);
                        }
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        updateTaskStatus(taskName, -1);
                    });
                }
            };
            
            tasks.add(task);
            new Thread(task).start();
        }
    }
    
    private void updateTaskStatus(String taskName, int progress) {
        String status;
        if (progress == 100) {
            status = taskName + ": ✓ Completato";
        } else if (progress == -1) {
            status = taskName + ": ✗ Fallito";
        } else {
            status = taskName + ": " + progress + "%";
        }
        
        // Aggiorna o aggiungi lo stato nella lista
        for (int i = 0; i < taskListView.getItems().size(); i++) {
            if (taskListView.getItems().get(i).startsWith(taskName)) {
                taskListView.getItems().set(i, status);
                return;
            }
        }
        taskListView.getItems().add(status);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}