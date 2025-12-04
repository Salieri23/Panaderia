package com.panaderia.controller;

import com.panaderia.entity.Cliente;
import com.panaderia.entity.OpinionPedido;
import com.panaderia.entity.PedidoCliente;
import com.panaderia.repository.ClienteRepository;
import com.panaderia.repository.PedidoClienteRepository;
import com.panaderia.service.OpinionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class OpinionClienteController {
    
    @Autowired
    private OpinionService opinionService;

    @Autowired
    private PedidoClienteRepository pedidoClienteRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * NUEVO MÉTODO: Muestra la página principal con el historial de opiniones del cliente.
     */
    @GetMapping("/cliente/opiniones")
    public String mostrarMisOpiniones(Principal principal, Model model) {
        
        String emailCliente = principal.getName();
        Cliente cliente = clienteRepository.findByEmail(emailCliente); 
        
        if (cliente == null) {
            return "redirect:/login"; 
        }
        
        Long idCliente = cliente.getIdCliente();

        // Buscamos todas las opiniones de este cliente
        List<OpinionPedido> misOpiniones = opinionService.buscarPorCliente(idCliente);

        model.addAttribute("misOpiniones", misOpiniones);
        model.addAttribute("nombreCliente", cliente.getNombre()); // Para personalizar el título
        
        return "cliente/opiniones"; // Apunta a la nueva página HTML que crearemos
    }

    /**
     * Muestra el formulario para agregar una NUEVA opinión.
     * Este método ya lo tenías, lo mantenemos.
     */
    @GetMapping("/cliente/opinion/nueva")
    public String mostrarNuevaOpinion(Principal principal, Model model) {
        
        String emailCliente = principal.getName();
        Cliente cliente = clienteRepository.findByEmail(emailCliente); 
        
        if (cliente == null) {
            return "redirect:/login"; 
        }
        
        Long idCliente = cliente.getIdCliente();

        model.addAttribute("opinionPedido", new OpinionPedido());
        model.addAttribute("listaopiniones", opinionService.PedidosCliente(idCliente)); // Lista de sus PEDIDOS para elegir
        
        return "cliente/opinion/nueva";
    }

    /**
     * Procesa el formulario y guarda la nueva opinión.
     * Este método ya lo tenías, lo mantenemos.
     */
    @PostMapping("/cliente/opinion/guardar")
    public String guardarOpinion(OpinionPedido opinionPedido, Principal principal, Model model) {
        
        Long idPedidoCliente = opinionPedido.getPedidoCliente().getIdPedidoCliente();
        PedidoCliente pedidoCliente = pedidoClienteRepository.findById(idPedidoCliente).orElse(null);

        if (pedidoCliente == null) {
            model.addAttribute("errorPedido", "El pedido con ID " + idPedidoCliente + " no fue encontrado o no existe.");
            String emailCliente = principal.getName();
            Cliente cliente = clienteRepository.findByEmail(emailCliente);
            model.addAttribute("listaopiniones", opinionService.PedidosCliente(cliente.getIdCliente()));
            model.addAttribute("opinionPedido", opinionPedido); // Devuelve los datos para no perderlos
            return "cliente/opinion/nueva"; 
        }

        String emailCliente = principal.getName();
        opinionPedido.setCliente(clienteRepository.findByEmail(emailCliente));
        opinionPedido.setFecha(LocalDateTime.now());
        opinionPedido.setPedidoCliente(pedidoCliente);

        opinionService.guardarOpinion(opinionPedido);

        return "redirect:/cliente/opiniones?agregada"; // Redirige a la página principal de opiniones
    }
}
