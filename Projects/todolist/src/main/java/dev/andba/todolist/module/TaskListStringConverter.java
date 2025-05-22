package dev.andba.todolist.module;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import javafx.scene.control.ListView;
import javafx.util.StringConverter;

// La classe estende StringConverter per convertire TaskList <-> String
public class TaskListStringConverter extends StringConverter<TaskList> {

    // EntityManager per interagire con il database (JPA)
    private final EntityManager em;

    // La ListView che mostra le TaskList
    private final ListView<TaskList> listView;

    // Numero totale delle liste (usato per assegnare un nome di default)
    private final int totalList;

    // Riferimento alla lista modificabile
    private final TaskList editableTaskList;

    // Costruttore: riceve EntityManager, ListView, numero di liste e la lista modificabile
    public TaskListStringConverter(EntityManager em, ListView<TaskList> listView, int totalList, TaskList editableTaskList) {
        this.em = em;
        this.listView = listView;
        this.totalList = totalList;
        this.editableTaskList = editableTaskList;
    }

    // Metodo per convertire un oggetto TaskList in una stringa da visualizzare nella ListView
    @Override
    public String toString(TaskList taskList) {
        if(taskList != null)
            return taskList.getName();  // Mostra il nome della lista
        else
            return "";  // Lista nulla = stringa vuota
    }

    // Metodo per convertire una stringa (digitata dall’utente) in una TaskList aggiornata
    @Override
    public TaskList fromString(String string) {
        // Ottiene l’elemento selezionato dalla ListView
        TaskList selected = listView.getSelectionModel().getSelectedItem();

        // Se è stato selezionato qualcosa ed è proprio la lista in fase di editing
        if (selected != null && selected == editableTaskList) {

            // Se l'utente non ha scritto nulla, assegna un nome di default
            if (string.isBlank()) {
                selected.setName("List id:" + totalList);
            } else {
                selected.setName(string);  // Altrimenti, usa il testo inserito
            }

            // Inizio della transazione JPA per aggiornare la lista nel database
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.merge(selected);
            tx.commit();

            // Disattiva l'editing nella ListView
            listView.setEditable(false);
        }

        // Ritorna l’oggetto aggiornato (o null se non selezionato)
        return selected;
    }
}