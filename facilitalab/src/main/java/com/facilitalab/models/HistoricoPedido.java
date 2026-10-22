package com.facilitalab.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "historico_pedido")
public class HistoricoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id", updatable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", updatable = false)
    private EstadoEnum estadoAnterior; // null quando é a criação do pedido

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_novo", nullable = false, updatable = false)
    private EstadoEnum estadoNovo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", updatable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, updatable = false)
    private PerfilEnum perfil; // perfil no momento da ação

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Column(name = "observacao", length = 500, updatable = false)
    private String observacao;

    public HistoricoPedido(Pedido pedido, EstadoEnum anterior, EstadoEnum novo,
                           Usuario usuario, String observacao) {
        this.pedido = pedido;
        this.estadoAnterior = anterior;
        this.estadoNovo = novo;
        this.usuario = usuario;
        this.perfil = usuario.getPerfil();
        this.observacao = observacao;
        this.dataHora = LocalDateTime.now();
    }
}