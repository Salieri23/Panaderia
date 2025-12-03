package com.panaderia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class InventarioVistaController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Ruta donde se guardarán las imágenes (definida en application.properties)
    @Value("${app.upload.dir}")
    private String uploadDir;

    // Vista principal del inventario (AHORA CARGA LA COLUMNA imagen_url)
    @GetMapping("/inventario")
    public String mostrarInventario(Model model) {

        String sql = """
            SELECT 
                p.id_producto,
                p.nombre,
                p.categoria,
                p.precio_base,
                i.cantidad,
                p.unidad_medida,
                i.ultima_actualizacion,
                p.imagen_url -- AÑADIMOS ESTA COLUMNA
            FROM inventario i
            INNER JOIN producto p ON i.id_producto = p.id_producto
            ORDER BY p.id_producto;
        """;

        try {
            List<Map<String, Object>> inventario = jdbcTemplate.queryForList(sql);
            model.addAttribute("inventario", inventario);

        } catch (Exception e) {
            model.addAttribute("inventario", List.of());
            System.out.println("Error al cargar inventario: " + e.getMessage());
        }

        return "inventario";
    }

  // Guardar un nuevo producto (LÓGICA CORREGIDA)
@PostMapping("/inventario/guardar")
public String guardarProducto(
        @RequestParam String nombre,
        @RequestParam String categoria,
        @RequestParam int cantidad,
        @RequestParam String unidad_medida,
        @RequestParam BigDecimal precio_base,
        @RequestParam(value = "file", required = false) MultipartFile file
) {
    String nombreArchivo = null;
    // CORRECCIÓN: Se ejecuta solo si el archivo TIENE contenido
    if (file != null && !file.isEmpty()) {
        nombreArchivo = guardarImagen(file);
    }

    try {
        String sqlProducto = """
            INSERT INTO producto (nombre, categoria, unidad_medida, precio_base, imagen_url)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id_producto;
            """;

        Integer idProducto = jdbcTemplate.queryForObject(
                sqlProducto,
                Integer.class,
                nombre, categoria, unidad_medida, precio_base, nombreArchivo
        );

        String sqlInventario = """
            INSERT INTO inventario (id_producto, cantidad, ultima_actualizacion)
            VALUES (?, ?, NOW());
            """;

        jdbcTemplate.update(sqlInventario, idProducto, cantidad);

    } catch (Exception e) {
        System.err.println("Error al guardar producto: " + e.getMessage());
    }

    return "redirect:/inventario";
}

// Actualizar producto existente (LÓGICA CORREGIDA)
@PostMapping("/inventario/actualizar")
public String actualizarProducto(
        @RequestParam int id_producto,
        @RequestParam String nombre,
        @RequestParam String categoria,
        @RequestParam BigDecimal precio_base,
        @RequestParam int cantidad,
        @RequestParam String unidad_medida,
        @RequestParam(value = "file", required = false) MultipartFile file
) {
    String nombreArchivo = null;
    // CORRECCIÓN: Se ejecuta solo si el archivo TIENE contenido
    if (file != null && !file.isEmpty()) {
        nombreArchivo = guardarImagen(file);
    }

    try {
        String sqlProducto;
        if (nombreArchivo != null) {
            sqlProducto = """
                UPDATE producto
                SET nombre = ?, categoria = ?, unidad_medida = ?, precio_base = ?, imagen_url = ?
                WHERE id_producto = ?;
                """;
            jdbcTemplate.update(sqlProducto, nombre, categoria, unidad_medida, precio_base, nombreArchivo, id_producto);
        } else {
            sqlProducto = """
                UPDATE producto
                SET nombre = ?, categoria = ?, unidad_medida = ?, precio_base = ?
                WHERE id_producto = ?;
                """;
            jdbcTemplate.update(sqlProducto, nombre, categoria, unidad_medida, precio_base, id_producto);
        }

        String sqlInventario = """
            UPDATE inventario
            SET cantidad = ?, ultima_actualizacion = NOW()
            WHERE id_producto = ?;
            """;

        jdbcTemplate.update(sqlInventario, cantidad, id_producto);

    } catch (Exception e) {
        System.err.println("Error al actualizar producto: " + e.getMessage());
    }

    return "redirect:/inventario";
}

    // Eliminar producto + inventario
    @GetMapping("/inventario/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") int idProducto) {

        try {
            jdbcTemplate.update("DELETE FROM inventario WHERE id_producto = ?", idProducto);
            jdbcTemplate.update("DELETE FROM producto WHERE id_producto = ?", idProducto);

        } catch (Exception e) {
            System.out.println("Error al eliminar producto: " + e.getMessage());
        }

        return "redirect:/inventario";
    }

    // Método auxiliar para guardar la imagen en el servidor
    private String guardarImagen(MultipartFile file) {
        try {
            // Crear directorio si no existe
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generar un nombre de archivo único para evitar sobreescrituras
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(uniqueFileName);

            Files.copy(file.getInputStream(), filePath);

            return uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo", e);
        }
    }
}
