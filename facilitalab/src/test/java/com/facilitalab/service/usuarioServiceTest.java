package com.facilitalab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.facilitalab.dtos.UsuarioCreateDTO;
import com.facilitalab.dtos.UsuarioSaidaDTO;
import com.facilitalab.models.PerfilEnum;
import com.facilitalab.models.Usuario;
import com.facilitalab.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class usuarioServiceTest {
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder; // Injetado via @Mock, NÃO pelo construtor!

    @InjectMocks
    private UsuarioService usuarioService;


    @Test
    void deveCriarUsuario() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO();
        dto.setNome("Neymar JR");
        dto.setEmail("neymar@email.com");
        dto.setSenha("123456");
        dto.setCpf("01052876385");
        dto.setPerfil(PerfilEnum.GESTOR);

        when(usuarioRepository.existsByEmail("neymar@email.com")).thenReturn(Boolean.valueOf(false));
        when(usuarioRepository.existsByCpf("01052876385")).thenReturn(Boolean.valueOf(false));

        org.mockito.Mockito.when(passwordEncoder.encode("123456")).thenReturn("senha_hash_123");

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setId(Long.valueOf(10L));
        usuarioSalvo.setNome("Neymar JR");
        usuarioSalvo.setEmail("neymar@email.com");
        usuarioSalvo.setCpf("01052876385");
        usuarioSalvo.setPerfil(PerfilEnum.GESTOR);

        org.mockito.Mockito.when(usuarioRepository.save(org.mockito.Mockito.any(Usuario.class)))
                .thenReturn(usuarioSalvo);

        UsuarioSaidaDTO resultado = usuarioService.criar(dto);

        assertEquals(10L, resultado.getId());
        assertEquals("Neymar JR", resultado.getNome());
        org.mockito.Mockito.verify(usuarioRepository, org.mockito.Mockito.times(1))
                .save(org.mockito.Mockito.any(Usuario.class));


    }



    @Test
    void deveBuscarUsuarioPorId() {

        Long idUsuario = (Long) 1L;
        Usuario usuarioEsperado = new Usuario();
        usuarioEsperado.setId(idUsuario);
        usuarioEsperado.setNome("João Silva");
        usuarioEsperado.setPerfil(PerfilEnum.DENTISTA);

        when(usuarioRepository.findById(idUsuario))
                .thenReturn(Optional.of(usuarioEsperado));

        UsuarioSaidaDTO resultado = usuarioService.buscarPorId(idUsuario);

        assertEquals(usuarioEsperado.getId(), resultado.getId());
        assertEquals("João Silva", resultado.getNome());
    }

    @Test
    void deveTirarPontosETraçoDoCpf() {
        String cpfComPontosETraco = "123.456.789-01";
        String cpfEsperado = "12345678901";

        String resultado = usuarioService.normalizarCpf(cpfComPontosETraco);

        assertEquals(cpfEsperado, resultado);
    }

    @Test
    void deveTirarCaracteresEspeciaisDoCpf() {
        String cpfComCaracteresEspeciais = "123.456.789-01@#";
        String cpfEsperado = "12345678901";

        String resultado = usuarioService.normalizarCpf(cpfComCaracteresEspeciais);

        assertEquals(cpfEsperado, resultado);
    }


}

