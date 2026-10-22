package com.facilitalab.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DevolucaoDTO {

    @NotBlank(message = "O motivo da devolução é obrigatório")
    @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
    private String motivo;
}