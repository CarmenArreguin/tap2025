package com.example.tap2025.vistas;

import com.example.tap2025.modelos.conexion;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.PreparedStatement;

public class Reservaciones {
    public void mostrar(Stage primaryStage) {
        Stage ventanaReservaciones = new Stage();
        ventanaReservaciones.setTitle("Nueva Reservación :)");

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(20));
        gp.setVgap(15);
        gp.setHgap(10);

        Label lblNombre = new Label("Nombre del cliente:");
        TextField txtNombre = new TextField();

        Label lblPersonas = new Label("Número de personas:");
        Spinner<Integer> spnPersonas = new Spinner<>(1, 20, 2);

        Label lblFecha = new Label("Fecha:");
        DatePicker datePicker = new DatePicker();

        Label lblHora = new Label("Hora:");
        TextField txtHora = new TextField();
        txtHora.setPromptText("HH:MM"); //La hora.

        Label lblMesa = new Label("Mesa:");
        Spinner<Integer> spnMesa = new Spinner<>(1, 20, 1);

        Button btnGuardar = new Button("Guardar Reservación");
        btnGuardar.setStyle("-fx-font-size: 16px; -fx-background-color: #4CAF50; -fx-text-fill: white;");

        Label lblMensaje = new Label();

        btnGuardar.setOnAction(e -> {
            String nombre = txtNombre.getText();
            int personas = spnPersonas.getValue();
            String fecha = (datePicker.getValue() != null) ? datePicker.getValue().toString() : null;
            String hora = txtHora.getText();
            int mesa = spnMesa.getValue();

            if (nombre.isEmpty() || fecha == null || hora.isEmpty()) {
                lblMensaje.setText("Se tienen que llenar todos los datos.");
                return;
            }

            guardarReservacion(nombre, personas, fecha, hora, mesa);
            lblMensaje.setText("Reservación guardada exitosamente :)");
        });

        gp.add(lblNombre, 0, 0);
        gp.add(txtNombre, 1, 0);

        gp.add(lblPersonas, 0, 1);
        gp.add(spnPersonas, 1, 1);

        gp.add(lblFecha, 0, 2);
        gp.add(datePicker, 1, 2);

        gp.add(lblHora, 0, 3);
        gp.add(txtHora, 1, 3);

        gp.add(lblMesa, 0, 4);
        gp.add(spnMesa, 1, 4);

        gp.add(btnGuardar, 1, 5);
        gp.add(lblMensaje, 1, 6);

        Scene escena = new Scene(gp, 400, 400);
        ventanaReservaciones.setScene(escena);
        ventanaReservaciones.show();
    }

    private void guardarReservacion(String nombre, int personas, String fecha, String hora, int mesa) {
        String sql = "INSERT INTO reservaciones (nombre_cliente, personas, fecha, hora, mesa) VALUES (?, ?, ?, ?, ?)";

        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }

            PreparedStatement pstmt = conexion.connection.prepareStatement(sql);
            pstmt.setString(1, nombre);
            pstmt.setInt(2, personas);
            pstmt.setString(3, fecha);
            pstmt.setString(4, hora);
            pstmt.setInt(5, mesa);
            pstmt.executeUpdate();

            pstmt.close();
            System.out.println("Reservación guardada en la base de datos.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
