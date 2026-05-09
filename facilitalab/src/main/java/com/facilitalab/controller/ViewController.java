package com.facilitalab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Mapear as rotas que retornam páginas HTML.

@Controller
public class ViewController {

    // Primeira tela que abre
    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro-usuario";
    }

    @GetMapping("/lista")
    public String lista() {return "lista-usuario";}

    @GetMapping("/dashboard")
    public String dashboard() {return "dashboard";}
}