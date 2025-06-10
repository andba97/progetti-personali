module dev.andba.trismultiplayergame {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires com.fasterxml.jackson.databind;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires java.desktop;


    exports dev.andba.trismultiplayergame;
    exports dev.andba.trismultiplayergame.controller;
    exports dev.andba.trismultiplayergame.module;
    opens dev.andba.trismultiplayergame to javafx.fxml;
    opens dev.andba.trismultiplayergame.controller to javafx.fxml;
    opens dev.andba.trismultiplayergame.module to org.hibernate.orm.core, javafx.fxml;
}