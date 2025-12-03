package com.panaderia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/clientes") // Ruta base para la gestión de clientes
public class ClienteViewController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =================================================================
    // OPERACIÓN READ (Leer / Mostrar todos los clientes)
    // =================================================================
    @GetMapping
    public String mostrarClientes(Model model) {
        // SQL para obtener todos los clientes
        String sql = "SELECT id_cliente, nombre, direccion, email, telefono FROM cliente ORDER BY id_cliente;";

        try {
            List<Map<String, Object>> clientes = jdbcTemplate.queryForList(sql);
            model.addAttribute("clientes", clientes);

        } catch (Exception e) {
            model.addAttribute("clientes", List.of());
            System.err.println("Error al cargar clientes: " + e.getMessage());
        }

        return "clientes"; // Renderiza la vista clientes.html
    }

    // =================================================================
    // OPERACIÓN CREATE (Crear / Guardar un nuevo cliente)
    // =================================================================
    @PostMapping("/guardar")
    public String guardarCliente(
            @RequestParam String nombre,
            @RequestParam String direccion,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam String password
    ) {
        // SQL para insertar un nuevo cliente.
        String sql = """
            INSERT INTO cliente (nombre, direccion, email, telefono, password)
            VALUES (?, ?, ?, ?, ?);
        """;

        try {
            jdbcTemplate.update(sql, nombre, direccion, email, telefono, password);
        } catch (Exception e) {
            System.err.println("Error al guardar cliente: " + e.getMessage());
        }

        return "redirect:/clientes";
    }

    // =================================================================
    // OPERACIÓN UPDATE (Actualizar un cliente existente)
    // =================================================================
    @PostMapping("/actualizar")
    public String actualizarCliente(
            @RequestParam Long id_cliente,
            @RequestParam String nombre,
            @RequestParam String direccion,
            @RequestParam String email,
            @RequestParam(required = false) String telefono
            // La contraseña NO se maneja aquí por seguridad
    ) {
        // SQL para actualizar los datos de un cliente específico (sin contraseña)
        String sql = """
            UPDATE cliente
            SET nombre = ?, direccion = ?, email = ?, telefono = ?
            WHERE id_cliente = ?;
        """;

        try {
            jdbcTemplate.update(sql, nombre, direccion, email, telefono, id_cliente);
        } catch (Exception e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
        }

        return "redirect:/clientes";
    }

    // =================================================================
    // OPERACIÓN DELETE (Eliminar un cliente)
    // =================================================================
    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable("id") Long idCliente) {

        String sql = "DELETE FROM cliente WHERE id_cliente = ?;";

        try {
            jdbcTemplate.update(sql, idCliente);
        } catch (Exception e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
        }

        return "redirect:/clientes";
    }
}
