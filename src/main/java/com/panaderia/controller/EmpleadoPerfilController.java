package com.panaderia.controller;

import com.panaderia.entity.Empleado;
import com.panaderia.repository.EmpleadoRepository;
import com.panaderia.service.EmpleadoUserDetails; // Asegúrate que esta importación sea correcta
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
     * Obtiene los datos del usuario directamente desde el contexto de Spring Security.
     */
    @GetMapping("/actualizar-perfil")
    public String mostrarFormularioPerfil(Model model) {
        // 1. Obtenemos la información de autenticación del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Verificamos que el usuario logeado sea un Empleado
        if (authentication != null && authentication.getPrincipal() instanceof EmpleadoUserDetails) {
            
            // 3. Hacemos un "cast" para obtener nuestro objeto EmpleadoUserDetails personalizado
            EmpleadoUserDetails empleadoUserDetails = (EmpleadoUserDetails) authentication.getPrincipal();
            
            // 4. Obtenemos el objeto Empleado completo desde nuestro UserDetails
            Empleado empleado = empleadoUserDetails.getEmpleado();

            // 5. Añadimos el objeto empleado al modelo para que Thymeleaf pueda usarlo
            model.addAttribute("empleado", empleado);
            
            return "actualizarEmpleado"; // Devolvemos la vista del formulario
        }

        // Si por alguna razón no es un empleado o no está autenticado, lo mandamos al login
        return "redirect:/login";
    }

    /**
     * Procesa el formulario de actualización de perfil.
     */
    @PostMapping("/actualizar-perfil")
    public String procesarActualizacionPerfil(@ModelAttribute Empleado empleadoForm, RedirectAttributes redirectAttributes) {
        // Obtenemos los datos del usuario logeado de la misma forma que en el método GET
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof EmpleadoUserDetails) {
            EmpleadoUserDetails empleadoUserDetails = (EmpleadoUserDetails) authentication.getPrincipal();
            Long idEmpleadoLogeado = empleadoUserDetails.getEmpleado().getIdEmpleado();

            // Verificación de seguridad: nos aseguramos de que el ID del formulario coincida con el del usuario logeado
            if (!idEmpleadoLogeado.equals(empleadoForm.getIdEmpleado())) {
                redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
                return "redirect:/empleadoMenu";
            }

            // Buscamos al empleado en la base de datos para actualizarlo
            Empleado empleadoExistente = empleadoRepository.findById(idEmpleadoLogeado).orElse(null);
            if (empleadoExistente == null) {
                redirectAttributes.addFlashAttribute("error", "No se pudo encontrar tu perfil.");
                return "redirect:/empleadoMenu";
            }

            // Actualizamos los campos
            empleadoExistente.setNombre(empleadoForm.getNombre());
            empleadoExistente.setEmail(empleadoForm.getEmail());
            empleadoExistente.setTelefono(empleadoForm.getTelefono());
            empleadoExistente.setCargo(empleadoForm.getCargo());

            // Lógica para la contraseña: solo se actualiza si el usuario escribió una nueva
            if (empleadoForm.getPassword() != null && !empleadoForm.getPassword().trim().isEmpty()) {
                // IMPORTANTE: En una aplicación real, hashea esta contraseña antes de guardarla
                empleadoExistente.setPassword(empleadoForm.getPassword());
            }

            // Guardamos los cambios
            empleadoRepository.save(empleadoExistente);

            redirectAttributes.addFlashAttribute("success", "¡Tu perfil ha sido actualizado con éxito!");
            return "redirect:/empleadoMenu";
        }

        return "redirect:/login";
    }
}
