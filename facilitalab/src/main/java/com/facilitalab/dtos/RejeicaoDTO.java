package com.facilitalab.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RejeicaoDTO {

    @NotBlank(message = "A observação da rejeição é obrigatória")
    @Size(max = 450, message = "A observação deve ter no máximo 450 caracteres")
    private String observacao;
}