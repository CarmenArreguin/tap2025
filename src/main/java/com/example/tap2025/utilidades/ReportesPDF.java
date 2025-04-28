package com.example.tap2025.utilidades;

import com.example.tap2025.modelos.conexion;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReportesPDF {
    public static void generarReporteProductosMasVendidos(String rutaArchivo) {
        Document documento = new Document();

        try {
            PdfWriter.getInstance(documento, new FileOutputStream(rutaArchivo));
            documento.open();

            Paragraph titulo = new Paragraph("Reporte: Productos más vendidos :)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20));
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            documento.add(titulo);

            documento.add(new Paragraph(" ")); //Espacio en blanco

            PdfPTable tabla = new PdfPTable(2); //Para: Producto y Cantidad.
            tabla.setWidthPercentage(100);

            PdfPCell celda1 = new PdfPCell(new Phrase("Producto"));
            PdfPCell celda2 = new PdfPCell(new Phrase("Cantidad Vendida"));
            tabla.addCell(celda1);
            tabla.addCell(celda2);

            if (conexion.connection == null || conexion.connection.isClosed()) {
                conexion.createConnection();
            }
            Statement stmt = conexion.connection.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT producto, SUM(cantidad) AS total FROM pedidos GROUP BY producto ORDER BY total DESC LIMIT 10"
            );

            while (rs.next()) {
                tabla.addCell(rs.getString("producto"));
                tabla.addCell(String.valueOf(rs.getInt("total")));
            }

            rs.close();
            stmt.close();

            documento.add(tabla);

            documento.close();
            System.out.println("Reporte PDF generado exitosamente :)");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
