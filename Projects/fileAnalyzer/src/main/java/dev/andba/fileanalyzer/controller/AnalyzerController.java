package dev.andba.fileanalyzer.controller;

import dev.andba.fileanalyzer.model.FileAnalyzer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class AnalyzerController {

        @FXML
        private Text descriptionText;

        @FXML
        private CheckBox dorianGray;

        @FXML
        private CheckBox mobyDick;

        @FXML
        private CheckBox prideAndPrejudice;

        @FXML
        private CheckBox romeoAndJuliet;

        @FXML
        private ChoiceBox<String> executionType;

        @FXML
        private ListView<String> view;

        ArrayList<File> files = new ArrayList<>();
        HashMap<String,Integer> result;

        @FXML
        public void initialize(){
                executionType.getItems().addAll("single thread","one thread for file","Two thread for file");
                executionType.setValue("single thread");

        }

        // Metodo chiamato quando l'utente clicca su "analyze"
        @FXML
        public void analyze() {
                long start = System.currentTimeMillis(); // Salva il tempo iniziale per calcolare la durata
                files.clear(); // Pulisce la lista dei file selezionati
                FileAnalyzer.reset(); // Reset dello stato interno del FileAnalyzer
                StringBuilder description;

                // Se nessun file è stato selezionato, mostra un messaggio
                if (!dorianGray.isSelected() && !mobyDick.isSelected() && !prideAndPrejudice.isSelected() && !romeoAndJuliet.isSelected()) {
                        description = new StringBuilder("Nessun file selezionato");
                } else {
                        // Inizia la descrizione dei file selezionati
                        description = new StringBuilder("I file analizzati sono:");

                        // Per ogni file selezionato, lo aggiunge alla lista e lo registra in FileAnalyzer
                        if (dorianGray.isSelected()) {
                                description.append("- Dorian Gray ");
                                try {
                                        File file = new File(getClass().getResource("/dev/andba/fileanalyzer/files/Dorian Gray.txt").toURI());
                                        files.add(file);
                                        FileAnalyzer.setFile(file);
                                } catch (Exception e) { e.printStackTrace(); }
                        }
                        if (mobyDick.isSelected()) {
                                description.append("- Moby Dick ");
                                try {
                                        File file = new File(getClass().getResource("/dev/andba/fileanalyzer/files/MobyDick.txt").toURI());
                                        files.add(file);
                                        FileAnalyzer.setFile(file);
                                } catch (Exception e) { e.printStackTrace(); }
                        }
                        if (prideAndPrejudice.isSelected()) {
                                description.append("- Pride and Prejudice ");
                                try {
                                        File file = new File(getClass().getResource("/dev/andba/fileanalyzer/files/Pride and Prejudice.txt").toURI());
                                        files.add(file);
                                        FileAnalyzer.setFile(file);
                                } catch (Exception e) { e.printStackTrace(); }
                        }
                        if (romeoAndJuliet.isSelected()) {
                                description.append("- Romeo and Juliet ");
                                try {
                                        File file = new File(getClass().getResource("/dev/andba/fileanalyzer/files/Romeo and juliet.txt").toURI());
                                        files.add(file);
                                        FileAnalyzer.setFile(file);
                                } catch (Exception e) { e.printStackTrace(); }
                        }
                }

                // Configura il tipo di esecuzione (thread pool) in base alla scelta dell'utente
                ExecutorService pool;
                int threadNumber;

                if (executionType.getValue().equals("single thread")) {
                        pool = Executors.newFixedThreadPool(1);
                        threadNumber = 1;
                        FileAnalyzer.setSingleAnalysis(true); // Flag per single-threaded analysis
                } else if (executionType.getValue().equals("one thread for file")) {
                        pool = Executors.newFixedThreadPool(files.size());
                        threadNumber = files.size();
                        FileAnalyzer.setSingleAnalysis(false);
                } else {
                        pool = Executors.newFixedThreadPool(2 * files.size());
                        threadNumber = 2 * files.size();
                        FileAnalyzer.setSingleAnalysis(false);
                }

                // Prepara i task da eseguire nel thread pool
                ArrayList<Callable<Object>> threads = new ArrayList<>();

                if (FileAnalyzer.getSingleAnalysis()) {
                        Runnable analyzer = new FileAnalyzer(null, true); // Analisi singola, stesso thread
                        threads.add(Executors.callable(analyzer));
                } else {
                        // Analisi multithread: distribuisce i file tra i thread
                        for (int i = 0; i < threadNumber; i++) {
                                Runnable analyzer = new FileAnalyzer(files.get(i % files.size()), false);
                                threads.add(Executors.callable(analyzer));
                        }
                }

                // Esegue tutti i task e attende la loro conclusione
                try {
                        pool.invokeAll(threads);
                        pool.shutdown();
                } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                }

                // Recupera i risultati finali dal FileAnalyzer
                result = FileAnalyzer.getResult();

                // Estrae le 10 parole più frequenti, ordinate per occorrenza decrescente
                List<String> topWords = result.entrySet()
                        .stream()
                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                        .limit(10)
                        .map(entry -> entry.getKey() + ": " + entry.getValue())
                        .toList();

                // Visualizza le parole più frequenti nella ListView
                view.getItems().setAll(topWords);

                // Calcola il tempo totale impiegato e aggiorna il testo descrittivo
                double totalTimeSec = (System.currentTimeMillis() - start);
                String totalTime = String.format("\n TOTAL TIME: %.2f seconds", totalTimeSec);
                description.append(totalTime);
                descriptionText.setText(description.toString());
        }
}