package com.example.tap2025.vistas;

import com.example.tap2025.modelos.Conexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class Reservaciones {
    private final Map<String, Integer> clienteMap = new HashMap<>();

    public void mostrar(Stage primaryStage) {
        Stage ventanaReservaciones = new Stage();
        ventanaReservaciones.setTitle("Nueva Reservación");

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(20));
        gp.setVgap(15);
        gp.setHgap(10);

        //ComboBox para seleccionar cliente existente
        Label lblCliente = new Label("Cliente:");
        ComboBox<String> cbClientes = new ComboBox<>();
        cargarClientes(cbClientes);

        //Opción para agregar nuevo cliente
        Button btnNuevoCliente = new Button("Nuevo Cliente");
        btnNuevoCliente.setOnAction(e -> {
            new ListaClientes();
            cargarClientes(cbClientes);
        });

        HBox clienteBox = new HBox(10, cbClientes, btnNuevoCliente);

        Label lblPersonas = new Label("Número de personas:");
        Spinner<Integer> spnPersonas = new Spinner<>(1, 20, 2);

        Label lblFecha = new Label("Fecha:");
        DatePicker datePicker = new DatePicker();

        Label lblHora = new Label("Hora (24 horas):");
        TextField txtHora = new TextField();
        txtHora.setPromptText("HH:MM");

        Label lblMesa = new Label("Mesa:");
        Spinner<Integer> spnMesa = new Spinner<>(1, 20, 1);

        Button btnGuardar = new Button("Guardar Reservación");
        btnGuardar.setStyle("-fx-font-size: 16px; -fx-background-color: #4CAF50; -fx-text-fill: white;");

        Label lblMensaje = new Label();

        btnGuardar.setOnAction(e -> {
            String clienteSeleccionado = cbClientes.getValue();
            if (clienteSeleccionado == null || clienteSeleccionado.isEmpty()) {
                lblMensaje.setText("Debe seleccionar un cliente.");
                lblMensaje.setStyle("-fx-text-fill: red;");
                return;
            }

            Integer idCliente = clienteMap.get(clienteSeleccionado);
            int personas = spnPersonas.getValue();
            String fecha = (datePicker.getValue() != null) ? datePicker.getValue().toString() : null;
            String hora = txtHora.getText();
            int mesa = spnMesa.getValue();

            if (fecha == null || hora.isEmpty()) {
                lblMensaje.setText("Debe completar todos los datos.");
                lblMensaje.setStyle("-fx-text-fill: red;");
                return;
            }

            if (!hora.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                lblMensaje.setText("Formato de hora incorrecto. Use HH:MM (24 horas).");
                lblMensaje.setStyle("-fx-text-fill: red;");
                return;
            }

            if (verificarConflictoReservacion(fecha, hora, mesa)) {
                lblMensaje.setText("Esa mesa ya está reservada en esa hora.");
                lblMensaje.setStyle("-fx-text-fill: red;");
                return;
            }

            guardarReservacion(idCliente, personas, fecha, hora, mesa);
            lblMensaje.setText("Reservación guardada exitosamente :)");
            lblMensaje.setStyle("-fx-text-fill: green;");
        });

        gp.add(lblCliente, 0, 0);
        gp.add(clienteBox, 1, 0, 2, 1);

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

        Scene escena = new Scene(gp, 500, 400);
        ventanaReservaciones.setScene(escena);
        ventanaReservaciones.show();
    }

    private void cargarClientes(ComboBox<String> cbClientes) {
        ObservableList<String> lista = FXCollections.observableArrayList();
        clienteMap.clear();
        try {
            String query = "SELECT id_cliente, nomCte FROM clientes WHERE estatus = 1";
            PreparedStatement stmt = Conexion.connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id_cliente");
                String nombre = rs.getString("nomCte");
                clienteMap.put(nombre, id);
                lista.add(nombre);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cbClientes.setItems(lista);
    }

    private boolean verificarConflictoReservacion(String fecha, String hora, int mesa) {
        try {
            String sql = "SELECT COUNT(*) FROM reservaciones WHERE fecha = ? AND hora = ? AND mesa = ?";
            PreparedStatement stmt = Conexion.connection.prepareStatement(sql);
            stmt.setString(1, fecha);
            stmt.setString(2, hora);
            stmt.setInt(3, mesa);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private void guardarReservacion(int idCliente, int personas, String fecha, String hora, int mesa) {
        String sql = "INSERT INTO reservaciones (id_cliente, personas, fecha, hora, mesa) VALUES (?, ?, ?, ?, ?)";

        try {
            if (Conexion.connection == null || Conexion.connection.isClosed()) {
                Conexion.createConnection();
            }

            PreparedStatement pstmt = Conexion.connection.prepareStatement(sql);
            pstmt.setInt(1, idCliente);
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
