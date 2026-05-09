package com.facilitalab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("activePage", "cadastro");
        return "cadastro-usuario";
    }

    @GetMapping("/lista")
    public String lista(Model model) {
        model.addAttribute("activePage", "lista");
        return "lista-usuario";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("activePage", "lista");
        return "editar-usuario";
    }
}