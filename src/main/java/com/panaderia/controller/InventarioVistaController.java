package com.panaderia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class InventarioVistaController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Vista principal del inventario (SIN COLUMNA imagen_url)
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
                i.ultima_actualizacion
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

    // Guardar un nuevo producto (LÓGICA SIN IMÁGENES)
    @PostMapping("/inventario/guardar")
    public String guardarProducto(
            @RequestParam String nombre,
            @RequestParam String categoria,
            @RequestParam int cantidad,
            @RequestParam String unidad_medida,
            @RequestParam BigDecimal precio_base
    ) {
        try {
            String sqlProducto = """
                INSERT INTO producto (nombre, categoria, unidad_medida, precio_base)
                VALUES (?, ?, ?, ?)
                RETURNING id_producto;
                """;

            Integer idProducto = jdbcTemplate.queryForObject(
                    sqlProducto,
                    Integer.class,
                    nombre, categoria, unidad_medida, precio_base
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

    // Actualizar producto existente (LÓGICA SIN IMÁGENES)
    @PostMapping("/inventario/actualizar")
    public String actualizarProducto(
            @RequestParam int id_producto,
            @RequestParam String nombre,
            @RequestParam String categoria,
            @RequestParam BigDecimal precio_base,
            @RequestParam int cantidad,
            @RequestParam String unidad_medida
    ) {
        try {
            String sqlProducto = """
                UPDATE producto
                SET nombre = ?, categoria = ?, unidad_medida = ?, precio_base = ?
                WHERE id_producto = ?;
                """;
            jdbcTemplate.update(sqlProducto, nombre, categoria, unidad_medida, precio_base, id_producto);

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
}
