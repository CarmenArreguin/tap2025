package com.example.tap2025.vistas;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Rompecabezas {
    private Stage stageRomp;
    private GridPane grpRomp;
    private List<ImageView> piezasRomp;
    private AtomicLong tiempoInicio;
    private int dimension;
    private Label lblTiempo;
    private Timer timer;
    private long tiempoFinalizado;
    private ImageView piezaSeleccionada;

    public Rompecabezas() {
        stageRomp = new Stage();
        stageRomp.setTitle("Rompecabezas Game :)");
        BorderPane bdpRoot = new BorderPane();
        grpRomp = new GridPane();
        grpRomp.setAlignment(Pos.CENTER);

        lblTiempo = new Label("Tiempo: 0 segundos");
        tiempoInicio = new AtomicLong(0);

        bdpRoot.setTop(lblTiempo);
        bdpRoot.setCenter(grpRomp);

        Scene escena = new Scene(bdpRoot, 600, 600);
        stageRomp.setScene(escena);
        stageRomp.show();

        elegirDimension();
    }

    private void elegirDimension() {
        Stage stageSeleccion = new Stage();
        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        stageSeleccion.setTitle("Dimensiones");
        Label lbl = new Label("Elegir tamaño del rompecabezas: ");
        Button btn3x3 = new Button("3x3");
        Button btn4x4 = new Button("4x4");
        Button btn5x5 = new Button("5x5");

        btn3x3.setOnAction(event -> comenzarJuego(3, stageSeleccion, "/images/3x3/"));
        btn4x4.setOnAction(event -> comenzarJuego(4, stageSeleccion, "/images/4x4/"));
        btn5x5.setOnAction(event -> comenzarJuego(5, stageSeleccion, "/images/5x5/"));

        vBox.getChildren().addAll(lbl, btn3x3, btn4x4, btn5x5);
        Scene escena = new Scene(vBox, 300, 200);
        stageSeleccion.setScene(escena);
        stageSeleccion.show();
    }

    private void comenzarJuego(int tam, Stage stageSeleccion, String baseRomp) {
        dimension = tam;
        stageSeleccion.close(); //cierra la ventana de selección
        imagenesRomp(baseRomp); //para cargar las piezas
        empezarTemporizador();
    }

    private void imagenesRomp(String baseRomp) {
        piezasRomp = new ArrayList<>();

        for (int i = 1; i <= dimension * dimension; i++) {
            Image img = new Image(getClass().getResourceAsStream(baseRomp + "pieza" + i + ".png"));
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(600 / dimension);
            imgView.setFitHeight(600 / dimension);
            imgView.setUserData("pieza" + i); //identificación de cada pieza
            imgView.setOnMouseClicked(event -> seleccionarPieza(imgView));
            piezasRomp.add(imgView);
        }

        Collections.shuffle(piezasRomp); //para mezclar las piezas
        actualizarGridPane(); //actualizar el grid y mostrarlas en la pantalla
    }

    private void seleccionarPieza(ImageView pieza) {
        if (piezaSeleccionada == null) {
            piezaSeleccionada = pieza;
            piezaSeleccionada.setStyle("-fx-effect: dropshadow(gaussian, red, 10, 0.5, 0, 0);"); //efecto de tipo marco
        } else {
            if (piezaSeleccionada != pieza){
                intercambiarPiezas(piezaSeleccionada, pieza);
            }
            piezaSeleccionada.setStyle("");
            piezaSeleccionada = null; //limpia la selección
        }
    }

    private void intercambiarPiezas(ImageView pieza1, ImageView pieza2) {
        int index1 = piezasRomp.indexOf(pieza1);
        int index2 = piezasRomp.indexOf(pieza2);
        Collections.swap(piezasRomp, index1, index2);
        actualizarGridPane();
        verificarRomp();
    }

    private void actualizarGridPane() {
        grpRomp.getChildren().clear();
        int index = 0;
        for (int fila = 0; fila < dimension; fila++) {
            for (int col = 0; col < dimension; col++) {
                grpRomp.add(piezasRomp.get(index), col, fila);
                index++;
            }
        }
    }

    private void empezarTemporizador() {
        tiempoInicio.set(System.currentTimeMillis());
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long tiempoTranscurrido = (System.currentTimeMillis() - tiempoInicio.get()) / 1000;
                Platform.runLater(() -> lblTiempo.setText("Tiempo: " + tiempoTranscurrido + " segundos"));
            }
        }, 0, 1000);
    }

    private void verificarRomp() {
        boolean correcto = true;
        for (int i = 0; i < piezasRomp.size(); i++) {
            if (!piezasRomp.get(i).getUserData().equals("pieza" + (i + 1))) {
                correcto = false;
                break;
            }
        }
        if (correcto) {
            if (timer != null) {
                timer.cancel();
            }
            tiempoFinalizado = (System.currentTimeMillis() - tiempoInicio.get()) / 1000;
            guardarTiempo();
            ventanaMensaje("¡Resolviste el rompecabezas en " + tiempoFinalizado + " segundos!");
        }
    }

    private void guardarTiempo() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("registro_tiempos.txt", true))) {
            writer.println("Tiempo: " + tiempoFinalizado + " segundos");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ventanaMensaje(String mensaje) {
        Stage msj = new Stage();
        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        Label lbl = new Label(mensaje);
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setOnAction(event -> msj.close());
        vBox.getChildren().addAll(lbl, btnCerrar);
        Scene escena = new Scene(vBox, 300, 150);
        msj.setScene(escena);
        msj.show();
    }
}
