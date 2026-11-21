
package com.medcare.model;

import jakarta.persistence.*;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Paciente que receberá o atendimento
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    // Funcionário responsável pelo atendimento
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Usuario profissional;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 30)
    private String status = "Confirmado";

    @Column(length = 500)
    private String observacoes;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Transient
    public LocalDateTime getDataHoraFim() {
        if (dataHora == null) {
            return null;
        }

        return dataHora.plusMinutes(30);
    }

    public Agendamento() {
    }

    @PrePersist
    public void antesDeSalvar() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }

        if (status == null || status.isBlank()) {
            status = "Confirmado";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }

    public Usuario getProfissional() {
        return profissional;
    }

    public void setProfissional(Usuario profissional) {
        this.profissional = profissional;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
