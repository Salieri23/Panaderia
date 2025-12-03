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
        
        // --- CORRECCIÓN CLAVE ---
        // Usamos .orElse(null) para manejar el Optional que devuelve findByEmail
        Empleado empleado = empleadoRepository.findByEmail(email).orElse(null);

        // Si no se encuentra el empleado, redirigimos al index (o a una página de error)
        if (empleado == null) {
            return "redirect:/index";
        }

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
        
        // --- CORRECCIÓN CLAVE ---
        // Buscamos al empleado existente en la BD usando .orElse(null)
        Empleado empleadoExistente = empleadoRepository.findByEmail(email).orElse(null);

        if (empleadoExistente == null) {
            redirectAttributes.addFlashAttribute("error", "No se pudo encontrar tu perfil.");
            return "redirect:/index";
        }

        // Verificación de seguridad: nos aseguramos de que el ID del formulario coincida con el del usuario logeado
        if (!empleadoExistente.getIdEmpleado().equals(empleadoForm.getIdEmpleado())) {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado. No puedes editar otro perfil.");
            return "redirect:/index";
        }

        // Actualizamos los campos
        empleadoExistente.setNombre(empleadoForm.getNombre());
        empleadoExistente.setEmail(empleadoForm.getEmail());
        empleadoExistente.setTelefono(empleadoForm.getTelefono());
        empleadoExistente.setCargo(empleadoForm.getCargo());

        // Lógica para la contraseña: solo se actualiza si el usuario escribió una nueva
        if (empleadoForm.getPassword() != null && !empleadoForm.getPassword().trim().isEmpty()) {
            // IMPORTANTE: En una aplicación real, hashea esta contraseña antes de guardarla
            // String passwordEncriptada = passwordEncoder.encode(empleadoForm.getPassword());
            // empleadoExistente.setPassword(passwordEncriptada);
            empleadoExistente.setPassword(empleadoForm.getPassword());
        }

        // Guardamos los cambios
        empleadoRepository.save(empleadoExistente);

        redirectAttributes.addFlashAttribute("success", "¡Tu perfil ha sido actualizado con éxito!");
        return "redirect:/index";
    }
}
