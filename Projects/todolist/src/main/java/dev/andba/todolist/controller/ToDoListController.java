package dev.andba.todolist.controller;

import dev.andba.todolist.Util.DatabaseUtil;
import dev.andba.todolist.module.TaskListStringConverter;
import dev.andba.todolist.module.Task;
import dev.andba.todolist.module.TaskList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.sql.Date;
import java.util.List;

public class ToDoListController {


    @FXML
    private ListView<TaskList> listView;
    @FXML
    private VBox titledVBox;
    @FXML
    private DatePicker filterDatePicker;
    @FXML
    private ChoiceBox<String> filterChoicheBox;

    // Liste osservabili per aggiornamenti automatici
    private ObservableList<TaskList> taskLists;
    private ObservableList<Task> tasks;
    private FilteredList<Task> filteredTasks;

    // EntityManager per operazioni JPA
    private EntityManager em;

    // Per gestione editing e numerazione liste
    private TaskList editableTaskList = null;
    private int totalList;

    @FXML
    private void initialize (){
        // Ottiene EntityManager per collegarsi al DB
        em = DatabaseUtil.getEntityManager();

        // Carica tutte le liste e i task dal DB
        List<TaskList> list = em.createQuery("FROM TaskList", TaskList.class).getResultList();
        taskLists = FXCollections.observableArrayList(list);
        totalList = taskLists.size();

        List<Task> task = em.createQuery("FROM Task", Task.class).getResultList();
        tasks = FXCollections.observableArrayList(task);

        // Mostra liste nella ListView
        listView.setItems(taskLists);
        listView.getSelectionModel().selectFirst();

        // Filtra solo i task della prima lista
        filteredTasks = new FilteredList<>(tasks, p -> true);
        TaskList firstList = listView.getSelectionModel().getSelectedItem();
        if (firstList != null) {
            filteredTasks.setPredicate(tasks -> tasks.getList().equals(firstList));
        }

        titledVBox.setFillWidth(true);

        // Crea i pannelli per ogni task
        for(Task temp : filteredTasks){
            TitledPane pane = createTitledPane(temp);
            VBox.setMargin(pane, new Insets(15, 15, 0, 15));
        }

        filterDatePicker.setEditable(false);

        // Imposta valori per la filtrazione per priorità
        ObservableList<String> priorityValues = FXCollections.observableArrayList("Low","Medium","High","All");
        filterChoicheBox.setItems(priorityValues);
        filterChoicheBox.setValue("All");
    }


    //Metodo per aggiungere una lista
    @FXML
    private void newList(){
        TaskList newTaskList = new TaskList("New list");
        totalList++;

        taskLists.add(newTaskList);
        listView.setItems(taskLists);
        listView.getSelectionModel().select(newTaskList);

        // Inizio della transazione JPA per aggiornare la lista nel database
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(newTaskList);
        tx.commit();

        renameList(); // Permette il rinomino immediato
    }


    //Metodo per eliminare una lista
    @FXML
    private void deleteList() {
        TaskList selectedList = listView.getSelectionModel().getSelectedItem(); // Prende la lista selezionata
        taskLists.remove(selectedList); // La rimuove dalla lista osservabile (aggiorna la UI)

        // Inizio della transazione JPA per aggiornare la lista nel database
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.remove(selectedList);
        tx.commit();

        listView.setItems(taskLists); // Aggiorna l'interfaccia grafica con la nuova lista
    }


    //Metodo per rinominare una lista
    @FXML
    private void renameList() {
        //Recupera la lista attualmente selezionata
        TaskList selectedList = listView.getSelectionModel().getSelectedItem();

        // Imposta la lista selezionata come quella modificabile
        editableTaskList = selectedList;

        // Crea il converter personalizzato che gestisce la modifica e il salvataggio
        TaskListStringConverter converter = new TaskListStringConverter(em, listView, totalList, selectedList);

        //Rende la ListView modificabile
        listView.setEditable(true);

        //Definisce il comportamento delle celle durante la modifica
        listView.setCellFactory(lv -> {
            TextFieldListCell<TaskList> cell = new TextFieldListCell<>(converter) {
                @Override
                public void startEdit() {
                    // Consente la modifica solo se l'elemento corrisponde a quello selezionato
                    if (getItem() == editableTaskList) {
                        super.startEdit();
                    }
                }
            };
            return cell;
        });

        // Avvia l'editing della cella della lista selezionata
        listView.edit(taskLists.indexOf(selectedList));
    }

