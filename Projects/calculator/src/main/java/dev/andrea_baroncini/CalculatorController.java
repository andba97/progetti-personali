package dev.andrea_baroncini;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class CalculatorController {

    @FXML
    private Label Monitor;

    // StringBuilder per gestire il testo visualizzato sul display
    public StringBuilder monitorText = new StringBuilder();

    // Lista per memorizzare l'espressione matematica (numeri e operatori)
    public ArrayList<String> expression = new ArrayList<>();

    // Deque (doppia coda) per gestire gli operatori in base alla loro precedenza
    public Deque<String> operatorBuffer = new LinkedList<>();

    // StringBuilder per costruire il numero attualmente in input
    StringBuilder numeratorBuffer =  new StringBuilder();

    // Flag per indicare se è stata inserita una virgola (punto decimale)
    boolean comma = false;

    // Metodo chiamato quando viene premuto un pulsante sulla calcolatrice
    @FXML
    private void addSymbol(MouseEvent event) {
        Button button = (Button) event.getSource();
        String text = button.getText();
        String lastOperation = "";

        // Gestisce l'input in base al testo del pulsante premuto
        switch(text){
            // Se il pulsante è un numero
            case "0","1","2","3",
                "4","5","6","7",
                        "8","9"  -> {
                                        // Aggiunge il numero al buffer del numero corrente
                                        numeratorBuffer.append(text);
                                        // Aggiunge il numero al testo del display
                                        monitorText.append(text);
                                        // Aggiorna il display
                                        Monitor.setText(monitorText.toString());
                                    }
            // Se il pulsante è un operatore
            case "÷","+","-","x" -> {
                                        // Se il display è vuoto, non fa nulla
                                        if (monitorText.isEmpty())
                                            return;
                                        // Se l'ultimo carattere è già un operatore, lo sostituisce
                                        else if(monitorText.toString().matches(".*[+\\-x÷]$"))
                                        {
                                            monitorText.deleteCharAt(monitorText.length()-1);
                                            operatorBuffer.pop();
                                        }
                                        // Se è stata inserita una virgola senza un numero dopo, la rimuove
                                        else if(comma && !monitorText.toString().matches(".*[0-9]$")){
                                            monitorText.deleteCharAt(monitorText.length()-1);
                                            numeratorBuffer.deleteCharAt(numeratorBuffer.length()-1);
                                        }
                                        // Se il buffer del numero non è vuoto, aggiunge il numero all'espressione
                                        if(!numeratorBuffer.isEmpty())
                                            expression.add(numeratorBuffer.toString());

                                        // Gestisce la precedenza degli operatori
                                        operatorValuation(button.getText());

                                        // Aggiunge l'operatore al testo del display
                                        monitorText.append(button.getText());
                                        // Aggiorna il display
                                        Monitor.setText(monitorText.toString());
                                        // Resetta il flag della virgola e il buffer del numero
                                        comma=false;
                                        numeratorBuffer = new StringBuilder();
                                    }
            // Se il pulsante è una virgola
            case "," -> {
                            // Se l'ultimo carattere è già una virgola, non fa nulla
                            if(monitorText.toString().matches(".*[.]$"))
                                return;
                            // Se l'ultimo carattere è un operatore o il display è vuoto, aggiunge "0."
                            else if (monitorText.toString().matches(".*[+\\-x÷]$") || monitorText.isEmpty()){
                                monitorText.append("0.");
                                numeratorBuffer.append("0.");
                            }
                            // Altrimenti, aggiunge solo la virgola
                            else{
                                monitorText.append(".");
                                numeratorBuffer.append(".");
                            }

                            // Aggiorna il display e imposta il flag
                            Monitor.setText(monitorText.toString());
                            comma=true;
                        }
            // Se il pulsante è "="
            case "=" -> {
                            // Se il display è vuoto, non fa nulla
                            if (monitorText.isEmpty())
                                return;

                            String result;
                            // Se l'ultimo carattere è un operatore
                            if(monitorText.toString().matches(".*[+\\-x÷]$")){
                                // Rimuove l'ultimo operatore inserito
                                lastOperation = operatorBuffer.poll();
                                // Aggiunge tutti gli operatori rimanenti all'espressione
                                while(!operatorBuffer.isEmpty())
                                    expression.add(operatorBuffer.poll());
                                // Calcola il risultato (che viene restituito nell'espressione)
                                result=calculate();
                                // Aggiunge di nuovo il risultato e l'ultimo operatore all'espressione
                                expression.add(expression.get(0));
                                expression.add(lastOperation);
                                // Calcola nuovamente il risultato
                                result=calculate();
                            }
                            else{
                                // Aggiunge il numero corrente all'espressione
                                expression.add(numeratorBuffer.toString());
                                // Aggiunge tutti gli operatori rimanenti all'espressione
                                while(!operatorBuffer.isEmpty())
                                    expression.add(operatorBuffer.poll());
                                // Calcola il risultato
                                result=calculate();
                            }

                            // Pulisce l'espressione
                            expression.clear();
                            // Resetta il buffer del numero con il risultato
                            numeratorBuffer = new StringBuilder();
                            numeratorBuffer.append(result);

                            // Resetta il testo del display con il risultato
                            monitorText = new StringBuilder();
                            monitorText.append(result);
                            // Aggiorna il display
                            Monitor.setText(monitorText.toString());
                            // Resetta il flag della virgola
                            comma=false;    
                        }
            // Se il pulsante è "+/-"
            case "+/-"->{
                            // Se l'ultimo carattere è un numero
                            if(monitorText.toString().matches(".*[0-9]$")){
                                // Inverte il segno del numero corrente
                                double invert = Double.parseDouble(numeratorBuffer.toString());
                                invert*=-1;
                                numeratorBuffer = new StringBuilder();
                                numeratorBuffer.append(String.valueOf(invert));

                                // Trova l'indice dell'ultimo operatore
                                String temp = monitorText.toString();
                                int lastPlus = temp.lastIndexOf("+");
                                int lastMinus = temp.lastIndexOf("-");
                                int lastMul = temp.lastIndexOf("*");
                                int lastOperatorIndex = Math.max(lastPlus, Math.max(lastMinus, lastMul));

                                // Rimuove il numero corrente dal testo del display
                                monitorText.delete(lastOperatorIndex + 1, monitorText.length());
                                // Aggiunge il numero invertito
                                monitorText.append(numeratorBuffer);
                                // Aggiorna il display
                                Monitor.setText(monitorText.toString());
                            }
                            // Se l'ultimo carattere è un operatore
                            else if(monitorText.toString().matches(".*[+\\-x÷]$")){
                                // Rimuove l'ultimo operatore
                                lastOperation = operatorBuffer.poll();
                                // Aggiunge tutti gli operatori rimanenti all'espressione
                                while (!operatorBuffer.isEmpty())
                                    expression.add(operatorBuffer.poll());

                                // Calcola il risultato
                                calculate();

                                // Inverte il segno del risultato
                                double temp = Double.parseDouble(expression.get(0));
                                temp *= -1;
                                expression.add(String.valueOf(temp));
                                expression.add(lastOperation);

                                // Calcola nuovamente il risultato
                                String result = calculate();

                                // Pulisce l'espressione
                                expression.clear();
                                // Resetta il buffer del numero con il risultato
                                numeratorBuffer = new StringBuilder();
                                numeratorBuffer.append(result);

                                // Resetta il testo del display con il risultato
                                monitorText = new StringBuilder();
                                monitorText.append(result);
                                // Aggiorna il display
                                Monitor.setText(monitorText.toString());

                                // Resetta il flag della virgola
                                comma = false;
                            }
                        }
            default -> throw new IllegalArgumentException("Invalid input");
        }
    }


    // Metodo per resettare completamente la calcolatrice
    @FXML
    private void clear(MouseEvent event) {
        expression.clear();
        operatorBuffer.clear();
        numeratorBuffer = new StringBuilder();
        monitorText = new StringBuilder();
        Monitor.setText("");
        comma = false;
    }


    // Metodo per cancellare l'ultima parte dell'input
    @FXML
    private void clearLast(MouseEvent event) {
        if (operatorBuffer.isEmpty())
            monitorText.delete(0, monitorText.length());
        else
            monitorText.delete(monitorText.lastIndexOf(operatorBuffer.peek()) + 1, monitorText.length());

        Monitor.setText(monitorText.toString());
        comma = false;
        numeratorBuffer = new StringBuilder();
    }


    // Metodo per cancellare l'ultimo carattere inserito
    @FXML
    private void delete(MouseEvent event) {
        if (monitorText.isEmpty())
            return;

        if (monitorText.toString().matches(".*[+\\-x÷]$")) {
            operatorBuffer.poll();
            String elem;

            for (int i = expression.size() - 1; i >= 0; i--) {
                elem = expression.get(i);
                if (elem.matches(".*[+\\-x÷]$"))
                    operatorBuffer.add(expression.remove(i));
                else {
                    Double.parseDouble(elem);
                    numeratorBuffer.append(elem);
                    expression.remove(i);
                    break;
                }
            }

            int commaIndex = numeratorBuffer.indexOf(".", 0);
            if (commaIndex == -1)
                comma = false;
            else
                comma = true;
            monitorText.deleteCharAt(monitorText.length() - 1);
            Monitor.setText(monitorText.toString());
        } else {
            numeratorBuffer.delete(numeratorBuffer.length() - 1, numeratorBuffer.length());
            if (numeratorBuffer.length() == 0) {
                numeratorBuffer = new StringBuilder();
            } else if (monitorText.toString().matches(".*[.]$"))
                comma = false;

            monitorText.deleteCharAt(monitorText.length() - 1);
            Monitor.setText(monitorText.toString());
        }
    }


    // Metodo per gestire la precedenza degli operatori
    private void operatorValuation (String operator){

        if(operatorBuffer.isEmpty()){
            operatorBuffer.add(operator);
            return;
        }
        switch (operator) {
            case "÷","x" ->{      
                                while(!operatorBuffer.isEmpty()) {
                                    String topOperation = operatorBuffer.peekFirst();
                                    if ("x".equals(topOperation) || "÷".equals(topOperation)) {
                                        expression.add(operatorBuffer.pollFirst());
                                    }
                                    else
                                        break;
                                }
                            }
            case "+","-" ->{    
                                while(!operatorBuffer.isEmpty())
                                    expression.add(operatorBuffer.pollFirst());   
                            }
            default -> throw new IllegalArgumentException("Invalid input");
        }
        operatorBuffer.addFirst(operator);
    }


    // Metodo che calcola il risultato dell'espressione aritmetica memorizzata nella lista `expression`
    private String calculate() {
        double result;

        // Cicla su tutta la lista expression
        for (int i = 0; i < expression.size(); i++) {
            switch(expression.get(i)) {

                // Se trova l'operatore "+"
                case "+" -> {
                    // Prende gli ultimi due numeri prima del "+"
                    result = Double.parseDouble(expression.get(i - 1));
                    result += Double.parseDouble(expression.get(i - 2));

                    // Sostituisce il "+" con il risultato
                    expression.set(i, String.valueOf(result));

                    // Rimuove i due numeri usati nell'operazione
                    expression.remove(i - 1);
                    expression.remove(i - 2);

                    // Torna indietro di 2 posizioni per riallineare l'indice
                    i -= 2;
                }

                // Se trova l'operatore "-"
                case "-" -> {
                    // Prende il primo numero (a sinistra) e sottrae il secondo (a destra)
                    result = Double.parseDouble(expression.get(i - 2));
                    result -= Double.parseDouble(expression.get(i - 1));

                    // Aggiorna la posizione dell'operatore con il risultato
                    expression.set(i, String.valueOf(result));

                    // Rimuove i due numeri usati
                    expression.remove(i - 1);
                    expression.remove(i - 2);

                    // Torna indietro per rileggere correttamente la lista
                    i -= 2;
                }

                // Se trova l'operatore "÷"
                case "÷" -> {
                    // Prende i due numeri (attenzione: ordine invertito rispetto al "-"!)
                    result = Double.parseDouble(expression.get(i - 1));
                    result /= Double.parseDouble(expression.get(i - 2));

                    // Salva il risultato e rimuove i due operandi
                    expression.set(i, String.valueOf(result));
                    expression.remove(i - 1);
                    expression.remove(i - 2);

                    i -= 2;
                }

                // Se trova l'operatore "x"
                case "x" -> {
                    // Prende i due numeri da moltiplicare
                    result = Double.parseDouble(expression.get(i - 1));
                    result *= Double.parseDouble(expression.get(i - 2));

                    // Sostituisce i tre elementi con il risultato
                    expression.set(i, String.valueOf(result));
                    expression.remove(i - 1);
                    expression.remove(i - 2);

                    i -= 2;
                }
            }
        }

        // Restituisce il risultato finale come stringa (sarà l'unico elemento rimasto)
        return String.valueOf(Double.parseDouble(expression.get(0)));
    }

 
}


