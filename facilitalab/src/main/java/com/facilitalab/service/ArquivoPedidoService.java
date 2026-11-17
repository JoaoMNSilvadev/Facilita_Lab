package com.facilitalab.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.facilitalab.dtos.ArquivoSaidaDTO;
import com.facilitalab.exception.AcessoNegadoException;
import com.facilitalab.models.ArquivoPedido;
import com.facilitalab.models.EstadoEnum;
import com.facilitalab.models.Pedido;
import com.facilitalab.models.PerfilEnum;
import com.facilitalab.models.TipoArquivoEnum;
import com.facilitalab.models.Usuario;
import com.facilitalab.repository.ArquivoPedidoRepository;
import com.facilitalab.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArquivoPedidoService {

    private static final long TAMANHO_MAXIMO = 100L * 1024 * 1024; // 100 MB (RF-13)

    private final ArquivoPedidoRepository arquivoRepository;
    private final PedidoRepository pedidoRepository;
    private final ArquivoStorageService storage;

    public record Download(String nome, Resource recurso) {}

    private Usuario autenticado() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void verificarAcesso(Pedido pedido, Usuario usuario) {
        if (usuario.getPerfil() == PerfilEnum.DENTISTA
                && !pedido.getDentista().getId().equals(usuario.getId())) {
            throw new AcessoNegadoException();
        }
    }

    private Pedido buscarPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }

    public void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("O arquivo STL é obrigatório.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new MaxUploadSizeExceededException(TAMANHO_MAXIMO);
        }
        String nome = arquivo.getOriginalFilename();
        if (nome == null || !nome.toLowerCase().endsWith(".stl")) {
            throw new IllegalArgumentException("Somente arquivos .stl são aceitos.");
        }
    }

    // Grava o arquivo e cria a próxima versão (v+1) do tipo informado
    @Transactional
    public ArquivoPedido armazenar(Pedido pedido, MultipartFile arquivo, TipoArquivoEnum tipo, Usuario autor) {
        validarArquivo(arquivo);

        int proximaVersao = arquivoRepository
                .findTopByPedidoIdAndTipoOrderByVersaoDesc(pedido.getId(), tipo)
                .map(a -> a.getVersao() + 1)
                .orElse(1);

        String nomeArmazenado = storage.salvar(arquivo);

        return arquivoRepository.save(new ArquivoPedido(
                pedido, tipo, proximaVersao, arquivo.getOriginalFilename(),
                nomeArmazenado, arquivo.getSize(), autor));
    }

    // Dentista envia um STL corrigido enquanto o pedido aguarda informações
    @Transactional
    public ArquivoSaidaDTO enviarNovaVersao(Long pedidoId, MultipartFile arquivo) {
        Pedido pedido = buscarPedido(pedidoId);
        Usuario usuario = autenticado();

        if (usuario.getPerfil() != PerfilEnum.DENTISTA) {
            throw new AcessoNegadoException();
        }
        verificarAcesso(pedido, usuario);

        if (pedido.getEstado() != EstadoEnum.AGUARDANDO_INFORMACOES) {
            throw new IllegalArgumentException(
                    "Só é possível enviar um novo STL enquanto o pedido aguarda informações.");
        }
        return converter(armazenar(pedido, arquivo, TipoArquivoEnum.STL_ORIGINAL, usuario));
    }

    public List<ArquivoSaidaDTO> listar(Long pedidoId) {
        Pedido pedido = buscarPedido(pedidoId);
        verificarAcesso(pedido, autenticado());
        return arquivoRepository.findByPedidoIdOrderByDataEnvioAsc(pedidoId).stream()
                .map(this::converter)
                .collect(Collectors.toList());
    }

    public Download baixar(Long pedidoId, Long arquivoId) {
        Pedido pedido = buscarPedido(pedidoId);
        verificarAcesso(pedido, autenticado());

        ArquivoPedido arquivo = arquivoRepository.findById(arquivoId)
                .filter(a -> a.getPedido().getId().equals(pedidoId))
                .orElseThrow(() -> new RuntimeException("Arquivo não encontrado"));

        return new Download(arquivo.getNomeOriginal(), storage.carregar(arquivo.getNomeArmazenado()));
    }

    // Usado ao excluir o pedido inteiro
    @Transactional
    public void removerDoPedido(Long pedidoId) {
        List<ArquivoPedido> arquivos = arquivoRepository.findByPedidoIdOrderByDataEnvioAsc(pedidoId);
        arquivoRepository.deleteByPedidoId(pedidoId);
        arquivos.forEach(a -> storage.remover(a.getNomeArmazenado()));
    }

    private ArquivoSaidaDTO converter(ArquivoPedido a) {
        return new ArquivoSaidaDTO(a.getId(), a.getTipo(), a.getVersao(), a.getNomeOriginal(),
                a.getTamanhoBytes(), a.getEnviadoPor().getNome(), a.getEnviadoPor().getPerfil(),
                a.getDataEnvio());
    }
}