    //Metodo per mostrare i task di una lista
    @FXML
    private void showTasks() {
        // Recupera la lista selezionata
        TaskList selectedList = listView.getSelectionModel().getSelectedItem();

        //Se nessuna lista è selezionata, termina il metodo
        if (selectedList == null)
            return;
        else {
            //Imposta il filtro per mostrare solo i task appartenenti alla lista selezionata
            filteredTasks.setPredicate(task -> task.getList().equals(selectedList));

            //Pulisce la VBox che contiene i pannelli dei task per evitare visualizzazioni duplicate
            titledVBox.getChildren().clear();

            //Applica eventuali filtri aggiuntivi e aggiorna la visualizzazione
            filterTasks();
        }
    }


    //Metodo per aggiungere un task ad una lista
    @FXML
    private void AddTask(){
        // Prendi la lista attualmente selezionata
        TaskList selectedList = listView.getSelectionModel().getSelectedItem();

        //Se nessuna lista è selezionata, esci
        if (selectedList == null)
            return;

        // Crea un nuovo task con valori di default e collegato alla lista selezionata
        Task newTask = new Task(selectedList,"New task",null, Task.Priority.Low,false);

        // Aggiungi il task alla lista globale
        tasks.add(newTask);

        // Crea il pannello grafico che rappresenta il task
        TitledPane pane = createTitledPane(newTask);

        // Aggiungi margini per un migliore layout visivo
        VBox.setMargin(pane, new Insets(15, 15, 0, 15));

        // Avvia la procedura di rinomina immediata per il nuovo task
        renameTask(newTask,pane);
    }


    //Metodo per eliminare un task da una lista
    @FXML
    private void deleteTask(Task selectedTask,TitledPane pane){
        titledVBox.getChildren().remove(pane);
        tasks.remove(selectedTask);

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.remove(selectedTask);
        tx.commit();
    }


    //Metodo per rinominare un task
    /**
     * Metodo per rinominare un task mostrato in un TitledPane.
     * Permette di modificare il titolo del task con un campo di testo editabile.
     *
     * @param selectedTask il task da rinominare
     * @param pane il TitledPane che rappresenta graficamente il task
     */
    @FXML
    private void renameTask(Task selectedTask, TitledPane pane) {

        // Ottiene il contenuto del TitledPane, che è un VBox con i controlli per data e priorità
        VBox vBox = (VBox) pane.getContent();

        // Prende il DatePicker (prima posizione) e il ChoiceBox per la priorità (seconda posizione)
        DatePicker datePicker = (DatePicker) vBox.getChildren().get(0);
        ChoiceBox<Task.Priority> priorityBox = (ChoiceBox<Task.Priority>) vBox.getChildren().get(1);

        // Disabilita temporaneamente i controlli per evitare modifiche durante la rinomina
        datePicker.setDisable(true);
        priorityBox.setDisable(true);

        // Crea un TextField precompilato con il testo corrente del titolo del pane (descrizione del task)
        TextField titleField = new TextField(pane.getText());

        // Inserisce il TextField in un HBox per sostituire temporaneamente la checkbox
        HBox titleBox = new HBox(titleField);

        // Salva la checkbox attuale (che è il graphic del TitledPane)
        CheckBox checkBox = (CheckBox) pane.getGraphic();

        // Imposta il graphic del TitledPane con il campo di testo (in modo da poter modificare il titolo)
        pane.setGraphic(titleBox);

        // Rimuove il testo del titolo (perché ora si modifica tramite il TextField)
        pane.setText("");

        // Evento al premere Invio nel TextField
        titleField.setOnAction(e -> {
            // Se il testo è vuoto, usa "Senza titolo" come fallback
            String titleText = titleField.getText().isBlank() ? "Senza titolo" : titleField.getText();

            // Imposta il testo del titolo con il nuovo valore
            pane.setText(titleText);

            // Ripristina la checkbox come graphic del TitledPane
            pane.setGraphic(checkBox);

            // Aggiorna la descrizione del task nel database
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            selectedTask.setDescription(titleText);
            em.merge(selectedTask);
            tx.commit();

            // Riabilita i controlli disabilitati
            datePicker.setDisable(false);
            priorityBox.setDisable(false);
        });

        // Richiede il focus sul campo di testo per iniziare subito la modifica
        titleField.requestFocus();
    }


