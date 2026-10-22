package com.facilitalab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.facilitalab.models.EstadoEnum;
import com.facilitalab.models.HistoricoPedido;

public interface HistoricoPedidoRepository extends JpaRepository<HistoricoPedido, Long> {

    List<HistoricoPedido> findByPedidoIdOrderByDataHoraAsc(Long pedidoId);

    @Transactional
    void deleteByPedidoId(Long pedidoId); // usado só ao excluir o pedido inteiro

    Optional<HistoricoPedido> findFirstByPedidoIdAndEstadoNovoOrderByDataHoraDesc(Long pedidoId, EstadoEnum estadoNovo);
}