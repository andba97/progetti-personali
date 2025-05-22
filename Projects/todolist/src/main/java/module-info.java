module dev.andba.todolist {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;

    exports dev.andba.todolist;
    exports dev.andba.todolist.controller;

    opens dev.andba.todolist to javafx.fxml;
    opens dev.andba.todolist.controller to javafx.fxml;
    opens dev.andba.todolist.module to org.hibernate.orm.core, javafx.fxml;
}