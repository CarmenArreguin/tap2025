package com.example.tap2025.vistas;

import com.example.tap2025.modelos.Reservacion;
import com.example.tap2025.modelos.Conexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ListaReservaciones {
    private TableView<Reservacion> tableView;
    private ObservableList<Reservacion> listaReservaciones;

    public void mostrar(Stage primaryStage) {
        Stage ventanaLista = new Stage();
        ventanaLista.setTitle("Lista de Reservaciones :)");

        tableView = new TableView<>();
        listaReservaciones = FXCollections.observableArrayList();

        //Las columnas.
        TableColumn<Reservacion, String> colNombre = new TableColumn<>("Cliente");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));

        TableColumn<Reservacion, Integer> colPersonas = new TableColumn<>("Personas");
        colPersonas.setCellValueFactory(new PropertyValueFactory<>("personas"));

        TableColumn<Reservacion, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        TableColumn<Reservacion, String> colHora = new TableColumn<>("Hora");
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));

        TableColumn<Reservacion, Integer> colMesa = new TableColumn<>("Mesa");
        colMesa.setCellValueFactory(new PropertyValueFactory<>("mesa"));

        tableView.getColumns().addAll(colNombre, colPersonas, colFecha, colHora, colMesa);

        Button btnEliminar = new Button("Eliminar Reservación");
        btnEliminar.setStyle("-fx-font-size: 16px; -fx-background-color: #F44336; -fx-text-fill: white;");

        btnEliminar.setOnAction(e -> eliminarReservacion());

        cargarReservaciones();

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(tableView, btnEliminar);

        Scene escena = new Scene(root, 700, 450);
        ventanaLista.setScene(escena);
        ventanaLista.show();
    }

    private void cargarReservaciones() {
        listaReservaciones.clear();
        try {
            if (Conexion.connection == null || Conexion.connection.isClosed()) {
                Conexion.createConnection();
            }
            Statement stmt = Conexion.connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT r.id_reservacion, r.id_cliente, c.nomCte, r.personas, r.fecha, r.hora, r.mesa " +
                            "FROM reservaciones r JOIN clientes c ON r.id_cliente = c.id_cliente"
            );


            while (rs.next()) {
                listaReservaciones.add(new Reservacion(
                        rs.getInt("id_reservacion"),
                        rs.getInt("id_cliente"),
                        rs.getString("nomCte"),
                        rs.getInt("personas"),
                        rs.getString("fecha"),
                        rs.getString("hora"),
                        rs.getInt("mesa")
                ));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        tableView.setItems(listaReservaciones);
    }

    private void eliminarReservacion() {
        Reservacion seleccionada = tableView.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            try {
                if (Conexion.connection == null || Conexion.connection.isClosed()) {
                    Conexion.createConnection();
                }

                String sql = "DELETE FROM reservaciones WHERE id_reservacion = ?";
                PreparedStatement pstmt = Conexion.connection.prepareStatement(sql);
                pstmt.setInt(1, seleccionada.getIdReservacion());
                pstmt.executeUpdate();
                pstmt.close();

                listaReservaciones.remove(seleccionada);
                System.out.println("Reservación eliminada.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Atención.");
            alerta.setHeaderText(null);
            alerta.setContentText("Elige la reservación a eliminar.");
            alerta.showAndWait();
        }
    }
}
