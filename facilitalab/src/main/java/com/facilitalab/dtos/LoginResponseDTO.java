package com.facilitalab.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
    private Long id;
    private String nome;
    private String perfil;
}