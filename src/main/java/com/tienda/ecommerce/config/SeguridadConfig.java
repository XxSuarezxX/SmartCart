package com.tienda.ecommerce.config;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class SeguridadConfig {
    private final JwtFilter jwtFilter;
    public SeguridadConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(
                        "http://localhost:4200",
                        "http://localhost:8080",
                        "https://smartcart-production-3c30.up.railway.app"
                    ));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setExposedHeaders(List.of("Authorization"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Archivos estáticos del frontend
                        .requestMatchers(
                            "/", "/index.html", "/favicon.ico",
                            "/**/*.js", "/**/*.css", "/**/*.ico",
                            "/assets/**", "/chunk-*.js", "/main-*.js",
                            "/styles-*.css", "/3rdpartylicenses.txt",
                            "/prerendered-routes.json"
                        ).permitAll()
                        // Rutas SPA
                        .requestMatchers(
                            "/login", "/registro", "/catalogo",
                            "/mi-cuenta", "/recomendados", "/carrito",
                            "/producto/**", "/admin"
                        ).permitAll()
                        // --- API PÚBLICA (sin sesión) ---
                        .requestMatchers("/api/auth/login", "/api/auth/registro").permitAll()
                        .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()
                        .requestMatchers("/api/interacciones/registrar").permitAll()
                        .requestMatchers("/api/interacciones/sugeridos/**").permitAll()
                        // --- SOLO ADMIN ---
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/auth/usuarios").hasAuthority("ADMIN")
                        .requestMatchers("/api/interacciones/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/pagos/todos").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/productos/cargar-csv").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/productos/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/productos/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/productos/**").hasAuthority("ADMIN")
                        // --- REQUIERE SESIÓN (cualquier rol autenticado) ---
                        .requestMatchers("/api/pagos/**").authenticated()
                        .requestMatchers("/api/interacciones/**").authenticated()
                        .requestMatchers("/carrito/**").authenticated()
                        .requestMatchers("/", "/index.html", "/*.js", "/*.css", "/*.ico", "/assets/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
