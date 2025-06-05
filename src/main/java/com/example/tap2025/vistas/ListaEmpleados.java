package com.example.tap2025.vistas;

import com.example.tap2025.modelos.EmpleadoDAO;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ListaEmpleados extends Stage {
    private TableView<EmpleadoDAO> tableView;

    public ListaEmpleados() {
        crearUI();
        this.setTitle("Lista de Empleados");
        this.show();
    }

    private void crearUI() {
        tableView = new TableView<>();

        //Columnas
        TableColumn<EmpleadoDAO, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idEmpleado"));

        TableColumn<EmpleadoDAO, String> colNombres = new TableColumn<>("Nombres");
        colNombres.setCellValueFactory(new PropertyValueFactory<>("nombres"));

        TableColumn<EmpleadoDAO, String> colApellidos = new TableColumn<>("Apellidos");
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));

        TableColumn<EmpleadoDAO, String> colPuesto = new TableColumn<>("Puesto");
        colPuesto.setCellValueFactory(new PropertyValueFactory<>("puesto"));

        TableColumn<EmpleadoDAO, String> colCelular = new TableColumn<>("Celular");
        colCelular.setCellValueFactory(new PropertyValueFactory<>("celular"));

        Button btnAgregar = new Button("Agregar Empleado");
        btnAgregar.setOnAction(e -> new EmpleadoForm(tableView, null));

        Button btnEditar = new Button("Editar");
        btnEditar.setOnAction(e -> {
            EmpleadoDAO empSeleccionado = tableView.getSelectionModel().getSelectedItem();
            if (empSeleccionado != null) {
                new EmpleadoForm(tableView, empSeleccionado); // Abre formulario con datos
            } else {
                mostrarAlerta("Error", "Seleccione un empleado para editar.", Alert.AlertType.WARNING);
            }
        });

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> {
            EmpleadoDAO empSeleccionado = tableView.getSelectionModel().getSelectedItem();
            if (empSeleccionado != null) {
                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar (inactivar) este empleado?", ButtonType.YES, ButtonType.NO);
                confirmacion.showAndWait();
                if (confirmacion.getResult() == ButtonType.YES) {
                    empSeleccionado.setActivo(false);
                    empSeleccionado.DELETE(); // Llamará a un metodo para inactivar
                    tableView.setItems(new EmpleadoDAO().SELECT());
                }
            } else {
                mostrarAlerta("Error", "Seleccione un empleado para eliminar.", Alert.AlertType.WARNING);
            }
        });

        Button btnActualizar = new Button("Actualizar Lista");
        btnActualizar.setOnAction(e -> tableView.setItems(new EmpleadoDAO().SELECT()));

        VBox vbox = new VBox(10, btnAgregar, btnEditar, btnEliminar, btnActualizar, tableView);
        vbox.setPadding(new Insets(10));

        tableView.getColumns().addAll(colId, colNombres, colApellidos, colPuesto, colCelular);
        tableView.setItems(new EmpleadoDAO().SELECT());

        Scene scene = new Scene(vbox, 800, 600);
        this.setScene(scene);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
