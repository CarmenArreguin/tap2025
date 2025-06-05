package com.example.tap2025.vistas;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class AgregarInsumo {

    public void mostrar(Stage stage) {
        stage.setTitle("Agregar Insumo");

        TextField tfNombre = new TextField();
        tfNombre.setPromptText("Nombre del Insumo");

        TextField tfUnidad = new TextField();
        tfUnidad.setPromptText("Unidad de Medida (e.g., kg)");

        TextField tfCantidad = new TextField();
        tfCantidad.setPromptText("Cantidad");

        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> {
            String nombre = tfNombre.getText().trim();
            String unidad = tfUnidad.getText().trim();
            int cantidad;

            try {
                cantidad = Integer.parseInt(tfCantidad.getText().trim());
            } catch (NumberFormatException ex) {
                mostrarAlerta("Error", "La cantidad debe ser un número entero.");
                return;
            }

            if (nombre.isEmpty() || unidad.isEmpty()) {
                mostrarAlerta("Error", "Completa todos los campos.");
                return;
            }

            guardarInsumo(nombre, unidad, cantidad);
            mostrarAlerta("Éxito", "¡Insumo agregado correctamente!");
            stage.close();
        });

        VBox layout = new VBox(10, tfNombre, tfUnidad, tfCantidad, btnGuardar);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 300, 200);
        stage.setScene(scene);
        stage.show();
    }

    private void guardarInsumo(String nombre, String unidadMedida, int cantidad) {
        String url = "jdbc:mysql://localhost:3306/restaurantec";
        String usuario = "admin2";
        String contraseña = "1234";

        String sql = "INSERT INTO insumos (nombre, unidad_medida, cantidad) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, unidadMedida);
            stmt.setInt(3, cantidad);

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo guardar el insumo.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
