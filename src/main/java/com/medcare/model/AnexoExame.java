package com.medcare.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "anexos_exames")
public class AnexoExame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exame ao qual o arquivo pertence
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exame_id", nullable = false)
    private Exame exame;

    // Profissional responsável pelo envio
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Usuario profissional;

    @Column(name = "nome_original", nullable = false, length = 255)
    private String nomeOriginal;

    @Column(name = "nome_armazenado", nullable = false, length = 255)
    private String nomeArmazenado;

    @Column(name = "caminho_arquivo", nullable = false, length = 500)
    private String caminhoArquivo;

    @Column(name = "tipo_conteudo", length = 100)
    private String tipoConteudo;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @Column(name = "data_upload", nullable = false)
    private LocalDateTime dataUpload;

    public AnexoExame() {
    }

    @PrePersist
    public void antesDeSalvar() {
        if (dataUpload == null) {
            dataUpload = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Exame getExame() {
        return exame;
    }

    public void setExame(Exame exame) {
        this.exame = exame;
    }

    public Usuario getProfissional() {
        return profissional;
    }

    public void setProfissional(Usuario profissional) {
        this.profissional = profissional;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getNomeArmazenado() {
        return nomeArmazenado;
    }

    public void setNomeArmazenado(String nomeArmazenado) {
        this.nomeArmazenado = nomeArmazenado;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public String getTipoConteudo() {
        return tipoConteudo;
    }

    public void setTipoConteudo(String tipoConteudo) {
        this.tipoConteudo = tipoConteudo;
    }

    public Long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public void setTamanhoBytes(Long tamanhoBytes) {
        this.tamanhoBytes = tamanhoBytes;
    }

    public LocalDateTime getDataUpload() {
        return dataUpload;
    }
}
