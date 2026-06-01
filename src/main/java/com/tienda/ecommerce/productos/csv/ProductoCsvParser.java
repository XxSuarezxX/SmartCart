package com.tienda.ecommerce.productos.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.tienda.ecommerce.common.exception.ReglaNegocioException;

/**
 * Responsable ÚNICO de leer y tokenizar el CSV de productos.
 * No accede a la base de datos: convierte el archivo en filas tipadas
 * ({@link FilaProductoCsv}) y acumula los errores de formato por línea.
 * La validación de negocio (p. ej. que la categoría exista) corre por
 * cuenta de quien consuma este resultado.
 */
@Component
public class ProductoCsvParser {

    private static final int COLUMNAS_ESPERADAS = 8;

    public ResultadoParseoCsv parse(MultipartFile archivo) {
        List<FilaProductoCsv> filas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {

            String linea;
            boolean primeraLinea = true;
            int numeroLinea = 0;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (primeraLinea) {
                    primeraLinea = false; // descartamos la cabecera
                    continue;
                }

                parsearLinea(linea, numeroLinea, filas, errores);
            }
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo leer el archivo CSV: " + e.getMessage());
        }

        return new ResultadoParseoCsv(filas, errores);
    }

    private void parsearLinea(String linea, int numeroLinea,
                              List<FilaProductoCsv> filas, List<String> errores) {
        String[] datos = separarCampos(linea);
        if (datos.length < COLUMNAS_ESPERADAS) {
            errores.add("Línea " + numeroLinea + ": formato inválido (se esperaban "
                    + COLUMNAS_ESPERADAS + " columnas, se encontraron " + datos.length + ")");
            return;
        }

        try {
            FilaProductoCsv fila = new FilaProductoCsv(
                    numeroLinea,
                    datos[0].trim(),
                    datos[1].trim(),
                    Double.parseDouble(datos[2].trim()),
                    Integer.parseInt(datos[3].trim()),
                    datos[4].trim(),
                    Long.parseLong(datos[5].trim()),
                    datos[6].trim(),
                    datos[7].trim());
            filas.add(fila);
        } catch (NumberFormatException e) {
            errores.add("Línea " + numeroLinea + ": valores numéricos inválidos");
        }
    }

    /** Separa una línea CSV por comas respetando los campos entre comillas. */
    private String[] separarCampos(String linea) {
        List<String> campos = new ArrayList<>();
        boolean dentroDeComillas = false;
        StringBuilder campo = new StringBuilder();

        for (char c : linea.toCharArray()) {
            if (c == '"') {
                dentroDeComillas = !dentroDeComillas;
            } else if (c == ',' && !dentroDeComillas) {
                campos.add(campo.toString().trim());
                campo = new StringBuilder();
            } else {
                campo.append(c);
            }
        }
        campos.add(campo.toString().trim());
        return campos.toArray(new String[0]);
    }
}