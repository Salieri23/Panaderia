package com.panaderia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/agregar")
public class ControladorVistaEmpleados {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =================================================================
    // OPERACIÓN READ (Leer / Mostrar todos los empleados)
    // =================================================================
    @GetMapping
    public String mostrarEmpleados(Model model) {

        // CAMBIO: Se elimina 'password' de la consulta por seguridad.
        String sql = "SELECT id_empleado, nombre, cargo, email, telefono FROM empleado ORDER BY id_empleado;";

        try {
            List<Map<String, Object>> empleados = jdbcTemplate.queryForList(sql);
            model.addAttribute("empleados", empleados);

        } catch (Exception e) {
            model.addAttribute("empleados", List.of());
            System.out.println("Error al cargar empleados: " + e.getMessage());
        }

        return "agregar";
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
        String sql = """
            INSERT INTO empleado (nombre, cargo, email, telefono, password)
            VALUES (?, ?, ?, ?, ?);
        """;

        try {
            // IMPORTANTE: Deberías encriptar la contraseña antes de guardarla.
            // Por ejemplo, usando BCryptPasswordEncoder.
            // String passwordEncriptada = passwordEncoder.encode(password);
            // jdbcTemplate.update(sql, nombre, cargo, email, telefono, passwordEncriptada);
            
            jdbcTemplate.update(sql, nombre, cargo, email, telefono, password);
        } catch (Exception e) {
            System.out.println("Error al guardar empleado: " + e.getMessage());
        }

        return "redirect:/agregar";
    }


    // =================================================================
    // OPERACIÓN UPDATE (Actualizar un empleado existente)
    // =================================================================
    @PostMapping("/actualizar")
    public String actualizarEmpleado(
            @RequestParam Long id_empleado,
            @RequestParam String nombre,
            @RequestParam String cargo,
            @RequestParam String email,
            @RequestParam(required = false) String telefono
            // CAMBIO: Se elimina el parámetro 'password' del método
    ) {
        // CAMBIO: Se elimina 'password' de la consulta SQL.
        String sql = """
            UPDATE empleado
            SET nombre = ?, cargo = ?, email = ?, telefono = ?
            WHERE id_empleado = ?;
        """;

        try {
            // CAMBIO: Se elimina la contraseña de la llamada a jdbcTemplate.update
            jdbcTemplate.update(sql, nombre, cargo, email, telefono, id_empleado);
        } catch (Exception e) {
            System.out.println("Error al actualizar empleado: " + e.getMessage());
        }

        return "redirect:/agregar";
    }


    // =================================================================
    // OPERACIÓN DELETE (Eliminar un empleado)
    // =================================================================
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
