import com.panaderia.entity.Empleado;
import com.panaderia.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/empleados") // URL más clara para la gestión de empleados
public class EmpleadoController {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Inyectamos el codificador de contraseñas

    // 1. LISTAR todos los empleados y mostrar el formulario de agregar
    @GetMapping
    public String listarEmpleados(Model model) {
        model.addAttribute("empleados", empleadoRepository.findAll());
        model.addAttribute("empleado", new Empleado()); // Para el formulario de "agregar nuevo"
        return "gestionEmpleados"; // Usaremos un template más descriptivo
    }

    // 2. GUARDAR un nuevo empleado
    @PostMapping("/guardar")
    public String guardarEmpleado(@ModelAttribute Empleado empleado, RedirectAttributes redirectAttributes) {
        try {
            // Hashear la contraseña ANTES de guardarla
            String passwordHasheado = passwordEncoder.encode(empleado.getPassword());
            empleado.setPassword(passwordHasheado);
            
            empleadoRepository.save(empleado);
            redirectAttributes.addFlashAttribute("mensaje", "Empleado guardado con éxito.");
            return "redirect:/empleados";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el empleado. El email podría ya existir.");
            return "redirect:/empleados";
        }
    }

    // 3. MOSTRAR el formulario para editar un empleado
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Empleado> empleadoOptional = empleadoRepository.findById(id);
        if (empleadoOptional.isPresent()) {
            model.addAttribute("empleado", empleadoOptional.get()); // Carga el empleado a editar
            model.addAttribute("esEdicion", true); // Una bandera para la vista
        } else {
            return "redirect:/empleados"; // Si no existe, redirige a la lista
        }
        model.addAttribute("empleados", empleadoRepository.findAll()); // También se necesita la lista
        return "gestionEmpleados";
    }

    // 4. ACTUALIZAR un empleado existente
    @PostMapping("/actualizar/{id}")
    public String actualizarEmpleado(@PathVariable Long id, @ModelAttribute Empleado empleadoDetalles, RedirectAttributes redirectAttributes) {
        try {
            Empleado empleadoExistente = empleadoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("ID de empleado no válido: " + id));

            // Actualizar los campos
            empleadoExistente.setNombre(empleadoDetalles.getNombre());
            empleadoExistente.setCargo(empleadoDetalles.getCargo());
            empleadoExistente.setEmail(empleadoDetalles.getEmail());
            empleadoExistente.setTelefono(empleadoDetalles.getTelefono());
            
            // IMPORTANTE: Solo actualizar la contraseña si se proporciona una nueva
            if (empleadoDetalles.getPassword() != null && !empleadoDetalles.getPassword().isEmpty()) {
                empleadoExistente.setPassword(passwordEncoder.encode(empleadoDetalles.getPassword()));
            }

            empleadoRepository.save(empleadoExistente);
            redirectAttributes.addFlashAttribute("mensaje", "Empleado actualizado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el empleado.");
        }
        return "redirect:/empleados";
    }

    // 5. ELIMINAR un empleado
    @GetMapping("/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            empleadoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Empleado eliminado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar el empleado. Puede que tenga datos asociados.");
        }
        return "redirect:/empleados";
    }
}
