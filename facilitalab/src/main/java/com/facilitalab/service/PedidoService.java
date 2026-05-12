package com.facilitalab.service;

import org.springframework.security.core.parameters.P;

import com.facilitalab.dtos.PedidoCreateDTO;
import com.facilitalab.models.EstadoEnum;
import com.facilitalab.models.Pedido;
import com.facilitalab.models.PrioridadeEnum;
import com.facilitalab.repository.PedidoRepository;

public class PedidoService {

    private final PedidoRepository pedidoRepository;

        public PedidoService(PedidoRepository pedidoRepository) {
            this.pedidoRepository = pedidoRepository;
        }

    
        public void deletePedido(Long id) {
            if (!pedidoRepository.existsById(id)) {
                throw new RuntimeException("Pedido não encontrado");
            }
            pedidoRepository.deleteById(id);
        }

        public void buscarPedidoPorId(Long id) {
            if (!pedidoRepository.existsById(id)) {
                throw new RuntimeException("Pedido não encontrado");
            }
            pedidoRepository.findById(id);
        }

        public void buscarPedidosPorDentistaId(Long dentistaId) {
            pedidoRepository.findByDentistaId(dentistaId);
        }

        public void buscarPedidosPorCadistaId(Long cadistaId) {
            pedidoRepository.findByCadistaId(cadistaId);
        }

        public void buscarPedidosPorEstado(EstadoEnum estado) {
            pedidoRepository.findByEstado(estado);
        }

        public void buscarPedidosPorPrioridade(PrioridadeEnum prioridade) {
            pedidoRepository.findByPrioridade(prioridade);
        }

        public void buscarTodosPedidos() {
            pedidoRepository.findAll();
        }

        public void criarPedido(PedidoCreateDTO dto) {
            Pedido pedido = new Pedido();
            pedido.setCor(dto.getCor());
            pedido.setMaterial(dto.getMaterial());
            pedido.setTipoProtese(dto.getTipoProtese());
            pedido.setPrazoEntrega(dto.getPrazoEntrega());
            pedido.setDataCriacao(java.time.LocalDateTime.now());
           //pedido.setCadistaId(dto.getCadistaId());
            pedido.setDentista(dto.getDentistaId());
            pedido.setObservacoes(dto.getObservacoes());
            pedido.setPrioridade(PrioridadeEnum.NORMAL);
            pedido.setEstado(EstadoEnum.AGUARDANDO_TRIAGEM);
            
            pedidoRepository.save(pedido);
        }

}
