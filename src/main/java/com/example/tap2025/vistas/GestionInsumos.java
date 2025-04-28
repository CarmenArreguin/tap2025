package com.example.tap2025.vistas;

import com.example.tap2025.modelos.conexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.*;

public class GestionInsumos {
    private ComboBox<String> comboProductos;
    private ComboBox<String> comboInsumos;
    private TextField txtCantidad;

    public void mostrar(Stage primaryStage) {
        Stage ventanaInsumos = new Stage();
        ventanaInsumos.setTitle("Asignar Insumos a Producto :)");

        GridPane gp = new GridPane();
        gp.setPadding(new Insets(20));
        gp.setVgap(15);
        gp.setHgap(10);

        comboProductos = new ComboBox<>();
        comboInsumos = new ComboBox<>();
        txtCantidad = new TextField();

        cargarProductos();
        cargarInsumos();

        Button btnGuardar = new Button("Guardar insumo al producto");
        btnGuardar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;");

        Label lblMensaje = new Label();

        btnGuardar.setOnAction(e -> {
            String producto = comboProductos.getValue();
            String insumo = comboInsumos.getValue();
            String cantidadStr = txtCantidad.getText();

            if (producto == null || insumo == null || cantidadStr.isEmpty()) {
                lblMensaje.setText("Todos los campos son obligatorios.");
                return;
            }

            try {
                double cantidad = Double.parseDouble(cantidadStr);
                guardarProductoInsumo(producto, insumo, cantidad);
                lblMensaje.setText("Insumo asignado correctamente :)");
            } catch (NumberFormatException ex) {
                lblMensaje.setText("Cantidad inválida.");
            }
        });

        gp.add(new Label("Producto:"), 0, 0);
        gp.add(comboProductos, 1, 0);

        gp.add(new Label("Insumo:"), 0, 1);
        gp.add(comboInsumos, 1, 1);

        gp.add(new Label("Cantidad:"), 0, 2);
        gp.add(txtCantidad, 1, 2);

        gp.add(btnGuardar, 1, 3);
        gp.add(lblMensaje, 1, 4);

        Scene escena = new Scene(gp, 500, 400);
        ventanaInsumos.setScene(escena);
        ventanaInsumos.show();
    }

    private void cargarProductos() {
        ObservableList<String> productos = FXCollections.observableArrayList();
        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }
            Statement stmt = conexion.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT nombre FROM productos");

            while (rs.next()) {
                productos.add(rs.getString("nombre"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        comboProductos.setItems(productos);
    }

    private void cargarInsumos() {
        ObservableList<String> insumos = FXCollections.observableArrayList();
        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }
            Statement stmt = conexion.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT nombre FROM insumos");

            while (rs.next()) {
                insumos.add(rs.getString("nombre"));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        comboInsumos.setItems(insumos);
    }

    private void guardarProductoInsumo(String producto, String insumo, double cantidad) {
        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }

            //Para los IDs.
            int idProducto = obtenerIdProducto(producto);
            int idInsumo = obtenerIdInsumo(insumo);

            String sql = "INSERT INTO producto_insumos (id_producto, id_insumo, cantidad) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conexion.connection.prepareStatement(sql);
            pstmt.setInt(1, idProducto);
            pstmt.setInt(2, idInsumo);
            pstmt.setDouble(3, cantidad);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int obtenerIdProducto(String nombre) throws SQLException {
        String sql = "SELECT id FROM productos WHERE nombre = ?";
        PreparedStatement pstmt = conexion.connection.prepareStatement(sql);
        pstmt.setString(1, nombre);
        ResultSet rs = pstmt.executeQuery();
        int id = 0;
        if (rs.next()) {
            id = rs.getInt("id");
        }
        rs.close();
        pstmt.close();
        return id;
    }

    private int obtenerIdInsumo(String nombre) throws SQLException {
        String sql = "SELECT id FROM insumos WHERE nombre = ?";
        PreparedStatement pstmt = conexion.connection.prepareStatement(sql);
        pstmt.setString(1, nombre);
        ResultSet rs = pstmt.executeQuery();
        int id = 0;
        if (rs.next()) {
            id = rs.getInt("id");
        }
        rs.close();
        pstmt.close();
        return id;
    }
}
