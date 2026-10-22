package com.facilitalab.service;

import com.facilitalab.exception.AcessoNegadoException;
import com.facilitalab.exception.TransicaoInvalidaException;
import com.facilitalab.models.EstadoEnum;
import com.facilitalab.models.PerfilEnum;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class EstadoTransicaoValidator {

    private static final Map<EstadoEnum, Set<EstadoEnum>> TRANSICOES = Map.of(
        EstadoEnum.AGUARDANDO_TRIAGEM, Set.of(
            EstadoEnum.AGUARDANDO_INFORMACOES,
            EstadoEnum.EM_ANALISE,
            EstadoEnum.CANCELADO
        ),
        EstadoEnum.AGUARDANDO_INFORMACOES, Set.of(
            EstadoEnum.AGUARDANDO_TRIAGEM,
            EstadoEnum.CANCELADO
        ),
        EstadoEnum.EM_ANALISE, Set.of(
            EstadoEnum.EM_MODELAGEM,
            EstadoEnum.CANCELADO
        ),
        EstadoEnum.EM_MODELAGEM, Set.of(
            EstadoEnum.AGUARDANDO_APROVACAO_DESIGN,
            EstadoEnum.CANCELADO
        ),
        EstadoEnum.AGUARDANDO_APROVACAO_DESIGN, Set.of(
            EstadoEnum.EM_CORRECAO,
            EstadoEnum.USINAGEM,
            EstadoEnum.CANCELADO
        ),
        EstadoEnum.EM_CORRECAO, Set.of(
            EstadoEnum.AGUARDANDO_APROVACAO_DESIGN,
            EstadoEnum.CANCELADO
        ),
        EstadoEnum.USINAGEM, Set.of(
            EstadoEnum.FINALIZADO,
            EstadoEnum.CANCELADO
        ),
        EstadoEnum.FINALIZADO, Set.of(),
        EstadoEnum.CANCELADO, Set.of()
    );
    

    public void validar(EstadoEnum estadoAtual, EstadoEnum novoEstado) {
        Set<EstadoEnum> permitidos = TRANSICOES.getOrDefault(estadoAtual, Set.of());
        if (!permitidos.contains(novoEstado)) {
            throw new TransicaoInvalidaException(estadoAtual.name(), novoEstado.name());
        }
    }

    private static String chave(EstadoEnum atual, EstadoEnum novo) {
    return atual.name() + ">" + novo.name();
}

private static final Map<String, Set<PerfilEnum>> ATORES = Map.ofEntries(
    Map.entry("AGUARDANDO_TRIAGEM>AGUARDANDO_INFORMACOES", Set.of(PerfilEnum.RECEPCAO)),
    Map.entry("AGUARDANDO_TRIAGEM>EM_ANALISE",             Set.of(PerfilEnum.RECEPCAO)),
    Map.entry("AGUARDANDO_INFORMACOES>AGUARDANDO_TRIAGEM", Set.of(PerfilEnum.DENTISTA)),
    Map.entry("EM_ANALISE>EM_MODELAGEM",                   Set.of(PerfilEnum.CADISTA)),
    Map.entry("EM_MODELAGEM>AGUARDANDO_APROVACAO_DESIGN",  Set.of(PerfilEnum.CADISTA)),
    Map.entry("AGUARDANDO_APROVACAO_DESIGN>EM_CORRECAO",   Set.of(PerfilEnum.DENTISTA)),
    Map.entry("AGUARDANDO_APROVACAO_DESIGN>USINAGEM",      Set.of(PerfilEnum.DENTISTA)),
    Map.entry("EM_CORRECAO>AGUARDANDO_APROVACAO_DESIGN",   Set.of(PerfilEnum.CADISTA)),
    Map.entry("USINAGEM>FINALIZADO",                       Set.of(PerfilEnum.RECEPCAO, PerfilEnum.CADISTA))
);

public void validar(EstadoEnum estadoAtual, EstadoEnum novoEstado, PerfilEnum perfil) {
    validar(estadoAtual, novoEstado); // continua checando se a transição existe

    if (perfil == PerfilEnum.GESTOR) {
        return; // gestor pode tudo
    }

    Set<PerfilEnum> autorizados = (novoEstado == EstadoEnum.CANCELADO)
            ? Set.of(PerfilEnum.RECEPCAO)
            : ATORES.getOrDefault(chave(estadoAtual, novoEstado), Set.of());

    if (!autorizados.contains(perfil)) {
        throw new AcessoNegadoException();
    }
    }
}