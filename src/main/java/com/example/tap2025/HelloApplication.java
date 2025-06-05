package com.example.tap2025;

import com.example.tap2025.modelos.Conexion;
import com.example.tap2025.vistas.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    private VBox vBox;
    private MenuBar mnbPrincipal;
    private Menu menCompetencia1, menCompetencia2;
    private MenuItem mitCalculadora, mitRestaurante, mitRompecabezas, mitHilos, mitAdmin, mitAgregarInsumo;
    private Scene escena;

    void CrearUI(){
        mitCalculadora = new MenuItem("Calculadora");
        //Se usa un objeto anonimo debido a que no vamos a necesitar que nos regresa nada, solo lo usamos.
        mitCalculadora.setOnAction(event -> new Calculadora());
        mitRestaurante = new MenuItem("Restaurante");
        //mitRestaurante.setOnAction(event -> new VentasRestaurante());
        mitRestaurante.setOnAction(event -> new VentasRestaurante().mostrar(new Stage()));
        mitRompecabezas = new MenuItem("Rompecabezas");
        mitRompecabezas.setOnAction(event -> new Rompecabezas());
        menCompetencia1 = new Menu("Competencia 1");
        menCompetencia1.getItems().addAll(mitCalculadora, mitRestaurante, mitRompecabezas);
        mitHilos = new MenuItem("Celayork");
        mitHilos.setOnAction(event -> new Celayork());
        mitAdmin = new MenuItem("Administrador");
        mitAdmin.setOnAction(event -> new LoginAdministrador().mostrar(new Stage()));
        mitAgregarInsumo = new MenuItem("Agregar Insumo");
        mitAgregarInsumo.setOnAction(event -> new AgregarInsumo().mostrar(new Stage()));
        menCompetencia2 = new Menu("Competencia 2");
        menCompetencia2.getItems().addAll(mitHilos, mitAdmin, mitAgregarInsumo);

        //No recibe parametros.
        mnbPrincipal = new MenuBar();
        mnbPrincipal.getMenus().addAll(menCompetencia1, menCompetencia2);
        vBox = new VBox(mnbPrincipal);
        escena = new Scene(vBox);
        escena.getStylesheets().add(getClass().getResource("/styles/main.css").toString());
    }

    @Override
    public void start(Stage stage) throws IOException {

        //los hilos van a segundo plano, por eso va antes de la conexión
//        new Hilo("Ruta Pinos").start();
//        new Hilo("Ruta Laureles").start();
//        new Hilo("Ruta San Juan de la Vega").start();
//        new Hilo("Ruta Monte Blanco").start();
//        new Hilo("Ruta Tenería").start();

        Conexion.createConnection();
        CrearUI();
        stage.setTitle("Hola Mundo de Eventos :)");
        stage.setScene(escena);
        /*Por medio de #new Scene# estoy aplicando lo de un objeto anónimo*/
        //stage.setScene(new Scene(vBox));
        //Es importante para que se muestre la interface.
        stage.show();
        stage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch();


    }
}

/*
package com.example.tap2025;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    //Declaramos los botones que vamos a utilizar.
private Button btnSaludo, btnSaludo2, btnSaludo3;
private VBox vBox;

@Override
public void start(Stage stage) throws IOException {
    btnSaludo = new Button("Bienvenido Amiguito :)");
    //btnSaludo.setOnAction(event -> clickEvent());
    btnSaludo2 = new Button("Bienvenido Amiguito :)");
    btnSaludo3 = new Button("Bienvenido Amiguito :)");
    vBox = new VBox(btnSaludo, btnSaludo2, btnSaludo3);
    vBox.setSpacing(10);
    //Padding es el margen del padre al objeto y Margin es del hijo al padre.
    vBox.setPadding(new Insets(10, 0, 0, 0));
    stage.setTitle("Hola Mundo de Eventos :)");
    //Por medio de #new Scene# estoy aplicando lo de un objeto anónimo.
    stage.setScene(new Scene(vBox, 200, 200));
    stage.show();
    stage.setMaximized(true);


}

public static void main(String[] args) {
    launch();

        /*
        void clickEvent(){
            System.out.println("Evento desde un metodo :)");
        }

}
 */