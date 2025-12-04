package com.panaderia.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.panaderia.entity.Cliente;
import com.panaderia.entity.OpinionPedido;
import com.panaderia.entity.PedidoCliente;
import com.panaderia.repository.ClienteRepository;
import com.panaderia.repository.PedidoClienteRepository;
import com.panaderia.service.OpinionService;

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

        model.addAttribute("opinionPedido", new OpinionPedido());
        model.addAttribute("listaopiniones", opinionService.PedidosCliente(idCliente));
        
        return "cliente/opinion/nueva";
    }

    @PostMapping("/cliente/opinion/guardar")
    public String postMethodName(OpinionPedido opinionPedido, Principal principal, Model model) {
        
        Long idPedidoCliente = opinionPedido.getPedidoCliente().getIdPedidoCliente();

        PedidoCliente pedidoCliente = pedidoClienteRepository.findById(idPedidoCliente).orElse(null);

        if (pedidoCliente == null) {
            model.addAttribute("errorPedido", "El pedido con ID " + idPedidoCliente + " no fue encontrado o no existe.");

            String emailCliente = principal.getName();
            Cliente cliente = clienteRepository.findByEmail(emailCliente);

            model.addAttribute("listaopiniones", opinionService.buscarPorCliente(cliente.getIdCliente()));

            return "cliente/opinion/nueva"; 
        }

        String emailCliente = principal.getName();

        opinionPedido.setCliente(clienteRepository.findByEmail(emailCliente));

        opinionPedido.setFecha(LocalDateTime.now());

        opinionPedido.setPedidoCliente(pedidoCliente);

        opinionService.guardarOpinion(opinionPedido);

        return "redirect:/clienteMenu?opinionEnviada";
    }
    

}
