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

