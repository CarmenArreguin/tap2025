package com.example.tap2025.vistas;

import com.example.tap2025.modelos.EmpleadoDAO;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;

public class EmpleadoForm extends Stage {
    private TextField txtNombres, txtApellidos, txtCurp, txtRfc, txtCelular, txtNss, txtPassword;
    private TextField txtSueldo;
    private ComboBox<String> cbPuesto;
    private DatePicker dpFechaIngreso;
    private EmpleadoDAO empleado;
    private TableView<EmpleadoDAO> tableView;

    public EmpleadoForm(TableView<EmpleadoDAO> tableView, EmpleadoDAO empleado) {
        this.tableView = tableView;
        this.empleado = empleado == null ? new EmpleadoDAO() : empleado;

        crearUI();
        this.setTitle(empleado == null ? "Nuevo Empleado" : "Editar Empleado");
        this.show();
    }

    private void crearUI() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(15);
        grid.setHgap(10);

        // Campos del formulario
        txtNombres = new TextField();
        txtApellidos = new TextField();
        txtCurp = new TextField();
        txtRfc = new TextField();
        txtSueldo = new TextField();
        txtCelular = new TextField();
        txtNss = new TextField();
        txtPassword = new TextField();

        // Limitar RFC a 20 caracteres
        txtRfc.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > 20) {
                txtRfc.setText(newText.substring(0, 20));
            }
        });

        cbPuesto = new ComboBox<>();
        cbPuesto.getItems().addAll("Mesero", "Gerente", "Administrador", "Cocinero", "Limpieza", "Barra");
        cbPuesto.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            txtPassword.setDisable(!newVal.equals("Mesero") && !newVal.equals("Gerente") && !newVal.equals("Administrador"));
        });

        dpFechaIngreso = new DatePicker(LocalDate.now());

        // Si estamos editando, cargar datos
        if (empleado.getIdEmpleado() > 0) {
            txtNombres.setText(empleado.getNombres());
            txtApellidos.setText(empleado.getApellidos());
            txtCurp.setText(empleado.getCurp());
            txtRfc.setText(empleado.getRfc());
            txtSueldo.setText(String.valueOf(empleado.getSueldo()));
            cbPuesto.setValue(empleado.getPuesto());
            txtCelular.setText(empleado.getCelular());
            txtNss.setText(empleado.getNss());
            dpFechaIngreso.setValue(empleado.getFechaIngreso().toLocalDate());
            txtPassword.setText(empleado.getPassword());
        }

        Button btnGuardar = new Button("Guardar");
        btnGuardar.setOnAction(e -> guardarEmpleado());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setOnAction(e -> this.close());

        // Organizar en grid
        grid.add(new Label("Nombres:"), 0, 0);
        grid.add(txtNombres, 1, 0);
        grid.add(new Label("Apellidos:"), 0, 1);
        grid.add(txtApellidos, 1, 1);
        grid.add(new Label("CURP:"), 0, 2);
        grid.add(txtCurp, 1, 2);
        grid.add(new Label("RFC:"), 0, 3);
        grid.add(txtRfc, 1, 3);
        grid.add(new Label("Sueldo (día):"), 0, 4);
        grid.add(txtSueldo, 1, 4);
        grid.add(new Label("Puesto:"), 0, 5);
        grid.add(cbPuesto, 1, 5);
        grid.add(new Label("Celular:"), 0, 6);
        grid.add(txtCelular, 1, 6);
        grid.add(new Label("NSS:"), 0, 7);
        grid.add(txtNss, 1, 7);
        grid.add(new Label("Fecha Ingreso:"), 0, 8);
        grid.add(dpFechaIngreso, 1, 8);
        grid.add(new Label("Contraseña:"), 0, 9);
        grid.add(txtPassword, 1, 9);

        GridPane botones = new GridPane();
        botones.setHgap(10);
        botones.add(btnGuardar, 0, 0);
        botones.add(btnCancelar, 1, 0);
        grid.add(botones, 1, 10);

        Scene scene = new Scene(grid, 500, 500);
        this.setScene(scene);
    }

    private void guardarEmpleado() {
        // Validaciones
        if (txtNombres.getText().isEmpty() || txtApellidos.getText().isEmpty() ||
                cbPuesto.getValue() == null || dpFechaIngreso.getValue() == null) {
            mostrarAlerta("Error", "Los campos obligatorios deben llenarse", Alert.AlertType.ERROR);
            return;
        }

        // Asegurar que RFC no exceda 20 caracteres
        String rfcFinal = txtRfc.getText();
        if (rfcFinal.length() > 20) {
            rfcFinal = rfcFinal.substring(0, 20);
        }

        // Asignar valores
        empleado.setNombres(txtNombres.getText());
        empleado.setApellidos(txtApellidos.getText());
        empleado.setCurp(txtCurp.getText());
        empleado.setRfc(rfcFinal);
        empleado.setSueldo(Double.parseDouble(txtSueldo.getText()));
        empleado.setPuesto(cbPuesto.getValue());
        empleado.setCelular(txtCelular.getText());
        empleado.setNss(txtNss.getText());
        empleado.setFechaIngreso(Date.valueOf(dpFechaIngreso.getValue()));
        empleado.setPassword(cbPuesto.getValue().equals("Mesero") ||
                cbPuesto.getValue().equals("Gerente") ||
                cbPuesto.getValue().equals("Administrador") ? txtPassword.getText() : null);

        // Guardar en BD
        if (empleado.getIdEmpleado() > 0) {
            empleado.UPDATE();
        } else {
            empleado.INSERT();
        }

        // Actualizar tabla
        tableView.setItems(empleado.SELECT());
        tableView.refresh();

        this.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
