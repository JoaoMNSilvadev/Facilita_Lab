package com.facilitalab.service;

import com.facilitalab.dtos.UsuarioSaidaDTO;
import com.facilitalab.repository.UsuarioRepository;

public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    
    public boolean emailExists(String email) {
        return usuarioRepository.findByEmail(email) != null;
    }

    public UsuarioSaidaDTO BuscarUsuarioPorEmail(String email){
        var usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        return new UsuarioSaidaDTO(usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name());
    }

    public UsuarioSaidaDTO BuscarUsuarioPorId(Long id) {
        var usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        return new UsuarioSaidaDTO(usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name());
    }


    public void deletarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        usuarioRepository.deleteById(id);
    }

}
