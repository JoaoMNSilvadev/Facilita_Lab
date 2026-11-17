package com.facilitalab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.facilitalab.models.ArquivoPedido;
import com.facilitalab.models.TipoArquivoEnum;

public interface ArquivoPedidoRepository extends JpaRepository<ArquivoPedido, Long> {

    Optional<ArquivoPedido> findTopByPedidoIdAndTipoOrderByVersaoDesc(Long pedidoId, TipoArquivoEnum tipo);

    List<ArquivoPedido> findByPedidoIdOrderByDataEnvioAsc(Long pedidoId);

    @Transactional
    void deleteByPedidoId(Long pedidoId);
}