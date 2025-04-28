package com.example.tap2025.utilidades;

import com.example.tap2025.modelos.Producto;
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class TicketPDF {
    public static void generarTicket(String mesa, List<Producto> productos, double total, File destino) {
        Document documento = new Document(PageSize.A6); //Para que el ticket sea pequeño.

        try {
            PdfWriter.getInstance(documento, new FileOutputStream(destino));
            documento.open();

            Paragraph titulo = new Paragraph("Ticket de Compra", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            titulo.setAlignment(Paragraph.ALIGN_CENTER);
            documento.add(titulo);

            documento.add(new Paragraph("Mesa: " + mesa));
            documento.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(3);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{4, 2, 2}); //Columnas: Producto, Cantidad, Total

            tabla.addCell("Producto");
            tabla.addCell("Cant.");
            tabla.addCell("Subtotal");

            for (Producto producto : productos) {
                tabla.addCell(producto.getNombre());
                tabla.addCell(String.valueOf(producto.getCantidad()));
                tabla.addCell("$" + String.format("%.2f", producto.getTotal()));
            }

            documento.add(tabla);

            documento.add(new Paragraph(" "));
            Paragraph totalPagar = new Paragraph("Total: $" + String.format("%.2f", total), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            totalPagar.setAlignment(Paragraph.ALIGN_RIGHT);
            documento.add(totalPagar);

            documento.close();
            System.out.println("Ticket generado exitosamente: " + destino.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
