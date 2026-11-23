
package com.medcare.service;

import com.medcare.exception.RecursoNaoEncontradoException;
import com.medcare.model.Agendamento;
import com.medcare.model.Consulta;
import com.medcare.repository.AgendamentoRepository;
import com.medcare.repository.ConsultaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final AgendamentoRepository agendamentoRepository;

    public ConsultaService(
            ConsultaRepository consultaRepository,
            AgendamentoRepository agendamentoRepository) {

        this.consultaRepository = consultaRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    // ==========================================
    // INICIAR CONSULTA A PARTIR DO AGENDAMENTO
    // ==========================================

    @Transactional
    public Consulta iniciarConsulta(Long agendamentoId) {

        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Agendamento não encontrado."
                        ));

        if ("Cancelado".equalsIgnoreCase(agendamento.getStatus())) {
            throw new IllegalArgumentException(
                    "Não é possível iniciar uma consulta de um agendamento cancelado."
            );
        }

        if ("Concluído".equalsIgnoreCase(agendamento.getStatus())) {
            throw new IllegalArgumentException(
                    "Este agendamento já foi concluído."
            );
        }

        if (consultaRepository.existsByAgendamento_Id(agendamentoId)) {
            throw new IllegalArgumentException(
                    "Já existe uma consulta vinculada a este agendamento."
            );
        }

        Consulta consulta = new Consulta();

        consulta.setAgendamento(agendamento);
        consulta.setStatus("Em Atendimento");
        consulta.setDataInicio(LocalDateTime.now());

        agendamento.setStatus("Em Atendimento");

        agendamentoRepository.save(agendamento);

        return consultaRepository.save(consulta);
    }

    // ==========================================
    // BUSCAR CONSULTA PELO AGENDAMENTO
    // ==========================================

    @Transactional(readOnly = true)
    public Optional<Consulta> buscarPorAgendamento(Long agendamentoId) {

        return consultaRepository.findByAgendamento_Id(agendamentoId);
    }

    // ==========================================
    // ATUALIZAR INFORMAÇÕES DA CONSULTA
    // ==========================================

    @Transactional
    public Consulta atualizarConsulta(
            Long consultaId,
            String queixaPrincipal,
            String anamnese,
            String diagnostico,
            String conduta) {

        Consulta consulta = buscarPorId(consultaId);

        validarConsultaEmAndamento(consulta);

        consulta.setQueixaPrincipal(queixaPrincipal);
        consulta.setAnamnese(anamnese);
        consulta.setDiagnostico(diagnostico);
        consulta.setConduta(conduta);

        return consultaRepository.save(consulta);
    }

    // ==========================================
    // FINALIZAR CONSULTA
    // ==========================================


    @Transactional
    public Consulta finalizarConsulta(
            Long consultaId,
            String queixaPrincipal,
            String anamnese,
            String diagnostico,
            String conduta) {

        Consulta consulta = buscarPorId(consultaId);

        validarConsultaEmAndamento(consulta);

        if (queixaPrincipal == null || queixaPrincipal.isBlank()
                || anamnese == null || anamnese.isBlank()
                || diagnostico == null || diagnostico.isBlank()
                || conduta == null || conduta.isBlank()) {

            throw new IllegalArgumentException(
                    "Preencha todos os campos clínicos antes de finalizar a consulta."
            );
        }

        consulta.setQueixaPrincipal(queixaPrincipal.trim());
        consulta.setAnamnese(anamnese.trim());
        consulta.setDiagnostico(diagnostico.trim());
        consulta.setConduta(conduta.trim());

        consulta.setStatus("Finalizada");
        consulta.setDataFinalizacao(LocalDateTime.now());

        Agendamento agendamento = consulta.getAgendamento();
        agendamento.setStatus("Concluído");

        agendamentoRepository.save(agendamento);

        return consultaRepository.save(consulta);
    }


    // ==========================================
    // BUSCAR CONSULTA POR ID
    // ==========================================

    @Transactional(readOnly = true)
    public Consulta buscarPorId(Long id) {

        return consultaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Consulta não encontrada."
                        ));
    }

    // ==========================================
    // LISTAR TODAS AS CONSULTAS
    // ==========================================

    @Transactional(readOnly = true)
    public List<Consulta> listarTodas() {

        return consultaRepository.findAll();
    }

    // ==========================================
    // CONSULTAS POR PACIENTE
    // ==========================================

    @Transactional(readOnly = true)
    public List<Consulta> listarPorPaciente(Long pacienteId) {

        return consultaRepository
                .findByAgendamento_Paciente_IdOrderByDataInicioDesc(pacienteId);
    }

    // ==========================================
    // CONSULTAS POR PROFISSIONAL
    // ==========================================

    @Transactional(readOnly = true)
    public List<Consulta> listarPorProfissional(Long profissionalId) {

        return consultaRepository
                .findByAgendamento_Profissional_IdOrderByDataInicioDesc(profissionalId);
    }

    // ==========================================
    // VALIDAR CONSULTA EM ANDAMENTO
    // ==========================================

    private void validarConsultaEmAndamento(Consulta consulta) {

        if ("Finalizada".equalsIgnoreCase(consulta.getStatus())) {
            throw new IllegalArgumentException(
                    "Não é possível alterar uma consulta finalizada."
            );
        }
    }
}
