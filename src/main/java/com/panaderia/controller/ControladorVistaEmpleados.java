package com.panaderia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/agregar") // Mantiene la ruta base que tenías
public class ControladorVistaEmpleados {

    // 1. CAMBIO CLAVE: Usamos JdbcTemplate en lugar del Repository
    @Autowired
    private JdbcTemplate jdbcTemplate;


    // =================================================================
    // OPERACIÓN READ (Leer / Mostrar todos los empleados)
    // =================================================================
    @GetMapping
    public String mostrarEmpleados(Model model) {

        // SQL para obtener todos los empleados de la tabla 'empleado'
        String sql = "SELECT id_empleado, nombre, cargo, email, telefono, password FROM empleado ORDER BY id_empleado;";

        try {
            // Ejecuta la consulta y obtiene una lista de mapas (cada mapa es una fila)
            List<Map<String, Object>> empleados = jdbcTemplate.queryForList(sql);
            model.addAttribute("empleados", empleados);

        } catch (Exception e) {
            // En caso de error, envía una lista vacía para evitar errores en la vista
            model.addAttribute("empleados", List.of());
            System.out.println("Error al cargar empleados: " + e.getMessage());
        }

        return "agregar"; // Renderiza la vista agregar.html
    }


    // =================================================================
    // OPERACIÓN CREATE (Crear / Guardar un nuevo empleado)
    // =================================================================
    @PostMapping("/guardar")
    public String guardarEmpleado(
            @RequestParam String nombre,
            @RequestParam String cargo,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam String password
    ) {
        // SQL para insertar un nuevo empleado. Asumo que 'id_empleado' es autoincremental.
        String sql = """
            INSERT INTO empleado (nombre, cargo, email, telefono, password)
            VALUES (?, ?, ?, ?, ?);
        """;

        try {
            jdbcTemplate.update(sql, nombre, cargo, email, telefono, password);
        } catch (Exception e) {
            System.out.println("Error al guardar empleado: " + e.getMessage());
        }

        return "redirect:/agregar"; // Redirige a la vista principal para ver el nuevo empleado
    }


    // =================================================================
    // OPERACIÓN UPDATE (Actualizar un empleado existente) - ¡NUEVO!
    // =================================================================
    @PostMapping("/actualizar")
    public String actualizarEmpleado(
            @RequestParam Long id_empleado, // Necesitamos el ID para saber qué actualizar
            @RequestParam String nombre,
            @RequestParam String cargo,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam String password
    ) {
        // SQL para actualizar los datos de un empleado específico
        String sql = """
            UPDATE empleado
            SET nombre = ?, cargo = ?, email = ?, telefono = ?, password = ?
            WHERE id_empleado = ?;
        """;

        try {
            jdbcTemplate.update(sql, nombre, cargo, email, telefono, password, id_empleado);
        } catch (Exception e) {
            System.out.println("Error al actualizar empleado: " + e.getMessage());
        }

        return "redirect:/agregar"; // Redirige para ver los cambios
    }


    // OPERACIÓN DELETE (Eliminar un empleado)
    @GetMapping("/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable("id") Long idEmpleado) {

        String sql = "DELETE FROM empleado WHERE id_empleado = ?;";

        try {
            jdbcTemplate.update(sql, idEmpleado);
        } catch (Exception e) {
            System.out.println("Error al eliminar empleado: " + e.getMessage());
        }

        return "redirect:/agregar";
    }
}
