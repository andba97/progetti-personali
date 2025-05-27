package dev.andba.fileanalyzer.model;

import java.io.*;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class FileAnalyzer implements Runnable {

    // File specifico su cui il thread lavorerà (in modalità multi-thread)
    private final File file;

    // Indica se usare un solo thread per l'analisi di tutti i file
    static boolean singleAnalys = true;

    // Mappa che associa ogni file al suo BufferedReader
    static ConcurrentHashMap<File, BufferedReader> bufferReaders = new ConcurrentHashMap<>();

    // Mappa che associa ogni file a una mappa delle parole (parola -> conteggio)
    static ConcurrentHashMap<File, ConcurrentHashMap<String, Integer>> map = new ConcurrentHashMap<>();

    // Costruttore: assegna il file e il tipo di analisi
    public FileAnalyzer(File file, boolean multipleAnalysType) {
        this.file = file;
        this.singleAnalys = multipleAnalysType; // se true => single-threaded analysis
    }

    // Aggiunge un file da analizzare, creando il reader e la mappa parole associata
    public static void setFile(File file){
        map.put(file, new ConcurrentHashMap<String, Integer>());
        try {
            bufferReaders.put(file, new BufferedReader(new FileReader(file)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // Setter per il tipo di analisi
    public static void setSingleAnalysis(Boolean analysisType){
        singleAnalys = analysisType;
    }

    // Getter per il tipo di analisi
    public static Boolean getSingleAnalysis(){
        return singleAnalys;
    }

    // Resetta tutti i dati caricati (file e mappe)
    public static void reset(){
        bufferReaders.clear();
        map.clear();
    }

    // Restituisce una mappa globale con la somma di tutte le parole trovate nei file
    public static HashMap<String, Integer> getResult() {
        HashMap<String, Integer> result = new HashMap<>();
        for (ConcurrentHashMap<String, Integer> fileMap : map.values()) {
            for (String key : fileMap.keySet()) {
                result.merge(key, fileMap.get(key), Integer::sum); // somma le occorrenze delle stesse parole da diversi file
            }
        }
        return result;
    }

    // Metodo chiamato quando il thread parte
    @Override
    public void run() {
        if (singleAnalys)
            singleThreadAnalysis();
        else
            multipleThreadAnalysis();
    }

    // Metodo per analisi in multi-thread: ogni thread lavora su un solo file
    private void multipleThreadAnalysis() {
        String line;
        String[] words;
        ConcurrentHashMap<String, Integer> wordsCountMap = map.get(file); // mappa per il file assegnato
        BufferedReader reader = bufferReaders.get(file);
        int lineCount = 0;

        while (true) {
            // Accesso sincronizzato al reader per evitare letture concorrenti errate
            synchronized (reader) {
                try {
                    if ((line = reader.readLine()) == null) break; // fine file
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            // Estrazione parole dalla riga
            words = line.split("[\\p{Punct}\\s]+");
            for (String word : words) {
                if (word.isEmpty()) continue;                // ignora stringhe vuote
                if (word.equals(word.toUpperCase())) continue; // ignora parole tutte in maiuscolo (presumibilmente acronimi)
                wordsCountMap.merge(word.toLowerCase(), 1, Integer::sum); // conta in minuscolo
            }

            // Simula un ritardo ogni 100 righe
            lineCount++;
            if (lineCount % 100 == 0) {
                try {
                    Thread.sleep(30); // delay artificiale
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // buona pratica per interrompere correttamente un thread
                    break;
                }
            }
        }
    }

    // Metodo per analisi single-thread: uno stesso thread legge tutti i file in sequenza
    private void singleThreadAnalysis() {
        String line;
        String[] words;
        int lineCount = 0;

        // Cicla su tutti i file registrati
        for (File key : map.keySet()) {
            BufferedReader reader = bufferReaders.get(key);
            ConcurrentHashMap<String, Integer> wordsCountMap = map.get(key);

            while (true) {
                try {
                    if ((line = reader.readLine()) == null) break; // fine file
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // Analisi parole nella riga
                words = line.split("[\\p{Punct}\\s]+");
                for (String word : words) {
                    if (word.isEmpty()) continue;
                    if (word.equals(word.toUpperCase())) continue;
                    wordsCountMap.merge(word.toLowerCase(), 1, Integer::sum);
                }

                // Simula un ritardo ogni 100 righe
                lineCount++;
                if (lineCount % 100 == 0) {
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
}
