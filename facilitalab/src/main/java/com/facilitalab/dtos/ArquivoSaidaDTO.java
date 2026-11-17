package com.facilitalab.dtos;

import java.time.LocalDateTime;

import com.facilitalab.models.PerfilEnum;
import com.facilitalab.models.TipoArquivoEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArquivoSaidaDTO {
    private Long id;
    private TipoArquivoEnum tipo;
    private Integer versao;
    private String nomeOriginal;
    private Long tamanhoBytes;
    private String nomeAutor;
    private PerfilEnum perfilAutor;
    private LocalDateTime dataEnvio;
}