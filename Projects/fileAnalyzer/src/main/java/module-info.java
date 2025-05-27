module dev.andba.fileanalyzer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens dev.andba.fileanalyzer to javafx.fxml;
    exports dev.andba.fileanalyzer;
    exports dev.andba.fileanalyzer.controller;
    opens dev.andba.fileanalyzer.controller to javafx.fxml;
}