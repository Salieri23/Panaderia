package com.panaderia.controller;

import com.panaderia.entity.Cliente;
import com.panaderia.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ControladorVistaClientes {

    @Autowired
    private ClienteRepository clienteRepo;

    // Mostrar página con lista de clientes
    @GetMapping
    public String mostrarClientes(Model model) {
        model.addAttribute("clientes", clienteRepo.findAll());
        return "clientes";
    }

    // Recibir formulario para guardar un nuevo cliente
    @PostMapping("/guardar")
    public String guardarCliente(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam String direccion,
            @RequestParam String password,
            RedirectAttributes redirectAttributes
    ) {
        // Aquí deberías hashear la contraseña antes de guardarla por seguridad
        // String hashedPassword = ...; 
        // cliente.setPassword(hashedPassword);
        
        // Verificar si el email ya existe
        if (clienteRepo.findByEmail(email) != null) {
            redirectAttributes.addFlashAttribute("error", "El email ya está registrado.");
            return "redirect:/clientes";
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setEmail(email);
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);
        cliente.setPassword(password); // Guardar la contraseña (hasheada en producción)
        cliente.setRol("CLIENTE"); // Asignar un rol por defecto

        clienteRepo.save(cliente);
        redirectAttributes.addFlashAttribute("success", "Cliente guardado con éxito.");

        return "redirect:/clientes";
    }

    // Eliminar un cliente
    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Cliente eliminado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el cliente. Puede tener pedidos asociados.");
        }
        return "redirect:/clientes";
    }

    // CORRECCIÓN: Nuevo método para manejar la actualización desde el modal
    @PostMapping("/actualizar")
    public String actualizarCliente(
            @RequestParam Long id_cliente, // El 'name' del input hidden es 'id_cliente'
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam String direccion,
            RedirectAttributes redirectAttributes
    ) {
        Cliente cliente = clienteRepo.findById(id_cliente)
                .orElseThrow(() -> new IllegalArgumentException("ID de cliente inválido: " + id_cliente));
        
        // Verificar si el nuevo email ya está en uso por otro cliente
        Cliente existingCliente = clienteRepo.findByEmail(email);
        
        // CORRECCIÓN CLAVE: Se usa getIdCliente() en lugar de getId_cliente()
        if (existingCliente != null && !existingCliente.getIdCliente().equals(id_cliente)) {
            redirectAttributes.addFlashAttribute("error", "El email ya está en uso por otro cliente.");
            return "redirect:/clientes";
        }

        cliente.setNombre(nombre);
        cliente.setEmail(email);
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);
        // No se actualiza la contraseña aquí por seguridad

        clienteRepo.save(cliente);
        redirectAttributes.addFlashAttribute("success", "Cliente actualizado con éxito.");
        
        return "redirect:/clientes";
    }
}
