package com.facilitalab.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class UsuarioSaidaDTO {

    private Long id;
    private String nome;
    private String email;
    private String perfil;
    private LocalDateTime dataCriacao;
    // Comuns obrigatórios
    private String cpf;
    private String telefone;

    // Exclusivos de funcionário
    private BigDecimal salario;
    private String cep;

    // Exclusivo de dentista
    private String cro;
}
