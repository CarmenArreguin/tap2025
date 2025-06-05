package com.example.tap2025.utilidades;

import com.example.tap2025.modelos.Producto;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketPDF {
    public static void generarTicket(String mesa, List<Producto> pedido, double total, File archivoDest, String cliente, String mesero) {
        Document documento = new Document(PageSize.A6); // Tamaño pequeño para ticket

        try {
            // Verificar y crear directorios si no existen
            archivoDest.getParentFile().mkdirs();

            PdfWriter.getInstance(documento, new FileOutputStream(archivoDest));
            documento.open();

            // Encabezado del ticket
            Paragraph titulo = new Paragraph("TICKET DE COMPRA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            documento.add(titulo);

            // Información de la venta
            String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            documento.add(new Paragraph("Fecha: " + fechaHora));
            documento.add(new Paragraph("Mesa: " + mesa));
            documento.add(new Paragraph("Cliente: " + (cliente == null || cliente.isEmpty() ? "No especificado" : cliente)));
            documento.add(new Paragraph("Mesero: " + (mesero == null || mesero.isEmpty() ? "No especificado" : mesero)));
            documento.add(new Paragraph(" ")); // Espacio en blanco

            // Tabla de productos
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);

            // Encabezados de la tabla
            agregarCeldaEncabezado(tabla, "Producto");
            agregarCeldaEncabezado(tabla, "Cantidad");
            agregarCeldaEncabezado(tabla, "Precio");
            agregarCeldaEncabezado(tabla, "Total");

            // Filas de productos
            for (Producto producto : pedido) {
                agregarCeldaNormal(tabla, producto.getNombre());
                agregarCeldaNormal(tabla, String.valueOf(producto.getCantidad()));
                agregarCeldaNormal(tabla, "$" + String.format("%.2f", producto.getPrecio()));
                agregarCeldaNormal(tabla, "$" + String.format("%.2f", producto.getTotal()));
            }

            documento.add(tabla);
            documento.add(new Paragraph(" ")); // Espacio en blanco

            // Total a pagar
            Paragraph totalPagar = new Paragraph(
                    "Total: $" + String.format("%.2f", total),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)
            );
            totalPagar.setAlignment(Paragraph.ALIGN_RIGHT);
            documento.add(totalPagar);

            documento.close();
            System.out.println("Ticket generado exitosamente: " + archivoDest.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Error al generar ticket: " + e.getMessage());
            throw new RuntimeException("Error al generar el ticket PDF", e);
        }
    }

    // Métodos auxiliares para la tabla
    private static void agregarCeldaEncabezado(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setBackgroundColor(new BaseColor(220, 220, 220));
        tabla.addCell(celda);
    }

    private static void agregarCeldaNormal(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(celda);
    }
}
