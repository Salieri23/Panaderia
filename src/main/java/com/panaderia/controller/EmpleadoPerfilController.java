package com.panaderia.controller;

import com.panaderia.entity.Empleado;
import com.panaderia.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/empleado")
public class EmpleadoPerfilController {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    /**
     * Muestra el formulario de actualización de perfil para el empleado logeado.
     */
    @GetMapping("/actualizar-perfil")
    public String mostrarFormularioPerfil(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Empleado empleado = empleadoRepository.findByEmail(email);

        model.addAttribute("empleado", empleado);
        return "actualizarEmpleado";
    }

    /**
     * Procesa el formulario de actualización de perfil.
     */
    @PostMapping("/actualizar-perfil")
    public String procesarActualizacionPerfil(@ModelAttribute Empleado empleadoForm, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Empleado empleadoExistente = empleadoRepository.findByEmail(email);

        if (empleadoExistente == null) {
            redirectAttributes.addFlashAttribute("error", "No se pudo encontrar tu perfil.");
            // CORRECCIÓN: Redirigir a /index
            return "redirect:/index";
        }

        // Verificación de seguridad
        if (!empleadoExistente.getIdEmpleado().equals(empleadoForm.getIdEmpleado())) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. No puedes editar otro perfil.");
            // CORRECCIÓN: Redirigir a /index
            return "redirect:/index";
        }

        // Actualizamos los campos
        empleadoExistente.setNombre(empleadoForm.getNombre());
        empleadoExistente.setEmail(empleadoForm.getEmail());
        empleadoExistente.setTelefono(empleadoForm.getTelefono());
        empleadoExistente.setCargo(empleadoForm.getCargo());

        // Lógica para la contraseña
        if (empleadoForm.getPassword() != null && !empleadoForm.getPassword().trim().isEmpty()) {
            empleadoExistente.setPassword(empleadoForm.getPassword());
        }

        empleadoRepository.save(empleadoExistente);

        redirectAttributes.addFlashAttribute("success", "¡Tu perfil ha sido actualizado con éxito!");
        // CORRECCIÓN: Redirigir a /index
        return "redirect:/index";
    }
}
