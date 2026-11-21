
package com.medcare.dto;

import java.time.LocalDateTime;

public class AgendamentoDTO {

    private Long id;
    private String pacienteNome;
    private String profissionalNome;
    private LocalDateTime dataHora;
    private String status;
    private String observacoes;

    public AgendamentoDTO() {
    }

    public AgendamentoDTO(
            Long id,
            String pacienteNome,
            String profissionalNome,
            LocalDateTime dataHora,
            String status,
            String observacoes
    ) {
        this.id = id;
        this.pacienteNome = pacienteNome;
        this.profissionalNome = profissionalNome;
        this.dataHora = dataHora;
        this.status = status;
        this.observacoes = observacoes;
    }

    public Long getId() {
        return id;
    }

    public String getPacienteNome() {
        return pacienteNome;
    }

    public String getProfissionalNome() {
        return profissionalNome;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getStatus() {
        return status;
    }

    public String getObservacoes() {
        return observacoes;
    }
}
