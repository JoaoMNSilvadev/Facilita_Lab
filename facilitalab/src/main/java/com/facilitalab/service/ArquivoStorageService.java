package com.facilitalab.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ArquivoStorageService {

    private final Path raiz;

    public ArquivoStorageService(@Value("${app.upload.dir:uploads}") String diretorio) throws IOException {
        this.raiz = Path.of(diretorio).toAbsolutePath().normalize();
        Files.createDirectories(raiz);
    }

    public String salvar(MultipartFile arquivo) {
        String nomeArmazenado = UUID.randomUUID() + ".stl";
        try (InputStream in = arquivo.getInputStream()) {
            Files.copy(in, raiz.resolve(nomeArmazenado));
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gravar o arquivo no servidor.", e);
        }
        return nomeArmazenado;
    }

    public Resource carregar(String nomeArmazenado) {
        Path caminho = raiz.resolve(nomeArmazenado).normalize();
        if (!caminho.startsWith(raiz) || !Files.exists(caminho)) {
            throw new RuntimeException("Arquivo não encontrado");
        }
        return new FileSystemResource(caminho);
    }

    public void remover(String nomeArmazenado) {
        try {
            Files.deleteIfExists(raiz.resolve(nomeArmazenado).normalize());
        } catch (IOException ignorado) {
            // arquivo órfão não impede a operação principal
        }
    }
}