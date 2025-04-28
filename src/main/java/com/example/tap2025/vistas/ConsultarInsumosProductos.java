package com.example.tap2025.vistas;

import com.example.tap2025.modelos.conexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConsultarInsumosProductos {
    private ComboBox<String> comboProductos;
    private TableView<InsumoProducto> tableView;
    private ObservableList<InsumoProducto> listaInsumos;

    public void mostrar(Stage primaryStage) {
        Stage ventanaConsultar = new Stage();
        ventanaConsultar.setTitle("Consultar Insumos de Producto :)");

        comboProductos = new ComboBox<>();
        cargarProductos();

        Button btnConsultar = new Button("Consultar Insumos");
        btnConsultar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px;");

        btnConsultar.setOnAction(e -> consultarInsumos());

        tableView = new TableView<>();
        listaInsumos = FXCollections.observableArrayList();

        TableColumn<InsumoProducto, String> colInsumo = new TableColumn<>("Insumo");
        colInsumo.setCellValueFactory(new PropertyValueFactory<>("nombreInsumo"));

        TableColumn<InsumoProducto, Double> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        TableColumn<InsumoProducto, String> colUnidad = new TableColumn<>("Unidad de Medida");
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidadMedida"));

        tableView.getColumns().addAll(colInsumo, colCantidad, colUnidad);

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(comboProductos, btnConsultar, tableView);

        Scene escena = new Scene(root, 700, 500);
        ventanaConsultar.setScene(escena);
        ventanaConsultar.show();
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

    private void consultarInsumos() {
        String productoSeleccionado = comboProductos.getValue();
        if (productoSeleccionado == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Advertencia.");
            alerta.setHeaderText(null);
            alerta.setContentText("Elige un producto.");
            alerta.showAndWait();
            return;
        }

        listaInsumos.clear();

        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }

            int idProducto = obtenerIdProducto(productoSeleccionado);

            String sql = "SELECT i.nombre, pi.cantidad, i.unidad_medida " +
                    "FROM producto_insumos pi " +
                    "INNER JOIN insumos i ON pi.id_insumo = i.id " +
                    "WHERE pi.id_producto = ?";

            PreparedStatement pstmt = conexion.connection.prepareStatement(sql);
            pstmt.setInt(1, idProducto);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                listaInsumos.add(new InsumoProducto(
                        rs.getString("nombre"),
                        rs.getDouble("cantidad"),
                        rs.getString("unidad_medida")
                ));
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tableView.setItems(listaInsumos);
    }

    private int obtenerIdProducto(String nombre) throws Exception {
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

    //Esta es la clase interna para el modelo.
    public static class InsumoProducto {
        private String nombreInsumo;
        private double cantidad;
        private String unidadMedida;

        public InsumoProducto(String nombreInsumo, double cantidad, String unidadMedida) {
            this.nombreInsumo = nombreInsumo;
            this.cantidad = cantidad;
            this.unidadMedida = unidadMedida;
        }

        public String getNombreInsumo() {
            return nombreInsumo;
        }

        public double getCantidad() {
            return cantidad;
        }

        public String getUnidadMedida() {
            return unidadMedida;
        }
    }
}
