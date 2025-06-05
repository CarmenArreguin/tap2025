package com.example.tap2025.utilidades;

import com.example.tap2025.modelos.Conexion;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;

import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

public class ReportesPDF {
    public static void generarReporteCompleto(String rutaArchivo,
                                              LineChart<String, Number> graficaVentasDia,
                                              BarChart<String, Number> graficaEmpleadosVentas) {
        Document documento = new Document();

        try {
            PdfWriter.getInstance(documento, new FileOutputStream(rutaArchivo));
            documento.open();

            // Título del reporte
            Paragraph titulo = new Paragraph("Reporte Completo de Ventas", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24));
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            documento.add(titulo);

            // Sección 1: Productos más vendidos por día (usando el metodo directamente)
            Paragraph subTitulo1 = new Paragraph("Productos más vendidos por día de la semana", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            subTitulo1.setSpacingAfter(10);
            documento.add(subTitulo1);

            if (Conexion.connection == null || Conexion.connection.isClosed()) {
                Conexion.createConnection();
            }

            for (DayOfWeek day : DayOfWeek.values()) {
                String nombreDia = day.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                int numeroDia = day.getValue() % 7 + 1;

                Paragraph diaTitulo = new Paragraph(nombreDia.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
                diaTitulo.setAlignment(Paragraph.ALIGN_LEFT);
                documento.add(diaTitulo);

                PdfPTable tabla = new PdfPTable(2);
                tabla.setWidthPercentage(100);
                tabla.addCell(new PdfPCell(new Phrase("Producto", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));
                tabla.addCell(new PdfPCell(new Phrase("Cantidad Vendida", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));

                String sql = "SELECT producto, SUM(cantidad) AS total " +
                        "FROM pedidos " +
                        "WHERE YEARWEEK(fecha, 1) = YEARWEEK(NOW(), 1) " +
                        "AND DAYOFWEEK(fecha) = ? " +
                        "GROUP BY producto " +
                        "ORDER BY total DESC " +
                        "LIMIT 10";

                PreparedStatement pstmt = Conexion.connection.prepareStatement(sql);
                pstmt.setInt(1, numeroDia);
                ResultSet rs = pstmt.executeQuery();

                boolean tieneDatos = false;
                while (rs.next()) {
                    tabla.addCell(rs.getString("producto"));
                    tabla.addCell(String.valueOf(rs.getInt("total")));
                    tieneDatos = true;
                }

                if (!tieneDatos) {
                    PdfPCell celdaVacia = new PdfPCell(new Phrase("No hay ventas registradas"));
                    celdaVacia.setColspan(2);
                    tabla.addCell(celdaVacia);
                }

                documento.add(tabla);
                documento.add(new Paragraph(" "));

                rs.close();
                pstmt.close();
            }

            // Sección 2: Gráfica de ventas por día
            Paragraph subTitulo2 = new Paragraph("Ventas por día de la semana", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            subTitulo2.setSpacingAfter(10);
            documento.add(subTitulo2);

            WritableImage imagenVentasDia = graficaVentasDia.snapshot(null, null);
            Image imgVentasDia = Image.getInstance(SwingFXUtils.fromFXImage(imagenVentasDia, null), null);
            imgVentasDia.scaleToFit(500, 300);
            imgVentasDia.setAlignment(Element.ALIGN_CENTER);
            documento.add(imgVentasDia);
            documento.add(new Paragraph(" "));

            // Sección 3: Gráfica de empleados con más ventas por día
            Paragraph subTitulo3 = new Paragraph("Empleados con más ventas por día de la semana", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            subTitulo3.setSpacingAfter(10);
            documento.add(subTitulo3);

            WritableImage imagenEmpleados = graficaEmpleadosVentas.snapshot(null, null);
            Image imgEmpleados = Image.getInstance(SwingFXUtils.fromFXImage(imagenEmpleados, null), null);
            imgEmpleados.scaleToFit(500, 300);
            imgEmpleados.setAlignment(Element.ALIGN_CENTER);
            documento.add(imgEmpleados);

            // Tabla de datos de empleados
            PdfPTable tablaEmpleados = new PdfPTable(3);
            tablaEmpleados.setWidthPercentage(100);
            tablaEmpleados.addCell(new PdfPCell(new Phrase("Día", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));
            tablaEmpleados.addCell(new PdfPCell(new Phrase("Empleado", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));
            tablaEmpleados.addCell(new PdfPCell(new Phrase("Ventas", FontFactory.getFont(FontFactory.HELVETICA_BOLD))));

            for (XYChart.Series<String, Number> serie : graficaEmpleadosVentas.getData()) {
                String dia = serie.getName();
                for (XYChart.Data<String, Number> dato : serie.getData()) {
                    tablaEmpleados.addCell(dia);
                    tablaEmpleados.addCell(dato.getXValue());
                    tablaEmpleados.addCell(dato.getYValue().toString());
                }
            }

            documento.add(tablaEmpleados);

            documento.close();
            System.out.println("Reporte completo generado exitosamente.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void generarReporteProductosPorCategoria(String rutaArchivo) {
        Document documento = new Document();

        try {
            PdfWriter.getInstance(documento, new FileOutputStream(rutaArchivo));
            documento.open();

            Paragraph titulo = new Paragraph("Reporte: Productos por Categoría", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20));
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            documento.add(titulo);
            documento.add(new Paragraph(" "));

            if (Conexion.connection == null || Conexion.connection.isClosed()) {
                Conexion.createConnection();
            }

            Statement stmt = Conexion.connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT categoria, nombre, precio FROM productos ORDER BY categoria");

            String categoriaActual = "";
            PdfPTable tabla = null;

            while (rs.next()) {
                String categoria = rs.getString("categoria");
                if (!categoria.equals(categoriaActual)) {
                    if (tabla != null) {
                        documento.add(tabla);
                        documento.add(new Paragraph(" "));
                    }

                    categoriaActual = categoria;

                    Paragraph catTitulo = new Paragraph("Categoría: " + categoria, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
                    documento.add(catTitulo);

                    tabla = new PdfPTable(2);
                    tabla.setWidthPercentage(100);
                    tabla.addCell(new PdfPCell(new Phrase("Producto")));
                    tabla.addCell(new PdfPCell(new Phrase("Precio")));
                }

                tabla.addCell(rs.getString("nombre"));
                tabla.addCell("$" + rs.getDouble("precio"));
            }

            if (tabla != null) {
                documento.add(tabla);
            }

            rs.close();
            stmt.close();
            documento.close();

            System.out.println("Reporte de productos por categoría generado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
