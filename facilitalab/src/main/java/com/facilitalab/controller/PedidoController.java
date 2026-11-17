package com.facilitalab.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.facilitalab.dtos.ArquivoSaidaDTO;
import com.facilitalab.dtos.DevolucaoDTO;
import com.facilitalab.dtos.HistoricoSaidaDTO;
import com.facilitalab.dtos.PedidoCreateDTO;
import com.facilitalab.dtos.PedidoSaidaDTO;
import com.facilitalab.exception.AcessoNegadoException;
import com.facilitalab.exception.TransicaoInvalidaException;
import com.facilitalab.models.EstadoEnum;
import com.facilitalab.models.PrioridadeEnum;
import com.facilitalab.service.ArquivoPedidoService;
import com.facilitalab.service.PedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ArquivoPedidoService arquivoService;

    public PedidoController(PedidoService pedidoService, ArquivoPedidoService arquivoService) {
        this.pedidoService = pedidoService;
        this.arquivoService = arquivoService;
    }

    // POST /pedidos  (multipart: parte "dados" em JSON + parte "arquivo" .stl)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PedidoSaidaDTO> criarPedido(
            @RequestPart("dados") @Valid PedidoCreateDTO dto,
            @RequestPart("arquivo") MultipartFile arquivo) {
        return ResponseEntity.status(201).body(pedidoService.criarPedido(dto, arquivo));
    }
    
    // POST /pedidos/{id}/arquivos  (dentista envia nova versão do STL)
    @PostMapping(value = "/{id}/arquivos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArquivoSaidaDTO> enviarArquivo(
            @PathVariable Long id,
            @RequestPart("arquivo") MultipartFile arquivo) {
        return ResponseEntity.status(201).body(arquivoService.enviarNovaVersao(id, arquivo));
    }
    
    // GET /pedidos/{id}/arquivos  (histórico de versões)
    @GetMapping("/{id}/arquivos")
    public ResponseEntity<List<ArquivoSaidaDTO>> listarArquivos(@PathVariable Long id) {
        return ResponseEntity.ok(arquivoService.listar(id));
    }
    
    // GET /pedidos/{id}/arquivos/{arquivoId}/download
    @GetMapping("/{id}/arquivos/{arquivoId}/download")
    public ResponseEntity<Resource> baixarArquivo(@PathVariable Long id, @PathVariable Long arquivoId) {
        ArquivoPedidoService.Download d = arquivoService.baixar(id, arquivoId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(d.nome(), StandardCharsets.UTF_8).build().toString())
                .body(d.recurso());
    }
    
    // RNF-08: arquivo acima de 100 MB
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, List<String>>> handleArquivoGrande(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(413)
                .body(Map.of("errors", List.of("O arquivo excede o limite de 100 MB.")));
    }
    
    // RF-07: parte obrigatória ausente (ex.: sem o arquivo STL)
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, List<String>>> handleParteAusente(MissingServletRequestPartException ex) {
        String msg = "arquivo".equals(ex.getRequestPartName())
                ? "O arquivo STL é obrigatório."
                : "A parte '" + ex.getRequestPartName() + "' é obrigatória.";
        return ResponseEntity.badRequest().body(Map.of("errors", List.of(msg)));
    }
    
    // Falha de gravação no disco
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, List<String>>> handleFalhaInterna(IllegalStateException ex) {
        return ResponseEntity.status(500)
                .body(Map.of("errors", List.of(ex.getMessage())));
    }

    // GET /pedidos/id/{id}
    @GetMapping("/id/{id}")

    public ResponseEntity<PedidoSaidaDTO> buscarPedidoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPedidoPorId(id));
    }

    // GET /pedidos/dentista/{dentistaId}
    @GetMapping("/dentista/{dentistaId}")
    public ResponseEntity<List<PedidoSaidaDTO>> listarPedidosPorDentista(@PathVariable Long dentistaId) {
        return ResponseEntity.ok(pedidoService.buscarPedidosPorDentistaId(dentistaId));
    }

    // GET /pedidos
    @GetMapping
    public ResponseEntity<List<PedidoSaidaDTO>> listarTodosPedidos() {
        return ResponseEntity.ok(pedidoService.buscarTodosPedidos());
    }

    // GET /pedidos/prioridade
    @GetMapping("/prioridade")
    public ResponseEntity<List<PedidoSaidaDTO>> listarPedidosPorPrioridade(@RequestParam PrioridadeEnum prioridade) {
        return ResponseEntity.ok(pedidoService.buscarPedidosPorPrioridade(prioridade));
    }

    // GET /pedidos/cadista/{cadistaId}
    @GetMapping("/cadista/{cadistaId}")
    public ResponseEntity<List<PedidoSaidaDTO>> listarPedidosPorCadista(@PathVariable Long cadistaId) {
        return ResponseEntity.ok(pedidoService.buscarPedidosPorCadistaId(cadistaId));
    }

    // GET /pedidos/estado?estado=AGUARDANDO_TRIAGEM
    @GetMapping("/estado")
    public ResponseEntity<List<PedidoSaidaDTO>> listarPedidosPorEstadoRequestParam(@RequestParam EstadoEnum estado) {
        return ResponseEntity.ok(pedidoService.buscarPedidosPorEstado(estado));
    }

    // GET /pedidos/{id}/historico
    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoSaidaDTO>> buscarHistorico(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarHistorico(id));
    }

    // UPDATE /{id}/estado
    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoSaidaDTO> atualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoEnum novoEstado,
            @RequestParam(required = false) Long cadistaId) {
        return ResponseEntity.ok(pedidoService.atualizarEstadoPedido(id, novoEstado, cadistaId));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<PedidoSaidaDTO> atualizarPedido(@PathVariable Long id,
            @Valid @RequestBody PedidoCreateDTO dto) {
        return ResponseEntity.ok(pedidoService.atualizarPedido(id, dto));
    }

    // PUT /pedidos/{id}/devolver { "motivo": "..." }
    @PutMapping("/{id}/devolver")
    public ResponseEntity<PedidoSaidaDTO> devolver(@PathVariable Long id,
            @Valid @RequestBody DevolucaoDTO dto) {
        return ResponseEntity.ok(pedidoService.devolverPedido(id, dto.getMotivo()));
    }

    // Regra violada pelo cliente (ex.: devolução sem passar pelo endpoint com
    // motivo)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, List<String>>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("errors", List.of(ex.getMessage())));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPedido(@PathVariable Long id) {
        pedidoService.deletarPedido(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> erros = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errors", erros));
    }

    // Recurso não encontrado (busca por ID, e-mail ou CPF inexistente)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, List<String>>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("errors", List.of(ex.getMessage())));
    }

    @ExceptionHandler(TransicaoInvalidaException.class)
    public ResponseEntity<Map<String, List<String>>> handleTransicaoInvalida(TransicaoInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.valueOf(422))
                .body(Map.of("errors", List.of(ex.getMessage())));
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Map<String, List<String>>> handleAcessoNegado(AcessoNegadoException ex) {
        return ResponseEntity.status(403)
                .body(Map.of("errors", List.of(ex.getMessage())));
    }

}
