package com.panaderia.controller;

import com.panaderia.entity.Empleado;
import com.panaderia.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/agregar")
public class ControladorVistaEmpleados {

    @Autowired
    private EmpleadoRepository empleadoRepo;

    // Mostrar página
    @GetMapping
    public String mostrarEmpleados(Model model) {
        model.addAttribute("empleados", empleadoRepo.findAll());
        return "agregar";
    }

    // Recibir formulario de Thymeleaf
    @PostMapping("/guardar")
    public String guardarEmpleado(
            @RequestParam String nombre,
            @RequestParam String cargo,
            @RequestParam String email,
            @RequestParam(required = false) String telefono,
            @RequestParam String password
    ) {

        Empleado emp = new Empleado();
        emp.setNombre(nombre);
        emp.setCargo(cargo);
        emp.setEmail(email);
        emp.setTelefono(telefono);
        emp.setPassword(password);

        empleadoRepo.save(emp);

        return "redirect:/agregar";
    }

    // eliminar desde Thymeleaf
    @GetMapping("/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable Long id) {
        empleadoRepo.deleteById(id);
        return "redirect:/agregar";
    }
}
