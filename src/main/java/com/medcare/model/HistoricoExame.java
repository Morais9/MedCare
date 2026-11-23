package com.medcare.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_exames")
public class HistoricoExame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exame_id", nullable = false)
    private Exame exame;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Usuario profissional;

    @Column(nullable = false, length = 100)
    private String acao;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;

    public HistoricoExame() {
    }

    @PrePersist
    public void antesDeSalvar() {
        if (dataEvento == null) {
            dataEvento = LocalDateTime.now();
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

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataEvento() {
        return dataEvento;
    }
}
