package com.facilitalab.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.facilitalab.dtos.HistoricoSaidaDTO;
import com.facilitalab.models.HistoricoPedido;
import com.facilitalab.repository.HistoricoPedidoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.facilitalab.dtos.PedidoCreateDTO;
import com.facilitalab.dtos.PedidoSaidaDTO;
import com.facilitalab.exception.AcessoNegadoException;
import com.facilitalab.models.EstadoEnum;
import com.facilitalab.models.Pedido;
import com.facilitalab.models.PerfilEnum;
import com.facilitalab.models.PrioridadeEnum;
import com.facilitalab.models.TipoArquivoEnum;
import com.facilitalab.models.Usuario;
import com.facilitalab.repository.PedidoRepository;
import com.facilitalab.repository.UsuarioRepository;

@Service
public class PedidoService {

    private final HistoricoPedidoRepository historicoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoTransicaoValidator estadoTransicaoValidator;
    private final ArquivoPedidoService arquivoService;

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository,
            EstadoTransicaoValidator estadoTransicaoValidator, HistoricoPedidoRepository historicoRepository, ArquivoPedidoService arquivoService   ) {
        this.historicoRepository = historicoRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoTransicaoValidator = estadoTransicaoValidator;
        this.arquivoService = arquivoService;   
    }

     private PedidoSaidaDTO converter(Pedido pedido) {
        PedidoSaidaDTO saida = new PedidoSaidaDTO();
        saida.setId(pedido.getId());
        saida.setCor(pedido.getCor());
        saida.setMaterial(pedido.getMaterial());
        saida.setTipoProtese(pedido.getTipoProtese());
        saida.setPrazoEntrega(pedido.getPrazoEntrega());
        saida.setEstado(pedido.getEstado());
        saida.setPrioridade(pedido.getPrioridade());
        saida.setObservacoes(pedido.getObservacoes());
        saida.setDataCriacao(pedido.getDataCriacao());
        saida.setDataAtualizacao(pedido.getDataAtualizacao());
        saida.setDentistaId(pedido.getDentista().getId());
        saida.setNomeDentista(pedido.getDentista().getNome());
        saida.setNomeCadista(pedido.getCadista() != null ? pedido.getCadista().getNome() : null);

        if (pedido.getEstado() == EstadoEnum.AGUARDANDO_INFORMACOES) {
            historicoRepository
                    .findFirstByPedidoIdAndEstadoNovoOrderByDataHoraDesc(pedido.getId(),
                            EstadoEnum.AGUARDANDO_INFORMACOES)
                    .ifPresent(h -> saida.setMotivoDevolucao(h.getObservacao()));
        }

        if (pedido.getEstado() == EstadoEnum.EM_CORRECAO) {
            historicoRepository
                    .findFirstByPedidoIdAndEstadoNovoOrderByDataHoraDesc(pedido.getId(),
                            EstadoEnum.EM_CORRECAO)
                    .ifPresent(h -> saida.setMotivoRejeicao(h.getObservacao()));
        }

        return saida;
    }

    // CREATE
    @Transactional
