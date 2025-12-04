package com.panaderia.controller;

import com.panaderia.entity.OpinionPedido;
import com.panaderia.repository.ClienteRepository;
import com.panaderia.repository.OpinionRepository;
import com.panaderia.repository.PedidoClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/opinion") // Mantenemos la ruta base para este controlador
public class OpinionController {

    @Autowired
    private OpinionRepository opinionRepo;

    @Autowired
    private ClienteRepository clienteRepo;

    @Autowired
    private PedidoClienteRepository pedidoRepo;

    // Inyectamos JdbcTemplate para las nuevas consultas complejas
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- Endpoint EXISTENTE (para registrar una nueva opinión) ---
    @PostMapping
    public Map<String, Object> registrarOpinion(@RequestBody Map<String, Object> datos) {

        Long idPedido = Long.valueOf(datos.get("id_pedido_cliente").toString());
        Long idCliente = Long.valueOf(datos.get("id_cliente").toString());

        OpinionPedido op = new OpinionPedido();
        op.setPedidoCliente(pedidoRepo.findById(idPedido).orElse(null));
        op.setCliente(clienteRepo.findById(idCliente).orElse(null));
        op.setComentario((String) datos.get("comentario"));
        op.setCalificacion(Integer.valueOf(datos.get("calificacion").toString()));
        op.setSatisfaccion(Integer.valueOf(datos.get("satisfaccion").toString()));
        op.setFecha(LocalDateTime.now());

        opinionRepo.save(op);

        return Map.of("id_opinion", op.getIdOpinion());
    }

    // --- NUEVO Endpoint para la TABLA de opiniones en el dashboard ---
    // Nota: La ruta es /api/opiniones para que coincida con tu fetch en el HTML
    @GetMapping(path = { "/", "/opiniones" })
    public ResponseEntity<List<Map<String, Object>>> getAllOpiniones() {
        String sql = """
            SELECT 
                id_pedido_cliente, 
                comentario, 
                calificacion, 
                satisfaccion, 
                fecha 
            FROM opinion_pedido 
            ORDER BY fecha DESC
            """;

        try {
            List<Map<String, Object>> opiniones = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(opiniones);
        } catch (Exception e) {
            System.err.println("Error al obtener la lista de opiniones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // --- NUEVO Endpoint para el GRÁFICO del dashboard ---
    // Nota: La ruta es /dashboard/opiniones como lo teníamos planeado
    @GetMapping(path = "/dashboard/opiniones")
    public ResponseEntity<List<Map<String, Object>>> getOpinionesParaGrafico() {
        String sql = """
            SELECT
                'Pedido ' || op.id_pedido_cliente as nombre,
                AVG(op.calificacion) as promedio,
                AVG(op.satisfaccion) as satisfaccion
            FROM opinion_pedido op
            GROUP BY op.id_pedido_cliente
            ORDER BY op.id_pedido_cliente
        """;

        try {
            List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            System.err.println("Error al obtener datos para el gráfico de opiniones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
