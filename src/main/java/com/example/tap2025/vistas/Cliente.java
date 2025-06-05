package com.example.tap2025.vistas;

import com.example.tap2025.modelos.ClientesDAO;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Cliente extends Stage {

    private TextField txtNomCte, txtDireccion, txtTelCte, txtEmail;
    private ClientesDAO objC;
    private TableView<ClientesDAO> tbvClientes;

    public Cliente(TableView<ClientesDAO> tbvCte, ClientesDAO obj){
        this.tbvClientes = tbvCte;
        this.objC = obj == null ? new ClientesDAO() : obj;
        CrearUI();
        //rehacemos de la línea de abajo (después de la condición).
        /*if(obj == null){
            new ClientesDAO();
        } else {
            //este objeto tiene todos los atributos.
            objC = obj;
            txtNomCte.setText(objC.getNomCte());
            txtDireccion.setText(objC.getDireccion());
            txtEmail.setText(objC.getEmailCte());
            txtTelCte.setText(objC.getTelCte());
        }*/
        //si el objeto es null, es una inserción.
        this.setTitle(obj == null ? "Nuevo Cliente" : "Editar Cliente");
        //this.setScene(new Scene(crearFormulario(), 400, 300));
        this.show();
    }

    private void CrearUI() {
        // Crear el contenedor principal
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(15);
        grid.setHgap(10);

        // Inicializar campos de texto
        txtNomCte = new TextField();
        txtDireccion = new TextField();
        txtTelCte = new TextField();
        txtEmail = new TextField();

        // Configurar tamaño preferido para los campos
        txtNomCte.setPrefWidth(250);
        txtDireccion.setPrefWidth(250);
        txtTelCte.setPrefWidth(250);
        txtEmail.setPrefWidth(250);

        // Si estamos editando, cargar los datos existentes
        if (objC.getIdCte() > 0) {
            txtNomCte.setText(objC.getNomCte());
            txtDireccion.setText(objC.getDireccion());
            txtTelCte.setText(objC.getTelCte());
            txtEmail.setText(objC.getEmailCte());
        }

        // Crear botón Guardar
        Button btnGuardar = new Button("Guardar");
        btnGuardar.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnGuardar.setOnAction(event -> guardarCliente());

        //Crear botón Cancelar
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-font-size: 14px; -fx-background-color: #f44336; -fx-text-fill: white;");
        btnCancelar.setOnAction(event -> this.close());

        //Organizar elementos en el grid
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNomCte, 1, 0);
        grid.add(new Label("Dirección:"), 0, 1);
        grid.add(txtDireccion, 1, 1);
        grid.add(new Label("Teléfono:"), 0, 2);
        grid.add(txtTelCte, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(txtEmail, 1, 3);

        //Panel para botones
        GridPane botonesPanel = new GridPane();
        botonesPanel.setHgap(10);
        botonesPanel.add(btnGuardar, 0, 0);
        botonesPanel.add(btnCancelar, 1, 0);

        grid.add(botonesPanel, 1, 4);

        //Configurar la escena
        Scene escena = new Scene(grid, 400, 250);
        this.setScene(escena);
    }

    private void guardarCliente() {
        // Validar campos obligatorios
        if (txtNomCte.getText().isEmpty() || txtTelCte.getText().isEmpty()) {
            mostrarAlerta("Campos requeridos", "Nombre y teléfono son campos obligatorios", Alert.AlertType.WARNING);
            return;
        }

        // Validar formato de teléfono (opcional)
        if (!txtTelCte.getText().matches("^[0-9\\-\\+\\s]{8,15}$")) {
            mostrarAlerta("Teléfono inválido", "Ingrese un número de teléfono válido", Alert.AlertType.WARNING);
            return;
        }

        // Validar formato de email (opcional)
        if (!txtEmail.getText().isEmpty() && !txtEmail.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            mostrarAlerta("Email inválido", "Ingrese un email válido o deje el campo vacío", Alert.AlertType.WARNING);
            return;
        }

        // Asignar valores al objeto cliente
        objC.setNomCte(txtNomCte.getText().trim());
        objC.setDireccion(txtDireccion.getText().trim());
        objC.setTelCte(txtTelCte.getText().trim());
        objC.setEmailCte(txtEmail.getText().trim());

        try {
            // Guardar en base de datos
            if (objC.getIdCte() > 0) {
                objC.UPDATE();
            } else {
                objC.INSERT();
            }

            // Actualizar la tabla de clientes
            tbvClientes.setItems(objC.SELECT());
            tbvClientes.refresh();

            // Cerrar la ventana
            this.close();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo guardar el cliente: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}