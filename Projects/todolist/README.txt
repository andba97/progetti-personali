📝 JavaFX ToDo List Manager
Questa applicazione JavaFX consente di gestire più liste di attività (ToDo Lists), offrendo un’interfaccia intuitiva per creare, modificare, filtrare e organizzare i propri task.

✨ Caratteristiche principali
🗂️ Gestione Liste
Crea nuove liste di task
Rinomina o elimina liste esistenti

✅ Gestione Task
Aggiungi, modifica o rimuovi task all'interno della lista selezionata

Ogni task include:
Descrizione
Data di scadenza (Deadline)
Priorità: Low, Medium, High
Stato: Completato o Incompleto

🖥️ Interfaccia Utente
ListView laterale per la selezione delle liste
Visualizzazione dei task tramite pannelli espandibili (TitledPane) in un VBox
Controlli interattivi per modificare rapidamente data, priorità e stato di completamento

🔍 Filtri Avanzati
Filtra i task visualizzati per:
Priorità
Data di scadenza
Visualizza solo le attività rilevanti per il tuo contesto

💾 Persistenza dei Dati
Utilizzo di JPA (Java Persistence API) con EntityManager

Tutte le operazioni (creazione, modifica, cancellazione) sono sincronizzate con un database relazionale tramite transazioni automatiche

## 🛠️ Tecnologie utilizzate
-Java 17+
-JPA / Hibernate
-Maven

## Guida rapida per configurare il database del progetto ToDoList

Passo 1: Modifica il file di configurazione
Apri il file:
src/main/resources/META-INF/persistence.xml

Troverai queste proprietà:
<property name="javax.persistence.jdbc.user" value="devuser"/>
<property name="javax.persistence.jdbc.password" value="devuser"/>
Modifica devuser e la password devuser con il nome utente e la password corretti del tuo database MySQL.

Passo 2: Crea il database
Apri il terminale MySQL connettiti e crea il database eseguendo:
CREATE DATABASE todolist;

Assicurati che il nome del database nel file persistence.xml corrisponda esattamente a todolist (case sensitive).
