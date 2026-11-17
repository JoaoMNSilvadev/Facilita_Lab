package com.facilitalab.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "arquivo_pedido",
       uniqueConstraints = @UniqueConstraint(columnNames = {"pedido_id", "tipo", "versao"}))
public class ArquivoPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pedido_id", updatable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, updatable = false)
    private TipoArquivoEnum tipo;

    @Column(name = "versao", nullable = false, updatable = false)
    private Integer versao;

    @Column(name = "nome_original", nullable = false, updatable = false)
    private String nomeOriginal;

    @Column(name = "nome_armazenado", nullable = false, updatable = false)
    private String nomeArmazenado;

    @Column(name = "tamanho_bytes", nullable = false, updatable = false)
    private Long tamanhoBytes;

    @ManyToOne(optional = false)
    @JoinColumn(name = "enviado_por_id", updatable = false)
    private Usuario enviadoPor;

    @Column(name = "data_envio", nullable = false, updatable = false)
    private LocalDateTime dataEnvio;

    public ArquivoPedido(Pedido pedido, TipoArquivoEnum tipo, Integer versao, String nomeOriginal,
                         String nomeArmazenado, Long tamanhoBytes, Usuario enviadoPor) {
        this.pedido = pedido;
        this.tipo = tipo;
        this.versao = versao;
        this.nomeOriginal = nomeOriginal;
        this.nomeArmazenado = nomeArmazenado;
        this.tamanhoBytes = tamanhoBytes;
        this.enviadoPor = enviadoPor;
        this.dataEnvio = LocalDateTime.now();
    }
}