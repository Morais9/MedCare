
package com.medcare.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exames")
public class Exame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Consulta que originou a solicitação do exame
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consulta_id", nullable = false)
    private Consulta consulta;

    // Tipo ou nome do exame solicitado
    @Column(nullable = false, length = 120)
    private String tipoExame;

    // Informações adicionais da solicitação
    @Column(columnDefinition = "TEXT")
    private String descricao;

    // Convênio do paciente, quando informado
    @Column(length = 100)
    private String convenio;

    // Prioridade: Normal, Urgente ou Emergencial
    @Column(nullable = false, length = 30)
    private String prioridade = "Normal";

    // Status do exame: Pendente, Realizado ou Cancelado
    @Column(nullable = false, length = 30)
    private String status = "Pendente";

    // Status do laudo: Não iniciado, Em emissão ou Emitido
    @Column(name = "status_laudo", nullable = false, length = 30)
    private String statusLaudo = "Não iniciado";

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_realizacao")
    private LocalDateTime dataRealizacao;

    public Exame() {
    }

    @PrePersist
    public void antesDeSalvar() {
        if (dataSolicitacao == null) {
            dataSolicitacao = LocalDateTime.now();
        }

        if (prioridade == null || prioridade.isBlank()) {
            prioridade = "Normal";
        }

        if (status == null || status.isBlank()) {
            status = "Pendente";
        }

        if (statusLaudo == null || statusLaudo.isBlank()) {
            statusLaudo = "Não iniciado";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Consulta getConsulta() {
        return consulta;
    }

    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }

    public String getTipoExame() {
        return tipoExame;
    }

    public void setTipoExame(String tipoExame) {
        this.tipoExame = tipoExame;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getConvenio() {
        return convenio;
    }

    public void setConvenio(String convenio) {
        this.convenio = convenio;
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusLaudo() {
        return statusLaudo;
    }

    public void setStatusLaudo(String statusLaudo) {
        this.statusLaudo = statusLaudo;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public LocalDateTime getDataRealizacao() {
        return dataRealizacao;
    }

    public void setDataRealizacao(LocalDateTime dataRealizacao) {
        this.dataRealizacao = dataRealizacao;
    }
}