    /**
     * Metodo per modificare la scadenza (deadline) di un task in base alla selezione di un DatePicker.
     * Se la nuova data selezionata è precedente alla data odierna, viene automaticamente impostata la data odierna.
     * Alla fine, aggiorna lo stato visivo del task chiamando il metodo validateTask.
     *
     * @param selectedTask il task di cui aggiornare la scadenza
     * @param datePicker il DatePicker da cui leggere la nuova data
     */
    @FXML
    private void reScheduleTask(Task selectedTask, DatePicker datePicker) {
        Date newDeadLine;

        // Ottiene la data selezionata dal DatePicker, oppure null se non selezionata
        if (datePicker.getValue() != null)
            newDeadLine = Date.valueOf(datePicker.getValue());
        else
            newDeadLine = null;

        // Se la nuova scadenza è prima di oggi, la forza a oggi (non si possono impostare deadline passate)
        if (newDeadLine != null && newDeadLine.toLocalDate().isBefore(LocalDate.now())) {
            newDeadLine = Date.valueOf(LocalDate.now());
            datePicker.setValue(LocalDate.now());
        }

        // Aggiorna la scadenza del task con la data corretta
        selectedTask.setDeadLine(newDeadLine);

        // Risale la gerarchia dei nodi partendo dal DatePicker fino a trovare il TitledPane contenitore
        Node pane = datePicker;
        while (pane != null && !(pane instanceof TitledPane)) {
            pane = pane.getParent();
        }

        // Chiama il metodo validateTask per aggiornare lo stato visivo e salvare il task
        validateTask(selectedTask, (TitledPane) pane);
    }


