package com.example.tap2025.utilidades;

import com.example.tap2025.modelos.conexion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.Statement;

public class ReporteGraficas {
    public void mostrar(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));

        //Productos más vendidos.
        PieChart graficaProductos = generarGraficaProductosVendidos();

        //Ventas por día.
        LineChart<String, Number> graficaVentasDia = generarGraficaVentasPorDia();

        //Botón para el PDF.
        Button btnExportarPDF = new Button("Exportar productos más vendidos en PDF");
        btnExportarPDF.setStyle("-fx-font-size: 16px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10;");

        btnExportarPDF.setOnAction(e -> {
            ReportesPDF.generarReporteProductosMasVendidos("productos_mas_vendidos.pdf");
        });

        root.getChildren().addAll(graficaProductos, graficaVentasDia, btnExportarPDF);

        Scene escena = new Scene(root, 1000, 800);
        Stage ventanaGraficas = new Stage();
        ventanaGraficas.setTitle("Reportes Gráficos :)");
        ventanaGraficas.setScene(escena);
        ventanaGraficas.show();
    }

    private PieChart generarGraficaProductosVendidos() {
        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();

        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }
            Statement stmt = conexion.connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT producto, SUM(cantidad) AS total FROM pedidos GROUP BY producto ORDER BY total DESC LIMIT 10"
            );
            while (rs.next()) {
                datos.add(new PieChart.Data(rs.getString("producto"), rs.getInt("total")));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        PieChart pieChart = new PieChart(datos);
        pieChart.setTitle("Productos más vendidos :)");
        return pieChart;
    }

    private LineChart<String, Number> generarGraficaVentasPorDia() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

        xAxis.setLabel("Fecha");
        yAxis.setLabel("Total de ventas");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventas por Día");

        try {
            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }
            Statement stmt = conexion.connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT DATE(fecha) AS fecha, SUM(total) AS total_ventas FROM pedidos GROUP BY DATE(fecha) ORDER BY fecha DESC LIMIT 7"
            );
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("fecha"), rs.getDouble("total_ventas")));
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        lineChart.getData().add(series);
        lineChart.setTitle("Ventas por Día :)");
        return lineChart;
    }
}
