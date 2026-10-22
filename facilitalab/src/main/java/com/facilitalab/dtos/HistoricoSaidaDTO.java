package com.facilitalab.dtos;

import java.time.LocalDateTime;

import com.facilitalab.models.EstadoEnum;
import com.facilitalab.models.PerfilEnum;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HistoricoSaidaDTO {
    private Long id;
    private EstadoEnum estadoAnterior;
    private EstadoEnum estadoNovo;
    private String nomeUsuario;
    private PerfilEnum perfil;
    private LocalDateTime dataHora;
    private String observacao;

    public HistoricoSaidaDTO(Long id, EstadoEnum estadoAnterior, EstadoEnum estadoNovo,
            String nomeUsuario, PerfilEnum perfil, LocalDateTime dataHora, String observacao) {
        this.id = id;
        this.estadoAnterior = estadoAnterior;
        this.estadoNovo = estadoNovo;
        this.nomeUsuario = nomeUsuario;
        this.perfil = perfil;
        this.dataHora = dataHora;
        this.observacao = observacao;
    }
}