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
                    config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:8080"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setExposedHeaders(List.of("Authorization"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/", "/index.html",
                                "/*.js", "/*.css", "/*.ico", "/assets/**").permitAll()
                        .requestMatchers("/login", "/registro", "/catalogo",
                                "/mi-cuenta", "/recomendados", "/carrito",
                                "/producto/**", "/admin").permitAll()
                        .requestMatchers("/usuarios/**", "/api/auth/**").permitAll()
                        .requestMatchers("/api/pagos/**").permitAll()
                        .requestMatchers("/api/interacciones/registrar").permitAll()
                        .requestMatchers("/api/interacciones/sugeridos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/productos/cargar-csv").permitAll()
                        .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/productos").authenticated()
                        .requestMatchers(HttpMethod.POST, "/productos/categorias").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/productos/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/productos/**").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/auth/usuarios").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/carrito/**").authenticated()
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}