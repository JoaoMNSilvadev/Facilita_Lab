package com.facilitalab.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

@Entity
@Table(name = "usuario")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "email", unique = true, length = 250)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "perfil")
    @Enumerated(EnumType.STRING)
    private PerfilEnum perfil;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @PrePersist
    public void prepersist() {
        this.dataCriacao = LocalDateTime.now();
    }

    @Column(name = "cpf", nullable = false, length = 14, unique = true)
    private String cpf;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    // Só funcionários (GESTOR, RECEPCAO, CADISTA)
    @Column(name = "salario")
    private BigDecimal salario;

    @Column(name = "cep", length = 9)
    private String cep;

    // Só DENTISTA
    @Column(name = "cro", length = 20)
    private String cro;
    
}