public PedidoSaidaDTO criarPedido(PedidoCreateDTO dto, MultipartFile arquivo) {
    arquivoService.validarArquivo(arquivo);   // RF-07: sem STL válido, nada é criado

    Usuario autenticado = getUsuarioAutenticado();

    Usuario dentista = usuarioRepository.findById(autenticado.getId())
            .orElseThrow(() -> new RuntimeException("Dentista não encontrado: " + autenticado.getId()));

    Pedido pedido = new Pedido();
    pedido.setCor(dto.getCor());
    pedido.setMaterial(dto.getMaterial());
    pedido.setTipoProtese(dto.getTipoProtese());
    pedido.setPrazoEntrega(dto.getPrazoEntrega());
    pedido.setDentista(dentista);
    pedido.setObservacoes(dto.getObservacoes());
    pedido.setPrioridade(PrioridadeEnum.NORMAL);
    pedido.setEstado(EstadoEnum.AGUARDANDO_TRIAGEM);

    Pedido salvo = pedidoRepository.save(pedido);
    arquivoService.armazenar(salvo, arquivo, TipoArquivoEnum.STL_ORIGINAL, autenticado);
    registrarHistorico(salvo, null, EstadoEnum.AGUARDANDO_TRIAGEM, autenticado, null);
    return converter(salvo);
}

    // READ - por ID
    public PedidoSaidaDTO buscarPedidoPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        verificarAcessoDentista(pedido, getUsuarioAutenticado());
        return converter(pedido);
    }

    // READ - por dentista
    public List<PedidoSaidaDTO> buscarPedidosPorDentistaId(Long dentistaId) {
        Usuario autenticado = getUsuarioAutenticado();
        if (autenticado.getPerfil() == PerfilEnum.DENTISTA &&
                !autenticado.getId().equals(dentistaId)) {
            throw new AcessoNegadoException();
        }
        return pedidoRepository.findByDentistaId(dentistaId).stream()
                .map(this::converter)
                .collect(Collectors.toList());
    }

    // READ - por cadista
    public List<PedidoSaidaDTO> buscarPedidosPorCadistaId(Long cadistaId) {
        List<Pedido> pedidos = pedidoRepository.findByCadistaId(cadistaId);
        return pedidos.stream()
                .map(this::converter)
                .collect(Collectors.toList());
    }

    // READ - por estado
    public List<PedidoSaidaDTO> buscarPedidosPorEstado(EstadoEnum estado) {
        List<Pedido> pedidos = pedidoRepository.findByEstado(estado);
        return pedidos.stream()
                .map(this::converter)
                .collect(Collectors.toList());
    }

    // READ - por prioridade
    public List<PedidoSaidaDTO> buscarPedidosPorPrioridade(PrioridadeEnum prioridade) {
        List<Pedido> pedidos = pedidoRepository.findByPrioridade(prioridade);
        return pedidos.stream()
                .map(this::converter)
                .collect(Collectors.toList());
    }

    // READ - todos
    public List<PedidoSaidaDTO> buscarTodosPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        return pedidos.stream()
                .map(this::converter)
                .collect(Collectors.toList());
    }

    // UPDATE - estado
    @Transactional
    public PedidoSaidaDTO atualizarEstadoPedido(Long id, EstadoEnum novoEstado, Long cadistaId) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (novoEstado == EstadoEnum.AGUARDANDO_INFORMACOES) {
            throw new IllegalArgumentException(
                    "Para devolver um pedido é obrigatório informar o motivo (PUT /pedidos/{id}/devolver).");
        }

        if (novoEstado == EstadoEnum.EM_CORRECAO) {
    throw new IllegalArgumentException("Para rejeitar o design é obrigatório informar a observação (PUT /pedidos/{id}/rejeitar-design).");
}

        Usuario autenticado = getUsuarioAutenticado();
        verificarAcessoDentista(pedido, autenticado);
        estadoTransicaoValidator.validar(pedido.getEstado(), novoEstado, autenticado.getPerfil());

        if (novoEstado == EstadoEnum.AGUARDANDO_APROVACAO_DESIGN) {
    arquivoService.exigirDesignAtual(pedido);   // RN-03
}

        EstadoEnum estadoAnterior = pedido.getEstado(); // <-- novo
        pedido.setEstado(novoEstado);

        // Atribui o cadista quando o pedido entra em análise
        if (novoEstado == EstadoEnum.EM_ANALISE && cadistaId != null) {
            Usuario cadista = usuarioRepository.findById(cadistaId)
                    .orElseThrow(() -> new RuntimeException("Cadista não encontrado"));
            pedido.setCadista(cadista);
        }

        Pedido salvo = pedidoRepository.save(pedido);
        registrarHistorico(salvo, estadoAnterior, novoEstado, autenticado, null); // <-- novo
        return converter(salvo);
    }

    // UPDATE
    public PedidoSaidaDTO atualizarPedido(Long id, PedidoCreateDTO dto) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        verificarAcessoDentista(pedido, getUsuarioAutenticado());

        Usuario dentista = usuarioRepository.findById(dto.getDentistaId())
                .orElseThrow(() -> new RuntimeException("Dentista não encontrado"));

        pedido.setDentista(dentista);
        pedido.setCor(dto.getCor());
        pedido.setMaterial(dto.getMaterial());
        pedido.setTipoProtese(dto.getTipoProtese());
        pedido.setPrazoEntrega(dto.getPrazoEntrega());
        pedido.setObservacoes(dto.getObservacoes());
        pedido.setPrioridade(dto.getPrioridade());

        return converter(pedidoRepository.save(pedido));
    }

    public List<HistoricoSaidaDTO> buscarHistorico(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        verificarAcessoDentista(pedido, getUsuarioAutenticado());

        return historicoRepository.findByPedidoIdOrderByDataHoraAsc(pedidoId).stream()
                .map(h -> new HistoricoSaidaDTO(
                        h.getId(), h.getEstadoAnterior(), h.getEstadoNovo(),
                        h.getUsuario().getNome(), h.getPerfil(),
                        h.getDataHora(), h.getObservacao()))
                .collect(Collectors.toList());
    }

    @Transactional
    public PedidoSaidaDTO devolverPedido(Long id, String motivo) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        Usuario autenticado = getUsuarioAutenticado();
        estadoTransicaoValidator.validar(pedido.getEstado(), EstadoEnum.AGUARDANDO_INFORMACOES,
                autenticado.getPerfil());

        EstadoEnum anterior = pedido.getEstado();
        pedido.setEstado(EstadoEnum.AGUARDANDO_INFORMACOES);
        Pedido salvo = pedidoRepository.save(pedido);

        registrarHistorico(salvo, anterior, EstadoEnum.AGUARDANDO_INFORMACOES, autenticado, motivo.trim());
        return converter(salvo);
    }

    @Transactional
