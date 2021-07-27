module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens org.example to javafx.fxml;
    exports org.example;
}