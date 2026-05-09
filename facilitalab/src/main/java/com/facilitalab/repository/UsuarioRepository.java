package com.facilitalab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.facilitalab.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    <optional> Usuario findByEmail(String email);

}
