package com.facilitalab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Sem Thymeleaf: os métodos fazem forward para arquivos estáticos em static/.
// Model foi removido pois não há mais variáveis sendo passadas ao template.
@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "forward:/cadastro-usuario.html";
    }

    @GetMapping("/lista")
    public String lista() {
        return "forward:/lista-usuario.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }

    // O id não é mais usado aqui — a página editar-usuario.js extrai o id do pathname
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id) {
        return "forward:/editar-usuario.html";
    }
}
