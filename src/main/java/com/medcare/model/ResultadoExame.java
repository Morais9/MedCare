package com.medcare.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resultados_exames")
public class ResultadoExame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exame ao qual este resultado pertence
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exame_id", nullable = false, unique = true)
    private Exame exame;

    // Profissional responsável pelo registro do resultado
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Usuario profissional;

    @Column(name = "descricao_resultado", nullable = false, columnDefinition = "TEXT")
    private String descricaoResultado;

    @Column(name = "descricao_laudo", columnDefinition = "TEXT")
    private String descricaoLaudo;

    // Pendente, Em emissão ou Emitido
    @Column(name = "status_laudo", nullable = false, length = 30)
    private String statusLaudo = "Pendente";

    @Column(name = "data_resultado", nullable = false)
    private LocalDateTime dataResultado;

    @Column(name = "data_emissao_laudo")
    private LocalDateTime dataEmissaoLaudo;

    public ResultadoExame() {
    }

    @PrePersist
    public void antesDeSalvar() {
        if (dataResultado == null) {
            dataResultado = LocalDateTime.now();
        }

        if (statusLaudo == null || statusLaudo.isBlank()) {
            statusLaudo = "Pendente";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDescricaoResultado() {
        return descricaoResultado;
    }

    public void setDescricaoResultado(String descricaoResultado) {
        this.descricaoResultado = descricaoResultado;
    }

    public String getDescricaoLaudo() {
        return descricaoLaudo;
    }

    public void setDescricaoLaudo(String descricaoLaudo) {
        this.descricaoLaudo = descricaoLaudo;
    }

    public String getStatusLaudo() {
        return statusLaudo;
    }

    public void setStatusLaudo(String statusLaudo) {
        this.statusLaudo = statusLaudo;
    }

    public LocalDateTime getDataResultado() {
        return dataResultado;
    }

    public void setDataResultado(LocalDateTime dataResultado) {
        this.dataResultado = dataResultado;
    }

    public LocalDateTime getDataEmissaoLaudo() {
        return dataEmissaoLaudo;
    }

    public void setDataEmissaoLaudo(LocalDateTime dataEmissaoLaudo) {
        this.dataEmissaoLaudo = dataEmissaoLaudo;
    }
}
