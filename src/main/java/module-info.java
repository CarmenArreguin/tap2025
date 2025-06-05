module com.example.tap2025 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    opens com.example.tap2025 to javafx.fxml;
    requires org.kordamp.bootstrapfx.core;
    exports com.example.tap2025;

    requires mysql.connector.j;
    requires java.sql;
    requires java.desktop;
    requires itextpdf;
    opens com.example.tap2025.modelos;
    exports com.example.tap2025.vistas;
    opens com.example.tap2025.vistas to javafx.fxml;
}