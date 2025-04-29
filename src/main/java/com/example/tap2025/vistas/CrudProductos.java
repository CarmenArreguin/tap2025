package com.example.tap2025.vistas;

import com.example.tap2025.modelos.Producto;
import com.example.tap2025.modelos.conexion;
import com.example.tap2025.utilidades.ReportesPDF;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CrudProductos {
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    public void mostrar(Stage stage) {
        cargarProductosDesdeBaseDeDatos();
        TextField txtFieldNombre = new TextField();
        TextField txtFieldPrecio = new TextField();
        ComboBox<String> comboBoxCategoria = new ComboBox<>();
        comboBoxCategoria.getItems().addAll("Aperitivos", "Platillos", "Bebidas", "Postres");
        Button btnImagen = new Button("Elegir Imagen");
        Label lblImagen = new Label("Sin imagen");
        Button btnAgregar = new Button("Agregar Producto");
        Button btnEditar = new Button("Editar Producto");
        Button btnEliminar = new Button("Eliminar Producto");
        Button btnAsignarInsumos = new Button("Asignar Insumos");
        Button btnReporte = new Button("Generar Reporte PDF");

        TableView<Producto> tableView = new TableView<>();
        TableColumn<Producto, String> tblColNombre = new TableColumn<>("Nombre");
        TableColumn<Producto, Double> tblColPrecio = new TableColumn<>("Precio");
        TableColumn<Producto, Integer> tblColCantidad = new TableColumn<>("Cantidad");
        TableColumn<Producto, String> tblColCategoria = new TableColumn<>("Categoría");
        TableColumn<Producto, String> tblColImagen = new TableColumn<>("Imagen");

        tblColNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tblColPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        tblColCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        tblColCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        tblColImagen.setCellValueFactory(new PropertyValueFactory<>("imagen"));

        tableView.getColumns().addAll(tblColNombre, tblColPrecio, tblColCantidad, tblColCategoria, tblColImagen);
        tableView.setItems(productos);

        btnImagen.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Elegir Imagen");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Imágenes válidas", "*.png", "*.jpg", "*.jpeg"));
            File fileImagen = fileChooser.showOpenDialog(stage);
            if (fileImagen != null) {
                String ruta = fileImagen.getAbsolutePath().toLowerCase();
                if (ruta.endsWith(".png") || ruta.endsWith(".jpg") || ruta.endsWith(".jpeg")) {
                    lblImagen.setText(fileImagen.getAbsolutePath());
                } else {
                    Alert alerta = new Alert(Alert.AlertType.ERROR, "La imagen tiene que ser formato PNG, JPG o JPEG.");
                    alerta.showAndWait();
                }
            }
        });

        btnAgregar.setOnAction(event -> {
            try {
                String nombre = txtFieldNombre.getText();
                double precio = Double.parseDouble(txtFieldPrecio.getText());
                String categoria = comboBoxCategoria.getValue();
                String imagen = lblImagen.getText();
                if (nombre.isEmpty() || categoria == null || imagen.equals("Sin imagen")) {
                    throw new IllegalArgumentException("Faltan datos");
                }
                Producto nuevo = new Producto(nombre, precio, categoria, imagen);
                productos.add(nuevo);
                guardarProductosEnBaseDeDatos(nuevo);
                txtFieldNombre.clear();
                txtFieldPrecio.clear();
                comboBoxCategoria.setValue(null);
                lblImagen.setText("Sin imagen");
            } catch (Exception e) {
                Alert alerta = new Alert(Alert.AlertType.ERROR, "Error al agregar producto: " + e.getMessage());
                alerta.showAndWait();
            }
        });

        btnEditar.setOnAction(event -> {
            Producto elegido = tableView.getSelectionModel().getSelectedItem();
            if (elegido != null) {
                txtFieldNombre.setText(elegido.getNombre());
                txtFieldPrecio.setText(String.valueOf(elegido.getPrecio()));
                comboBoxCategoria.setValue(elegido.getCategoria());
                lblImagen.setText(elegido.getImagen());
                productos.remove(elegido);
                eliminarProductoEnBaseDeDatos(elegido); ;
            }
        });

        btnEliminar.setOnAction(event -> {
            Producto elegido = tableView.getSelectionModel().getSelectedItem();
            if (elegido != null) {
                productos.remove(elegido);
                eliminarProductoEnBaseDeDatos(elegido); ;
            }
        });

        btnAsignarInsumos.setOnAction(event -> {
            new GestionInsumos().mostrar(stage);
        });

        btnReporte.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
            File archivo = fileChooser.showSaveDialog(stage);

            if (archivo != null) {
                ReportesPDF.generarReporteProductosMasVendidos(archivo.getAbsolutePath());
            }
        });

        VBox vBoxContenido = new VBox(10,
                new Label("Nombre"), txtFieldNombre,
                new Label("Precio"), txtFieldPrecio,
                new Label("Categoría"), comboBoxCategoria,
                btnImagen, lblImagen,
                btnAgregar, btnEditar, btnEliminar,
                btnReporte
        );
        vBoxContenido.setPadding(new Insets(10));
        vBoxContenido.setStyle("-fx-background-color: #f0f8ff; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, gray, 10, 0, 0, 5);");

        VBox vBoxTabla = new VBox(tableView);
        vBoxTabla.setPadding(new Insets(15));
        vBoxTabla.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 15; -fx-background-radius: 15;");

        HBox root = new HBox(20, vBoxContenido, vBoxTabla);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #e0f7fa;");

        Scene escena = new Scene(root, 1100, 500);
        stage.setScene(escena);
        stage.setTitle("Gestionar Productos");
        stage.show();
    }

    private void guardarProductosEnBaseDeDatos(Producto p) {
        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }
            String sql = "INSERT INTO productos (nombre, precio, cantidad, categoria, imagen) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conexion.connection.prepareStatement(sql);
            pstmt.setString(1, p.getNombre());
            pstmt.setDouble(2, p.getPrecio());
            pstmt.setInt(3, p.getCantidad());
            pstmt.setString(4, p.getCategoria());
            pstmt.setString(5, p.getImagen());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void eliminarProductoEnBaseDeDatos(Producto p) {
        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }
            String sql = "DELETE FROM productos WHERE nombre = ? AND categoria = ?";
            PreparedStatement pstmt = conexion.connection.prepareStatement(sql);
            pstmt.setString(1, p.getNombre());
            pstmt.setString(2, p.getCategoria());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarProductosDesdeBaseDeDatos() {
        productos.clear();
        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }
            String sql = "SELECT * FROM productos";
            Statement stmt = conexion.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                productos.add(new Producto(
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("cantidad"),
                        rs.getString("categoria"),
                        rs.getString("imagen")
                ));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
