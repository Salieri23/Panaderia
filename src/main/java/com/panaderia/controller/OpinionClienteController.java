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

    @GetMapping("/cliente/opinion/nueva")
    public String mostrarNuevaOpinion(Principal principal, Model model) {
        
        String emailCliente = principal.getName();
        Cliente cliente = clienteRepository.findByEmail(emailCliente); 
        
        if (cliente == null) {
            return "redirect:/login"; 
        }
        
        Long idCliente = cliente.getIdCliente();

        // 1. Lista de PEDIDOS del cliente para el formulario desplegable
        List<PedidoCliente> listaPedidos = opinionService.PedidosCliente(idCliente);
        model.addAttribute("listaPedidos", listaPedidos);

        // 2. Lista de OPINIONES ya registradas por el cliente para la tabla
        List<OpinionPedido> misOpiniones = opinionService.buscarPorCliente(idCliente);
        model.addAttribute("misOpiniones", misOpiniones);

        model.addAttribute("opinionPedido", new OpinionPedido());
        
        return "cliente/opinion/nueva";
    }

    @PostMapping("/cliente/opinion/guardar")
    public String guardarOpinion(OpinionPedido opinionPedido, Principal principal, Model model) {
        
        Long idPedidoCliente = opinionPedido.getPedidoCliente().getIdPedidoCliente();
        PedidoCliente pedidoCliente = pedidoClienteRepository.findById(idPedidoCliente).orElse(null);

        if (pedidoCliente == null) {
            model.addAttribute("errorPedido", "El pedido con ID " + idPedidoCliente + " no fue encontrado o no existe.");
            // En caso de error, volvemos a cargar las listas necesarias
            String emailCliente = principal.getName();
            Cliente cliente = clienteRepository.findByEmail(emailCliente);
            model.addAttribute("listaPedidos", opinionService.PedidosCliente(cliente.getIdCliente()));
            model.addAttribute("misOpiniones", opinionService.buscarPorCliente(cliente.getIdCliente()));
            model.addAttribute("opinionPedido", opinionPedido); 
            return "cliente/opinion/nueva"; 
        }

        String emailCliente = principal.getName();
        opinionPedido.setCliente(clienteRepository.findByEmail(emailCliente));
        opinionPedido.setFecha(LocalDateTime.now());
        opinionPedido.setPedidoCliente(pedidoCliente);

        opinionService.guardarOpinion(opinionPedido);

        return "redirect:/cliente/opinion/nueva?exito"; // Añadimos un parámetro para mostrar un mensaje de éxito
    }
}
