package com.example.tap2025.vistas;

import com.example.tap2025.modelos.Producto;
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

public class CrudProductos {
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private static final String RUTA_ARCHIVO = "productos.csv";

    public void mostrar(Stage stage) {
        cargarProductosDesdeArchivo();

        TextField txtFieldNombre = new TextField();
        TextField txtFieldPrecio = new TextField();
        ComboBox<String> comboBoxCategoria = new ComboBox<>();
        comboBoxCategoria.getItems().addAll("Entradas", "Platillos", "Bebidas", "Postres");
        Button btnImagen = new Button("Elegir Imagen");
        Label lblImagen = new Label("Sin imagen");
        Button btnAgregar = new Button("Agregar Producto");
        Button btnEditar = new Button("Editar Producto");
        Button btnEliminar = new Button("Eliminar Producto");
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
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
            File fileImagen = fileChooser.showOpenDialog(stage);
            if (fileImagen != null) {
                lblImagen.setText(fileImagen.getAbsolutePath());
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
                productos.add(new Producto(nombre, precio, categoria, imagen));
                guardarProductosEnArchivo();
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
                guardarProductosEnArchivo();
            }
        });

        btnEliminar.setOnAction(event -> {
            Producto elegido = tableView.getSelectionModel().getSelectedItem();
            if (elegido != null) {
                productos.remove(elegido);
                guardarProductosEnArchivo();
            }
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

    private void guardarProductosEnArchivo() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RUTA_ARCHIVO))) {
            for (Producto p : productos) {
                writer.println(p.getNombre() + "," + p.getPrecio() + "," + p.getCantidad() + "," + p.getCategoria() + "," + p.getImagen());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar productos: " + e.getMessage());
        }
    }

    private void cargarProductosDesdeArchivo() {
        productos.clear();
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",", -1);
                if (datos.length == 5) {
                    String nombre = datos[0];
                    double precio = Double.parseDouble(datos[1]);
                    int cantidad = Integer.parseInt(datos[2]);
                    String categoria = datos[3];
                    String imagen = datos[4];
                    productos.add(new Producto(nombre, precio, cantidad, categoria, imagen));
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer productos: " + e.getMessage());
        }
    }
}