    //Metodo per cambiare la prioritá ad un task
    @FXML
    private void rePrioritizeTask(Task selectedTask,ChoiceBox<Task.Priority> choiceBox) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        selectedTask.setPriority((Task.Priority) choiceBox.getValue());
        em.merge(selectedTask);
        tx.commit();
    }


    /**
     * Metodo per filtrare i task visualizzati in base ai criteri selezionati dall'utente:
     * - Priorità del task (Low, Medium, High, All)
     * - Data di scadenza massima (fino a una certa data)
     *
     * Applica i filtri sulla lista di task già filtrata per lista (filteredTasks),
     * aggiorna la visualizzazione creando un TitledPane per ogni task filtrato e lo aggiunge alla VBox.
     */
    @FXML
    private void filterTasks() {
        // Primo filtro basato sulla priorità selezionata nella ChoiceBox
        FilteredList<Task> priorityFilteredList = new FilteredList<>(filteredTasks);

        String priority = filterChoicheBox.getValue();
        titledVBox.getChildren().clear();  // Pulizia dei task visualizzati prima di aggiornare

        // Se la priorità selezionata non è "All", applica il filtro per priorità
        if (!priority.equals("All"))
            priorityFilteredList.setPredicate(task -> task.getPriority().equals(Task.Priority.valueOf(priority)));

        // Crea una lista osservabile con i task filtrati per priorità
        ObservableList<Task> finalList = FXCollections.observableArrayList(priorityFilteredList);

        // Crea un ulteriore filtro per la data di scadenza, inizialmente accettando tutti i task
        FilteredList<Task> finalFilteredList = new FilteredList<>(finalList, p -> true);

        // Se è stata selezionata una data nel DatePicker, applica il filtro sulla scadenza:
        // Mostra solo i task che non hanno scadenza oltre la data selezionata (o senza scadenza)
        if (filterDatePicker.getValue() != null)
            finalFilteredList.setPredicate(task -> {
                if (task.getDeadLine() == null) {
                    return true;  // task senza scadenza sono sempre inclusi
                } else {
                    LocalDate taskDeadlineDate = task.getDeadLine().toLocalDate();
                    LocalDate filterDate = filterDatePicker.getValue();
                    return !taskDeadlineDate.isAfter(filterDate);
                }
            });

        // Per ogni task filtrato crea un TitledPane e lo aggiunge alla VBox per visualizzazione
        for (Task temp : finalFilteredList) {
            TitledPane pane = createTitledPane(temp);
            VBox.setMargin(pane, new Insets(15, 15, 0, 15));
        }
    }


    /**
     * Metodo per aggiornare lo stato di completamento di un task e modificare il colore del titolo
     * nel TitledPane associato, in base al completamento e alla scadenza del task.
     *
     * Se il task è completato, il titolo viene colorato di verde.
     * Se il task non è completato ed è scaduto (data di scadenza antecedente a oggi), il titolo diventa rosso.
     * Altrimenti, il titolo mantiene il colore bianco.
     *
     * Aggiorna anche lo stato nel database.
     *
     * @param selectedTask Il task da aggiornare
     * @param pane Il TitledPane corrispondente al task, usato per aggiornare l'interfaccia
     */
    @FXML
    private void validateTask(Task selectedTask, TitledPane pane){
        // Recupera la CheckBox associata al TitledPane (che rappresenta lo stato di completamento)
        CheckBox checkBox = (CheckBox) pane.getGraphic();

        // Cambia il colore del testo in base allo stato del task e alla sua scadenza
        if(checkBox.isSelected())
            pane.setTextFill(Color.GREEN); // Task completato: verde
        else if (selectedTask.getDeadLine() != null && selectedTask.getDeadLine().toLocalDate().isBefore(LocalDate.now()))
            pane.setTextFill(Color.RED);   // Task non completato e scaduto: rosso
        else
            pane.setTextFill(Color.WHITE); // Altrimenti bianco

        // Aggiorna lo stato del task nel database
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        selectedTask.setComplete(checkBox.isSelected());
        em.merge(selectedTask);
        tx.commit();
    }


    @FXML
    private void resetFilters() {
        filterDatePicker.setValue(null);
        filterChoicheBox.setValue("All");
    }


    /**
     * Metodo per creare un TitledPane personalizzato che rappresenta un singolo task.
     *
     * Il TitledPane contiene un DatePicker per impostare la scadenza, un ChoiceBox per la priorità,
     * una CheckBox per indicare il completamento del task, e un menu contestuale con azioni di
     * cancellazione e rinomina.
     *
     * Viene inoltre gestito il colore del titolo in base allo stato del task:
     * - Verde se completato
     * - Rosso se scaduto e non completato
     * - Bianco altrimenti
     *
     * Infine, il TitledPane viene aggiunto al VBox che contiene la lista di task visibili.
     *
     * @param selectedTask Il task da rappresentare nel TitledPane
     * @return Il TitledPane creato e configurato
     */
    private TitledPane createTitledPane(Task selectedTask){

        // Crea il DatePicker inizializzato alla scadenza del task, se presente
        DatePicker datePicker;
        if(selectedTask.getDeadLine() != null)
            datePicker = new DatePicker(selectedTask.getDeadLine().toLocalDate());
        else
            datePicker = new DatePicker(null);

        // Imposta l'evento per aggiornare la scadenza quando si seleziona una nuova data
        datePicker.setOnAction(e -> reScheduleTask(selectedTask, datePicker));

        // Crea la ChoiceBox con le priorità disponibili e imposta il valore attuale del task
        ObservableList<Task.Priority> priorityValues = FXCollections.observableArrayList(
                Task.Priority.Low, Task.Priority.Medium, Task.Priority.High
        );
        ChoiceBox<Task.Priority> priorityBox = new ChoiceBox<>(priorityValues);
        priorityBox.setValue(selectedTask.getPriority());

        // Imposta l'evento per aggiornare la priorità quando l'utente cambia la selezione
        priorityBox.setOnAction(e -> rePrioritizeTask(selectedTask, priorityBox));

        // Inserisce DatePicker e ChoiceBox in un VBox verticale
        VBox vbox = new VBox(datePicker, priorityBox);

        // Crea il TitledPane con il testo della descrizione del task e il VBox come contenuto
        TitledPane pane = new TitledPane(selectedTask.getDescription(), vbox);

        // Crea la CheckBox per indicare se il task è completato
        CheckBox checkBox = new CheckBox();
        checkBox.setOnAction(event -> validateTask(selectedTask, pane));  // Evento al cambio stato della checkbox

        // Imposta la CheckBox come grafica accanto al titolo del TitledPane
        pane.setGraphic(checkBox);

        // Crea il menu contestuale con le voci Delete e Rename
        MenuItem delete = new MenuItem("Delete");
        MenuItem rename = new MenuItem("Rename");
        ContextMenu contextMenu = new ContextMenu(delete, rename);

        // Eventi per le voci di menu: cancellazione e rinomina del task
        delete.setOnAction(e -> deleteTask(selectedTask, pane));
        rename.setOnAction(e -> renameTask(selectedTask, pane));
        pane.setContextMenu(contextMenu);

        pane.setExpanded(false);   // Il TitledPane è inizialmente chiuso

        // Imposta lo spazio verticale tra i TitledPane nel VBox che li contiene
        titledVBox.setSpacing(20);

        // Aggiunge il TitledPane creato alla lista visibile dei task
        titledVBox.getChildren().add(pane);

        // Se il task è completato, seleziona la checkbox
        if(selectedTask.getComplete())
            checkBox.setSelected(true);

        // Aggiorna il colore del titolo in base allo stato del task (completato, scaduto o normale)
        if(checkBox.isSelected())
            pane.setTextFill(Color.GREEN);
        else if (selectedTask.getDeadLine() != null && selectedTask.getDeadLine().toLocalDate().isBefore(LocalDate.now()))
            pane.setTextFill(Color.RED);
        else
            pane.setTextFill(Color.WHITE);

        return pane;
    }
}
