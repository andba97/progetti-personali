package dev.andba.todolist.module;

import java.sql.Date;


import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Entity
@Table(name = "TASK")
public class Task {

    // Enum interna per rappresentare i livelli di priorità di un task
    public enum Priority { Low, Medium, High; }

    // Identificatore univoco per ogni task, con generazione automatica
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Relazione molti-a-uno con la classe TaskList: ogni task appartiene a una lista
    @ManyToOne
    @JoinColumn(name = "list_id", nullable = false) // Colonna nel DB che rappresenta la chiave esterna
    @OnDelete(action = OnDeleteAction.CASCADE)      // Quando una lista viene eliminata, i task associati vengono eliminati a cascata
    private TaskList list;

    // Descrizione testuale del task
    @Column(name = "description")
    private String description;

    // Data di scadenza del task
    @Column(name = "date")
    private Date deadLine;

    // Priorità del task (salvata come stringa nel DB, es. "Low", "Medium", "High")
    @Enumerated(EnumType.STRING)
    private Priority priority;

    // Stato di completamento del task (true = completato, false = da fare)
    @Column(name = "complete")
    private Boolean complete;

    // Costruttore vuoto richiesto da JPA per creare istanze dell'entità
    public Task() {}

    // Costruttore completo per creare un task con tutti i campi (tranne ID, che è autogenerato)
    public Task(TaskList list, String description, Date deadLine, Priority priority, Boolean complete) {
        this.list = list;
        this.description = description;
        this.deadLine = deadLine;
        this.priority = priority;
        this.complete = complete;
    }

    // Getter per l'id (non serve setter, l'id è autogenerato)
    public int getId() {
        return id;
    }

    public TaskList getList() {
        return list;
    }

    // Getter e setter per la descrizione
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter e setter per la data di scadenza
    public Date getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(Date deadLine) {
        this.deadLine = deadLine;
    }

    // Getter e setter per la priorità
    public Priority getPriority() {
        return this.priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    // Getter e setter per lo stato di completamento
    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    // Override del metodo toString per restituire la descrizione del task quando viene stampato
    @Override
    public String toString() {
        return description;
    }
}
