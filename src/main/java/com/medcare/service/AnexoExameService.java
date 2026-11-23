package com.medcare.service;

import com.medcare.model.AnexoExame;
import com.medcare.model.Exame;
import com.medcare.model.Usuario;

import com.medcare.repository.AnexoExameRepository;
import com.medcare.repository.ExameRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class AnexoExameService {

    private final AnexoExameRepository anexoRepository;
    private final ExameRepository exameRepository;

    @Value("${medcare.upload-dir:uploads/exames}")
    private String diretorioUpload;

    private static final long TAMANHO_MAXIMO = 20 * 1024 * 1024;

    private static final Set<String> FORMATOS_PERMITIDOS = Set.of(
            "jpg",
            "jpeg",
            "png",
            "pdf",
            "dcm"
    );

    public AnexoExameService(
            AnexoExameRepository anexoRepository,
            ExameRepository exameRepository) {

        this.anexoRepository = anexoRepository;
        this.exameRepository = exameRepository;
    }

    // ==========================================
    // SALVAR ANEXO
    // ==========================================

    public AnexoExame salvar(
            Long exameId,
            Usuario profissional,
            MultipartFile arquivo) {

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Selecione um arquivo para enviar."
            );
        }

        if (profissional == null) {
            throw new IllegalArgumentException(
                    "Profissional não informado."
            );
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException(
                    "O arquivo não pode ultrapassar 20 MB."
            );
        }

        String nomeOriginal = arquivo.getOriginalFilename();

        if (nomeOriginal == null || nomeOriginal.isBlank()) {
            throw new IllegalArgumentException(
                    "Nome do arquivo inválido."
            );
        }

        String extensao = obterExtensao(nomeOriginal);

        if (!FORMATOS_PERMITIDOS.contains(extensao)) {
            throw new IllegalArgumentException(
                    "Formato não permitido. Utilize JPG, PNG, PDF ou DICOM."
            );
        }

        Exame exame = exameRepository.findById(exameId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Exame não encontrado."
                ));

        if ("Cancelado".equalsIgnoreCase(exame.getStatus())) {
            throw new IllegalArgumentException(
                    "Não é possível anexar arquivos a um exame cancelado."
            );
        }

        String nomeArmazenado =
                UUID.randomUUID() + "." + extensao;

        Path diretorio = Paths.get(diretorioUpload)
                .toAbsolutePath()
                .normalize();

        Path destino = diretorio.resolve(nomeArmazenado)
                .normalize();

        if (!destino.startsWith(diretorio)) {
            throw new IllegalArgumentException(
                    "Caminho de armazenamento inválido."
            );
        }

        try {

            Files.createDirectories(diretorio);

            arquivo.transferTo(destino);

            AnexoExame anexo = new AnexoExame();

            anexo.setExame(exame);
            anexo.setProfissional(profissional);
            anexo.setNomeOriginal(nomeOriginal);
            anexo.setNomeArmazenado(nomeArmazenado);
            anexo.setCaminhoArquivo(destino.toString());
            anexo.setTipoConteudo(arquivo.getContentType());
            anexo.setTamanhoBytes(arquivo.getSize());

            return anexoRepository.save(anexo);

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Não foi possível armazenar o arquivo.",
                    e
            );
        }
    }

    // ==========================================
    // LISTAR ANEXOS DE UM EXAME
    // ==========================================

    @Transactional(readOnly = true)
    public List<AnexoExame> listarPorExame(Long exameId) {

        return anexoRepository
                .findByExameIdOrderByDataUploadDesc(exameId);
    }

    // ==========================================
    // LISTAR ANEXOS DE UM PROFISSIONAL
    // ==========================================

    @Transactional(readOnly = true)
    public List<AnexoExame> listarPorProfissional(
            Long profissionalId) {

        return anexoRepository
                .findByProfissionalIdOrderByDataUploadDesc(
                        profissionalId
                );
    }

    // ==========================================
    // OBTER EXTENSÃO DO ARQUIVO
    // ==========================================

    private String obterExtensao(String nomeArquivo) {

        int ponto = nomeArquivo.lastIndexOf('.');

        if (ponto < 0 || ponto == nomeArquivo.length() - 1) {
            throw new IllegalArgumentException(
                    "O arquivo não possui uma extensão válida."
            );
        }

        return nomeArquivo.substring(ponto + 1)
                .toLowerCase();
    }

    public AnexoExame buscarPorIdEExame(Long anexoId, Long exameId) {

        return anexoRepository.findByIdAndExameId(anexoId, exameId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Anexo não encontrado para este exame."));
    }

    public File buscarArquivo(Long anexoId, Long exameId) {

        AnexoExame anexo = buscarPorIdEExame(anexoId, exameId);

        File arquivo = new File(anexo.getCaminhoArquivo());

        if (!arquivo.exists() || !arquivo.isFile()) {
            throw new IllegalArgumentException("Arquivo não encontrado no servidor.");
        }

        return arquivo;
    }
}