public PedidoSaidaDTO rejeitarDesign(Long id, String observacao) {
    Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

    Usuario autenticado = getUsuarioAutenticado();
    verificarAcessoDentista(pedido, autenticado);
    estadoTransicaoValidator.validar(pedido.getEstado(), EstadoEnum.EM_CORRECAO, autenticado.getPerfil());

    int versaoRejeitada = arquivoService.ultimaVersaoDesign(id);
    pedido.setVersaoDesignMinima(versaoRejeitada + 1);   // RN-04: passa a exigir v+1

    EstadoEnum anterior = pedido.getEstado();
    pedido.setEstado(EstadoEnum.EM_CORRECAO);
    Pedido salvo = pedidoRepository.save(pedido);

    registrarHistorico(salvo, anterior, EstadoEnum.EM_CORRECAO, autenticado,
            "Design v" + versaoRejeitada + " rejeitado: " + observacao.trim());
    return converter(salvo);
}

    // DELETE
    @Transactional
public void deletarPedido(Long id) {
    if (!pedidoRepository.existsById(id)) {
        throw new RuntimeException("Pedido não encontrado");
    }
    arquivoService.removerDoPedido(id);
    historicoRepository.deleteByPedidoId(id);
    pedidoRepository.deleteById(id);
}  

    private void registrarHistorico(Pedido pedido, EstadoEnum anterior, EstadoEnum novo,
            Usuario usuario, String observacao) {
        historicoRepository.save(new HistoricoPedido(pedido, anterior, novo, usuario, observacao));
    }

    private Usuario getUsuarioAutenticado() {
        return (Usuario) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private void verificarAcessoDentista(Pedido pedido, Usuario autenticado) {
        if (autenticado.getPerfil() == PerfilEnum.DENTISTA &&
                !pedido.getDentista().getId().equals(autenticado.getId())) {
            throw new AcessoNegadoException();
        }
    }
}
