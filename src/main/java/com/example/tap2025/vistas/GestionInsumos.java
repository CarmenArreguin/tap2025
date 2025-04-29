package com.example.tap2025.vistas;

import com.example.tap2025.modelos.conexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class GestionInsumos {
    private ComboBox<String> comboProductos;
    private ComboBox<String> comboInsumos;
    private TextField txtCantidad;
    private TableView<AsignacionInsumo> tableView;
    private ObservableList<AsignacionInsumo> listaAsignaciones;

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
        tableView = new TableView<>();
        listaAsignaciones = FXCollections.observableArrayList();

        cargarProductos();
        cargarInsumos();

        comboProductos.setOnAction(e -> cargarAsignaciones());

        Button btnGuardar = new Button("Guardar insumo al producto");
        btnGuardar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;");

        Button btnEliminar = new Button("Eliminar Asignación");
        btnEliminar.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 16px;");

        Label lblMensaje = new Label();

        configurarTabla();

        btnGuardar.setOnAction(e -> {
            guardarProductoInsumo();
            cargarAsignaciones();
        });
        btnEliminar.setOnAction(e -> {
            AsignacionInsumo seleccionado = tableView.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                eliminarAsignacion(seleccionado);
                cargarAsignaciones();
            }
        });

        gp.add(new Label("Producto:"), 0, 0);
        gp.add(comboProductos, 1, 0);
        gp.add(new Label("Insumo:"), 0, 1);
        gp.add(comboInsumos, 1, 1);
        gp.add(new Label("Cantidad:"), 0, 2);
        gp.add(txtCantidad, 1, 2);
        gp.add(btnGuardar, 1, 3);
        gp.add(btnEliminar, 1, 4);
        gp.add(lblMensaje, 1, 5);

        VBox root = new VBox(15, gp, tableView);
        root.setPadding(new Insets(20));

        Scene escena = new Scene(root, 800, 600);
        ventanaInsumos.setScene(escena);
        ventanaInsumos.show();
    }

    private void configurarTabla() {
        TableColumn<AsignacionInsumo, String> colInsumo = new TableColumn<>("Insumo");
        colInsumo.setCellValueFactory(new PropertyValueFactory<>("nombreInsumo"));

        TableColumn<AsignacionInsumo, Double> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        tableView.getColumns().addAll(colInsumo, colCantidad);
        tableView.setItems(listaAsignaciones);
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

    private void cargarAsignaciones() {
        listaAsignaciones.clear();
        String productoSeleccionado = comboProductos.getValue();
        if (productoSeleccionado == null) return;

        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }

            int idProducto = obtenerIdProducto(productoSeleccionado);

            String sql = "SELECT i.nombre, pi.cantidad FROM producto_insumos pi INNER JOIN insumos i ON pi.id_insumo = i.id WHERE pi.id_producto = ?";
            PreparedStatement pstmt = conexion.connection.prepareStatement(sql);
            pstmt.setInt(1, idProducto);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                listaAsignaciones.add(new AsignacionInsumo(
                        rs.getString("nombre"),
                        rs.getDouble("cantidad")
                ));
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void guardarProductoInsumo() {
        String producto = comboProductos.getValue();
        String insumo = comboInsumos.getValue();
        String cantidadStr = txtCantidad.getText();

        if (producto == null || insumo == null || cantidadStr.isEmpty()) {
            return;
        }

        try {
            double cantidad = Double.parseDouble(cantidadStr);

            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }

            int idProducto = obtenerIdProducto(producto);
            int idInsumo = obtenerIdInsumo(insumo);

            String checkSql = "SELECT * FROM producto_insumos WHERE id_producto = ? AND id_insumo = ?";
            PreparedStatement checkStmt = conexion.connection.prepareStatement(checkSql);
            checkStmt.setInt(1, idProducto);
            checkStmt.setInt(2, idInsumo);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String updateSql = "UPDATE producto_insumos SET cantidad = ? WHERE id_producto = ? AND id_insumo = ?";
                PreparedStatement updateStmt = conexion.connection.prepareStatement(updateSql);
                updateStmt.setDouble(1, cantidad);
                updateStmt.setInt(2, idProducto);
                updateStmt.setInt(3, idInsumo);
                updateStmt.executeUpdate();
                updateStmt.close();
            } else {
                String insertSql = "INSERT INTO producto_insumos (id_producto, id_insumo, cantidad) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conexion.connection.prepareStatement(insertSql);
                insertStmt.setInt(1, idProducto);
                insertStmt.setInt(2, idInsumo);
                insertStmt.setDouble(3, cantidad);
                insertStmt.executeUpdate();
                insertStmt.close();
            }
            rs.close();
            checkStmt.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void eliminarAsignacion(AsignacionInsumo asignacion) {
        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }

            int idProducto = obtenerIdProducto(comboProductos.getValue());
            int idInsumo = obtenerIdInsumo(asignacion.getNombreInsumo());

            String sql = "DELETE FROM producto_insumos WHERE id_producto = ? AND id_insumo = ?";
            PreparedStatement pstmt = conexion.connection.prepareStatement(sql);
            pstmt.setInt(1, idProducto);
            pstmt.setInt(2, idInsumo);
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

    public static class AsignacionInsumo {
        private String nombreInsumo;
        private double cantidad;

        public AsignacionInsumo(String nombreInsumo, double cantidad) {
            this.nombreInsumo = nombreInsumo;
            this.cantidad = cantidad;
        }

        public String getNombreInsumo() {
            return nombreInsumo;
        }

        public double getCantidad() {
            return cantidad;
        }
    }
}
