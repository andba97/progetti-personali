module dev.andrea_baroncini {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens dev.andrea_baroncini to javafx.fxml;
    exports dev.andrea_baroncini;
}
