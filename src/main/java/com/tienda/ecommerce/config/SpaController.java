package com.tienda.ecommerce.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({
            "/login",
            "/registro",
            "/admin",
            "/catalogo",
            "/mi-cuenta",
            "/recomendados",
            "/carrito",
            "/producto/{id}"
    })
    public String forwardSpa() {
        return "forward:/index.html";
    }
}
