Questa applicazione JavaFX permette di gestire più liste di task (ToDo Lists), con funzionalità per creare, modificare e filtrare le attività.

Caratteristiche principali
Gestione Liste:
Creazione, rinomina e cancellazione di liste di task.

Gestione Task:
Aggiunta, modifica, rimozione di task all’interno di una lista selezionata.

Dettagli Task:
Ogni task ha una descrizione, una data di scadenza (deadline), una priorità (Low, Medium, High) e uno stato di completamento (completo o no).

Interfaccia Utente:
Lato sinistro c’è una ListView che mostra le liste di task. A destra, un VBox mostra i task della lista selezionata come pannelli espandibili (TitledPane), con controlli per data, priorità e completamento.

Filtri:
È possibile filtrare i task mostrati per priorità e per data di scadenza, così da visualizzare solo quelli rilevanti.

L’applicazione utilizza JPA con un EntityManager per salvare e recuperare dati da un database. Ogni modifica (creazione, modifica, cancellazione) viene sincronizzata con il database tramite transazioni.

