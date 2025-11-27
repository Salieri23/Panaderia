package com.panaderia.config;

import com.panaderia.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Inyección de dependencias por constructor (mejor práctica)
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomSuccessHandler customSuccessHandler;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, CustomSuccessHandler customSuccessHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.customSuccessHandler = customSuccessHandler;
    }

    // Bean para el codificador de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean para el proveedor de autenticación
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Configuración principal de la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Se recomienda mantener CSRF activado para aplicaciones web tradicionales.
            // Thymeleaf añade el token automáticamente a los formularios.
            // .csrf(csrf -> csrf.disable()) 

            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth
                // Rutas públicas (acceso sin login)
                .requestMatchers("/", "/login", "/registroCliente",
                                 "/css/**", "/js/**", "/images/**").permitAll()

                // Rutas exclusivas para Empleados (o Administradores)
                // Asegúrate de que el rol en la BD sea "ROLE_EMPLEADO" o cámbialo a "ROLE_ADMIN"
                .requestMatchers("/empleados/**", "/inventario", "/reportes", 
                                 "/registrar", "/consultar", "/observar", "/entregas")
                    .hasRole("EMPLEADO")

                // Rutas exclusivas para Clientes
                .requestMatchers("/clienteMenu", "/cliente/**", "/actualizarCliente")
                    .hasRole("CLIENTE")

                // Cualquier otra petición requiere estar autenticado
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login") // Página de login personalizada
                .loginProcessingUrl("/login") // URL donde se procesa el login
                .successHandler(customSuccessHandler) // Redirección personalizada tras login exitoso
                .failureUrl("/login?error=true") // Redirección en caso de fallo
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout") // URL para cerrar sesión
                .logoutSuccessUrl("/login?logout") // Página a la que se redirige tras cerrar sesión
                .permitAll()
            );

        return http.build();
    }
}
