package com.tienda.ecommerce.pago.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tienda.ecommerce.carrito.model.Carrito;
import com.tienda.ecommerce.pago.model.Pago;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReciboPdfService {

    private static final DateTimeFormatter FECHA_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Value("${smartcart.mail.nombre-tienda:SmartCart}")
    private String nombreTienda;

    public byte[] generarRecibo(Pago pago, List<Carrito> items) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(documento, out);
            documento.open();

            agregarEncabezado(documento, pago);
            agregarDatosCliente(documento, pago);
            agregarTablaProductos(documento, items);
            agregarTotal(documento, pago);
            agregarPiePagina(documento);

            documento.close();
            return out.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new RuntimeException("Error generando el recibo PDF", e);
        }
    }

    private void agregarEncabezado(Document doc, Pago pago) throws DocumentException {
        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(33, 37, 41));
        Paragraph titulo = new Paragraph(nombreTienda, tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
        Paragraph sub = new Paragraph("Recibo de compra", subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(15f);
        doc.add(sub);

        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Paragraph numero = new Paragraph("Recibo N°: " + pago.getId(), infoFont);
        Paragraph fecha = new Paragraph(
                "Fecha: " + (pago.getFechaPago() != null ? pago.getFechaPago().format(FECHA_FMT) : ""),
                infoFont);
        Paragraph estado = new Paragraph("Estado: " + pago.getEstado(), infoFont);
        estado.setSpacingAfter(15f);
        doc.add(numero);
        doc.add(fecha);
        doc.add(estado);
    }

    private void agregarDatosCliente(Document doc, Pago pago) throws DocumentException {
        Font tituloSec = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Paragraph t = new Paragraph("Cliente", tituloSec);
        t.setSpacingAfter(5f);
        doc.add(t);

        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);
        doc.add(new Paragraph("Nombre: " + pago.getUsuario().getNombre(), normal));
        Paragraph correo = new Paragraph("Correo: " + pago.getUsuario().getEmail(), normal);
        correo.setSpacingAfter(15f);
        doc.add(correo);
    }

    private void agregarTablaProductos(Document doc, List<Carrito> items) throws DocumentException {
        PdfPTable tabla = new PdfPTable(new float[]{4f, 1.2f, 1.6f, 1.8f});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(5f);
        tabla.setSpacingAfter(10f);

        agregarCeldaCabecera(tabla, "Producto");
        agregarCeldaCabecera(tabla, "Cant.");
        agregarCeldaCabecera(tabla, "P. Unit.");
        agregarCeldaCabecera(tabla, "Subtotal");

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        for (Carrito item : items) {
            double subtotal = item.getProducto().getPrecio() * item.getCantidad();
            agregarCelda(tabla, item.getProducto().getNombre(), cellFont, Element.ALIGN_LEFT);
            agregarCelda(tabla, String.valueOf(item.getCantidad()), cellFont, Element.ALIGN_CENTER);
            agregarCelda(tabla, formatoMoneda(item.getProducto().getPrecio()), cellFont, Element.ALIGN_RIGHT);
            agregarCelda(tabla, formatoMoneda(subtotal), cellFont, Element.ALIGN_RIGHT);
        }
        doc.add(tabla);
    }

    private void agregarCeldaCabecera(PdfPTable tabla, String texto) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setBackgroundColor(new Color(33, 37, 41));
        celda.setPadding(8f);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(celda);
    }

    private void agregarCelda(PdfPTable tabla, String texto, Font font, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setPadding(6f);
        celda.setHorizontalAlignment(alineacion);
        tabla.addCell(celda);
    }

    private void agregarTotal(Document doc, Pago pago) throws DocumentException {
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph total = new Paragraph("TOTAL: " + formatoMoneda(pago.getMontoTotal()), totalFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingAfter(20f);
        doc.add(total);
    }

    private void agregarPiePagina(Document doc) throws DocumentException {
        Font pie = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY);
        Paragraph mensaje = new Paragraph(
                "¡Gracias por tu compra en " + nombreTienda + "!\n" +
                "Este es un comprobante generado automáticamente.", pie);
        mensaje.setAlignment(Element.ALIGN_CENTER);
        doc.add(mensaje);
    }

    private String formatoMoneda(Double valor) {
        if (valor == null) return "$0.00";
        return String.format("$%,.2f", valor);
    }
}
