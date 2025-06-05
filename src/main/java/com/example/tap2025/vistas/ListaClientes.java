package com.example.tap2025.vistas;

import com.example.tap2025.componentes.ButtonCell;
import com.example.tap2025.modelos.ClientesDAO;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ListaClientes extends Stage {

    private ToolBar tblMenu;
    private TableView<ClientesDAO> tbvClientes;
    private VBox vBox;
    private Scene escena;
    private Button btnAgregar, btnActualizar;

    public ListaClientes() {
        CrearUI();
        this.setTitle("Listado de Clientes :)");
        this.setScene(escena);
        this.show();
    }

    private void CrearUI() {
        tbvClientes = new TableView<>();
        btnAgregar = new Button("Agregar Cliente"); //agrega una imagen, iconfinder
        //si es una inserción se tiene que mandar un parámetro null
        btnAgregar.setOnAction(event -> new Cliente(tbvClientes, null));
        //btnAgregar.setGraphic(new ImageView(getClass().getResource("/images/agregar.png").toString()));
        //ImageView imv = new ImageView(getClass().getResource("/images/211872_person_add_icon.png").toString());
        /*
        ImageView imv = new ImageView(getClass().getResource("/images/....png)
        imv.setFitWidth(20);
        imv.setFitHeight(20);
        * */

        // Botón para actualizar lista
        btnActualizar = new Button("Actualizar");
        btnActualizar.setOnAction(event -> actualizarTabla());
        //btnAgregar.setGraphic(imv);
        tblMenu = new ToolBar(btnAgregar, btnActualizar);

        CreateTable();

        vBox = new VBox(tblMenu, tbvClientes);
        escena = new Scene(vBox, 800, 600);

    }

    private void CreateTable() {
        // Limpiar columnas existentes
        tbvClientes.getColumns().clear();
        //ClientesDAO objC = new ClientesDAO();

        TableColumn<ClientesDAO, Integer> tbcId = new TableColumn<>("ID");
        tbcId.setCellValueFactory(new PropertyValueFactory<>("idCte"));

        TableColumn<ClientesDAO,String> tbcNomCte = new TableColumn<>("Nombre");
        tbcNomCte.setCellValueFactory(new PropertyValueFactory<>("nomCte"));

        TableColumn<ClientesDAO,String> tbcDireccion = new TableColumn<>("Dirección");
        tbcDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        TableColumn<ClientesDAO,String> tbcTelefono = new TableColumn<>("Telefono");
        tbcTelefono.setCellValueFactory(new PropertyValueFactory<>("telCte"));

        TableColumn<ClientesDAO,String> tbcEmail = new TableColumn<>("Email");
        tbcEmail.setCellValueFactory(new PropertyValueFactory<>("emailCte"));

        //tbvClientes.getColumns().addAll(tbcNomCte,tbcDireccion,tbcTelefono,tbcEmail);
        TableColumn<ClientesDAO, String> tbcEditar = new TableColumn<>("Editar");
        tbcEditar.setCellFactory(param -> new ButtonCell("Editar") {
            @Override
            public void handleEdit() {
                ClientesDAO cliente = getTableView().getItems().get(getIndex());
                new Cliente(tbvClientes, cliente);
            }

            @Override
            public void handleDelete() {}
        });

        TableColumn<ClientesDAO, String> tbcEliminar = new TableColumn<>("Eliminar");
        tbcEliminar.setCellFactory(param -> new ButtonCell("Eliminar") {
            @Override
            public void handleEdit() {}

            @Override
            public void handleDelete() {
                ClientesDAO cliente = getTableView().getItems().get(getIndex());
                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                confirmacion.setTitle("Confirmar eliminación");
                confirmacion.setHeaderText("¿Estás seguro de eliminar al cliente " + cliente.getNomCte() + "?");
                confirmacion.setContentText("Esta acción no se puede deshacer.");

                confirmacion.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        cliente.DELETE();
                        tbvClientes.setItems(cliente.SELECT());
                        tbvClientes.refresh();
                    }
                });
            }
        });

        // Añadir columnas a la tabla
        tbvClientes.getColumns().addAll(tbcId, tbcNomCte, tbcDireccion, tbcTelefono, tbcEmail, tbcEditar, tbcEliminar);

        // Cargar datos
        actualizarTabla();
    }

    private void actualizarTabla() {
        ClientesDAO objC = new ClientesDAO();
        tbvClientes.setItems(objC.SELECT());
        tbvClientes.refresh();
    }

    private void eliminarCliente(ClientesDAO cliente) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Estás seguro de eliminar al cliente " + cliente.getNomCte() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cliente.DELETE();
                actualizarTabla();
            }
        });
    }
}
