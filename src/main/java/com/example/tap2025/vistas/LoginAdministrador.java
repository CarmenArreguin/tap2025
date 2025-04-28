package com.example.tap2025.vistas;

import com.example.tap2025.utilidades.ReporteGraficas;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginAdministrador {
    public void mostrar(Stage primaryStage){
        Stage stgLogin = new Stage();
        stgLogin.setTitle("Login Administrador :)");
        GridPane gp = new GridPane();
        gp.setPadding(new Insets(20));
        gp.setHgap(10);
        gp.setVgap(10);

        Label lblUsuario = new Label("Usuario: ");
        TextField txtFieldUsuario = new TextField();
        Label lblPassword = new Label("Contraseña: ");
        PasswordField pssField = new PasswordField();
        Button btnLogin = new Button("Iniciar Sesión");
        Label lblTexto = new Label();

        gp.add(lblUsuario, 0, 0);
        gp.add(txtFieldUsuario, 1, 0);
        gp.add(lblPassword, 0, 1);
        gp.add(pssField, 1, 1);
        gp.add(btnLogin, 1, 2);
        gp.add(lblTexto, 1,3);

        btnLogin.setOnAction(event -> {
            String usuario = txtFieldUsuario.getText();
            String password = pssField.getText();

            if (usuario.equals("Javi") && password.equals("1234")){
                stgLogin.close();
                mostrarMenuAdministrador(primaryStage);
            } else {
                lblTexto.setText("Datos incorrectos.");
            }
        });

        Scene escena = new Scene(gp, 300, 200);
        stgLogin.setScene(escena);
        stgLogin.show();
    }

    private void mostrarMenuAdministrador(Stage primaryStage) {
        Stage stgMenuAdmin = new Stage();
        stgMenuAdmin.setTitle("Menú Administrador :)");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));

        Button btnCrudProductos = new Button("CRUD Productos");
        btnCrudProductos.setOnAction(e -> {
            stgMenuAdmin.close();
            new CrudProductos().mostrar(primaryStage);
        });

        Button btnReservaciones = new Button("Reservaciones");
        btnReservaciones.setOnAction(e -> {
            stgMenuAdmin.close();
            new Reservaciones().mostrar(primaryStage);
        });

        Button btnReportesGraficas = new Button("Reportes y Gráficas");
        btnReportesGraficas.setOnAction(e -> {
            stgMenuAdmin.close();
            new ReporteGraficas().mostrar(primaryStage);
        });
        Button btnListaReservaciones = new Button("Ver Reservaciones");
        btnListaReservaciones.setOnAction(e -> {
            new ListaReservaciones().mostrar(primaryStage);
        });

        Button btnGestionInsumos = new Button("Gestionar Insumos");
        btnGestionInsumos.setOnAction(e -> {
            new GestionInsumos().mostrar(primaryStage);
        });

        Button btnConsultarInsumos = new Button("Consultar Insumos Producto");
        btnConsultarInsumos.setOnAction(e -> {
            new ConsultarInsumosProductos().mostrar(primaryStage);
        });

        btnCrudProductos.setStyle("-fx-font-size: 16px;");
        btnReservaciones.setStyle("-fx-font-size: 16px;");
        btnReportesGraficas.setStyle("-fx-font-size: 16px;");
        btnListaReservaciones.setStyle("-fx-font-size: 16px;");
        btnGestionInsumos.setStyle("-fx-font-size: 16px;");
        btnConsultarInsumos.setStyle("-fx-font-size: 16px;");

        root.getChildren().addAll(btnCrudProductos, btnReservaciones, btnReportesGraficas);

        Scene escena = new Scene(root, 400, 300);
        stgMenuAdmin.setScene(escena);
        stgMenuAdmin.show();
    }

}
