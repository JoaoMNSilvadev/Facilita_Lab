package com.facilitalab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "redirect:/login";}

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";}

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";}

    @GetMapping("/cadastro-usuario")
    public String cadastroUsuario() {
        return "forward:/cadastro-usuario.html";}

    @GetMapping("/lista-usuarios")
    public String listaUsuarios() {
        return "forward:/lista-usuario.html";}

    @GetMapping("/editar-usuario/{id}")
    public String editarUsuario(@PathVariable Long id) {
        return "forward:/editar-usuario.html";}

    @GetMapping("/cadastro-pedido")
    public String cadastroPedido() {
        return "forward:/cadastro-pedido.html";}

    @GetMapping("/lista-pedidos")
    public String listaPedidos() {
        return "forward:/lista-pedido.html";}
    
    @GetMapping("/editar-pedido/{id}")
    public String editarPedido(@PathVariable Long id) {
        return "forward:/editar-pedido.html";}
}