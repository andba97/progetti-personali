package dev.andba.todolist.module;



import jakarta.persistence.*;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task_list")
public class TaskList {

    // Chiave primaria autogenerata per ogni lista
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Nome della lista (es: "Spesa", "Lavoro", "Scuola")
    @Column(name = "name")
    private String name;

    // Relazione uno-a-molti con i task: una lista può contenere più task
    // `mappedBy = "list"` indica che il lato proprietario della relazione è il campo `list` nella classe `Task`
    // `cascade = CascadeType.ALL`: tutte le operazioni (persist, merge, remove, ecc.) sulla lista si propagano ai task
    // `orphanRemoval = true`: se un task viene rimosso dalla lista, viene eliminato anche dal database
    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasksList;

    // Costruttore vuoto necessario per JPA
    public TaskList() {}

    // Costruttore per creare una lista con nome specifico
    public TaskList(String name) {
        this.name = name;
        this.tasksList = new ArrayList<Task>(); // Inizializza la lista di task vuota
    }

    // Getter per l'ID (nessun setter, poiché è autogenerato)
    public int getId() {
        return id;
    }

    // Getter e setter per il nome della lista
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Setter per l'elenco dei task
    public void setTasksList(ObservableList<Task> tasksList) {
        this.tasksList = tasksList;
    }

    // Getter per ottenere la lista dei task
    public List<Task> getTasks() {
        return tasksList;
    }

    // Metodo per aggiungere un task alla lista
    public void AddTask(Task task) {
        tasksList.add(task);
    }

    // Metodo per rimuovere un task dalla lista
    public void DeleteTask(Task task) {
        tasksList.remove(task);
    }

    // Metodo toString: utile per la visualizzazione della lista
    @Override
    public String toString() {
        return name;
    }
}