package com.panaderia.controller;

import com.panaderia.entity.Empleado;
import com.panaderia.repository.EmpleadoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
     * Obtiene el ID del empleado desde la sesión HTTP.
     */
    @GetMapping("/actualizar-perfil")
    public String mostrarFormularioPerfil(HttpSession session, Model model) {
        // Obtenemos el ID del empleado que está en la sesión (lo guardaste al hacer login)
        Long idEmpleado = (Long) session.getAttribute("empleadoLogeadoId");

        // Si no hay un empleado en la sesión, redirigir al login
        if (idEmpleado == null) {
            return "redirect:/login";
        }

        // Buscamos al empleado en la base de datos
        Empleado empleado = empleadoRepository.findById(idEmpleado).orElse(null);

        // Si por alguna razón no se encuentra, redirigir a una página de error o login
        if (empleado == null) {
            return "redirect:/login?error";
        }

        // Añadimos el objeto empleado al modelo para que Thymeleaf pueda usarlo
        model.addAttribute("empleado", empleado);

        // Devolvemos el nombre de la vista HTML
        return "actualizarEmpleado";
    }

    /**
     * Procesa el formulario de actualización de perfil.
     */
    @PostMapping("/actualizar-perfil")
    public String procesarActualizacionPerfil(@ModelAttribute Empleado empleadoForm, HttpSession session, RedirectAttributes redirectAttributes) {
        // Obtenemos el ID del empleado logeado desde la sesión para seguridad
        Long idEmpleadoLogeado = (Long) session.getAttribute("empleadoLogeadoId");

        if (idEmpleadoLogeado == null || !idEmpleadoLogeado.equals(empleadoForm.getIdEmpleado())) {
            // Si no hay sesión o el ID del formulario no coincide con el de la sesión, es un intento de hackeo
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/login";
        }

        // Buscamos al empleado existente en la base de datos para actualizarlo
        Empleado empleadoExistente = empleadoRepository.findById(idEmpleadoLogeado).orElse(null);
        if (empleadoExistente == null) {
            redirectAttributes.addFlashAttribute("error", "No se pudo encontrar tu perfil.");
            return "redirect:/empleadoMenu";
        }

        // Actualizamos los campos con los datos del formulario
        empleadoExistente.setNombre(empleadoForm.getNombre());
        empleadoExistente.setEmail(empleadoForm.getEmail());
        empleadoExistente.setTelefono(empleadoForm.getTelefono());
        empleadoExistente.setCargo(empleadoForm.getCargo());

        // Lógica para la contraseña: solo se actualiza si el usuario escribió una nueva
        if (empleadoForm.getPassword() != null && !empleadoForm.getPassword().trim().isEmpty()) {
            // IMPORTANTE: En una aplicación real, deberías hashear esta contraseña antes de guardarla
            // String hashedPassword = passwordEncoder.encode(empleadoForm.getPassword());
            // empleadoExistente.setPassword(hashedPassword);
            empleadoExistente.setPassword(empleadoForm.getPassword());
        }

        // Guardamos los cambios en la base de datos
        empleadoRepository.save(empleadoExistente);

        // Añadimos un mensaje de éxito y redirigimos de vuelta al menú
        redirectAttributes.addFlashAttribute("success", "¡Tu perfil ha sido actualizado con éxito!");
        return "redirect:/empleadoMenu";
    }
}
