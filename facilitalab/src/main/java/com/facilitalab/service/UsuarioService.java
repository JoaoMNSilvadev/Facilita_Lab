package com.facilitalab.service;

import java.util.List;

import com.facilitalab.dtos.UsuarioUpdateDTO;
import org.springframework.stereotype.Service;

import com.facilitalab.dtos.UsuarioCreateDTO;
import com.facilitalab.dtos.UsuarioSaidaDTO;
import com.facilitalab.models.PerfilEnum;
import com.facilitalab.models.Usuario;
import com.facilitalab.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // CREATE
    public UsuarioSaidaDTO criar(UsuarioCreateDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado: " + dto.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenhaHash(hashSenha(dto.getSenha()));
        usuario.setPerfil(dto.getPerfil());

        Usuario salvo = usuarioRepository.save(usuario);
        return toSaidaDTO(salvo);
    }

    // READ - todos
    public List<UsuarioSaidaDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toSaidaDTO)
                .toList();
    }

    // READ - por e-mail
    public UsuarioSaidaDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com e-mail: " + email));
        return toSaidaDTO(usuario);
    }

    // READ - por ID
    public UsuarioSaidaDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));
        return toSaidaDTO(usuario);
    }

    // READ - por perfil
    public List<UsuarioSaidaDTO> listarPorPerfil(PerfilEnum perfil) {
        return usuarioRepository.findByPerfil(perfil)
                .stream()
                .map(this::toSaidaDTO)
                .toList();
    }

    // UPDATE
    public UsuarioSaidaDTO atualizar(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));

        usuarioRepository.findByEmail(dto.getEmail()).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new RuntimeException("E-mail já cadastrado: " + dto.getEmail());
            }
        });

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setPerfil(dto.getPerfil());

        // Só atualiza a senha se uma nova foi informada
        String novaSenha = dto.getSenha();
        if (novaSenha != null && !novaSenha.isBlank()) {
            usuario.setSenhaHash(hashSenha(novaSenha));
        }

        Usuario atualizado = usuarioRepository.save(usuario);
        return toSaidaDTO(atualizado);
    }

    // DELETE
    public void deletar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado com ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // --- helpers ---

    private UsuarioSaidaDTO toSaidaDTO(Usuario usuario) {
        return new UsuarioSaidaDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil().name(),
                usuario.getDataCriacao()
        );
    }

    // Substituir por BCryptPasswordEncoder quando integrar Spring Security
    private String hashSenha(String senha) {
        return senha; // TODO: bcrypt
    }
}