package com.panaderia.controller;

import com.panaderia.entity.Cliente;
import com.panaderia.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam String direccion
    ) {

        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setEmail(email);
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);

        clienteRepo.save(cliente);

        return "redirect:/clientes";  // Redirige a la lista de clientes 
    }

    // Eliminar un cliente desde Thymeleaf
    @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id) {
        clienteRepo.deleteById(id);
        return "redirect:/clientes";  // Redirige a la lista de clientes
    }

    // Mostrar página para editar cliente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Cliente cliente = clienteRepo.findById(id).orElse(null);
        model.addAttribute("cliente", cliente);
        return "editar_cliente";  
    }

    // Guardar cambios después de editar cliente
    @PostMapping("/editar/{id}")
    public String editarCliente(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam String direccion
    ) {
        Cliente cliente = clienteRepo.findById(id).orElse(null);
        if (cliente != null) {
            cliente.setNombre(nombre);
            cliente.setEmail(email);
            cliente.setTelefono(telefono);
            cliente.setDireccion(direccion);
            clienteRepo.save(cliente);
        }
        return "redirect:/clientes";  
    }
}
