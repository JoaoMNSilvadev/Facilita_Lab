package com.facilitalab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Mapear as rotas que retornam páginas HTML.

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "redirect:/cadastro";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro-usuario";
    }

    @GetMapping("/lista")
    public String lista() {return "lista-usuario";}
}