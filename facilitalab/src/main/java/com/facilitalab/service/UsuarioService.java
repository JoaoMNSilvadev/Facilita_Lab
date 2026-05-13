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

        boolean isFuncionario = List.of(PerfilEnum.GESTOR, PerfilEnum.RECEPCAO, PerfilEnum.CADISTA)
                .contains(dto.getPerfil());
        boolean isDentista = dto.getPerfil() == PerfilEnum.DENTISTA;

        if (isFuncionario) {
            if (dto.getSalario() == null)
                throw new IllegalArgumentException("Salário é obrigatório para funcionários.");
            if (dto.getCep() == null || dto.getCep().isBlank())
                throw new IllegalArgumentException("CEP é obrigatório para funcionários.");
        }

        if (isDentista) {
            if (dto.getCro() == null || dto.getCro().isBlank())
                throw new IllegalArgumentException("CRO é obrigatório para dentistas.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenhaHash(hashSenha(dto.getSenha()));
        usuario.setPerfil(dto.getPerfil());
        usuario.setCpf(dto.getCpf());
        usuario.setTelefone(dto.getTelefone());
        usuario.setSalario(dto.getSalario());
        usuario.setCep(dto.getCep());
        usuario.setCro(dto.getCro());

        return toSaidaDTO(usuarioRepository.save(usuario));
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

        boolean isFuncionario = List.of(PerfilEnum.GESTOR, PerfilEnum.RECEPCAO, PerfilEnum.CADISTA)
                .contains(dto.getPerfil());
        boolean isDentista = dto.getPerfil() == PerfilEnum.DENTISTA;

        if (isFuncionario) {
            if (dto.getSalario() == null)
                throw new IllegalArgumentException("Salário é obrigatório para funcionários.");
            if (dto.getCep() == null || dto.getCep().isBlank())
                throw new IllegalArgumentException("CEP é obrigatório para funcionários.");
        }

        if (isDentista) {
            if (dto.getCro() == null || dto.getCro().isBlank())
                throw new IllegalArgumentException("CRO é obrigatório para dentistas.");
        }

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setPerfil(dto.getPerfil());
        usuario.setCpf(dto.getCpf());
        usuario.setTelefone(dto.getTelefone());
        usuario.setSalario(isFuncionario ? dto.getSalario() : null);
        usuario.setCep(isFuncionario ? dto.getCep() : null);
        usuario.setCro(isDentista ? dto.getCro() : null);

        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuario.setSenhaHash(hashSenha(dto.getSenha()));
        }

        return toSaidaDTO(usuarioRepository.save(usuario));
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
                usuario.getDataCriacao(),
                usuario.getCpf(),
                usuario.getTelefone(),
                usuario.getSalario(),
                usuario.getCep(),
                usuario.getCro()
        );
    }

    // Substituir por BCryptPasswordEncoder quando integrar Spring Security
    private String hashSenha(String senha) {
        return senha; // TODO: bcrypt
    }
}