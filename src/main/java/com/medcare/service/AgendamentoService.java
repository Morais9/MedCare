
package com.medcare.service;

import com.medcare.exception.HorarioIndisponivelException;
import com.medcare.exception.RecursoNaoEncontradoException;
import com.medcare.model.Agendamento;
import com.medcare.model.Paciente;
import com.medcare.model.Usuario;
import com.medcare.repository.AgendamentoRepository;
import com.medcare.repository.PacienteRepository;
import com.medcare.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    private static final Set<String> CARGOS_ATENDIMENTO = Set.of(
            "Medico",
            "Enfermeira",
            "Radiologista",
            "Outro profissional"
    );

    private static final Set<String> STATUS_PERMITIDOS = Set.of(
            "Confirmado",
            "Em Atendimento",
            "Aguardando",
            "Reagendado",
            "Concluído",
            "Cancelado"
    );

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository) {

        this.agendamentoRepository = agendamentoRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Cadastrar agendamento
    @Transactional
    public Agendamento cadastrar(
            Long pacienteId,
            Long profissionalId,
            LocalDateTime dataHora,
            String observacoes) {

        // Validar paciente
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente não encontrado."
                ));

        // Validar profissional
        Usuario profissional = usuarioRepository.findById(profissionalId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Profissional não encontrado."
                ));

        if (!profissional.isAtivo()) {
            throw new IllegalArgumentException(
                    "Este profissional não está ativo."
            );
        }

        if (!CARGOS_ATENDIMENTO.contains(profissional.getCargo())) {
            throw new IllegalArgumentException(
                    "Este funcionário não pode realizar atendimentos."
            );
        }

        if (profissional.getEspecialidade() == null ||
                profissional.getEspecialidade().isBlank()) {
            throw new IllegalArgumentException(
                    "O profissional não possui especialidade cadastrada."
            );
        }

        // Validar data e horário
        if (dataHora == null) {
            throw new IllegalArgumentException(
                    "Informe a data e o horário do atendimento."
            );
        }

        if (!dataHora.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "O agendamento deve ser realizado para uma data futura."
            );
        }

        // Verificar conflito de horário
        LocalDateTime inicioConflito = dataHora.minusMinutes(30);
        LocalDateTime fimConflito = dataHora.plusMinutes(30);

        boolean horarioOcupado =
                agendamentoRepository.existeConflitoDeHorario(
                        profissionalId,
                        inicioConflito,
                        fimConflito
                );

        if (horarioOcupado) {
            throw new HorarioIndisponivelException(
                    "Este profissional já possui um atendimento nesse horário."
            );
        }

        // Criar agendamento
        Agendamento agendamento = new Agendamento();

        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        agendamento.setDataHora(dataHora);
        agendamento.setStatus("Confirmado");

        if (observacoes != null && !observacoes.isBlank()) {
            agendamento.setObservacoes(observacoes.trim());
        }

        return agendamentoRepository.save(agendamento);
    }

    // Listar todos os agendamentos
    @Transactional(readOnly = true)
    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAllByOrderByDataHoraAsc();
    }

    // Buscar agendamentos de um paciente
    @Transactional(readOnly = true)
    public List<Agendamento> listarPorPaciente(Long pacienteId) {
        return agendamentoRepository
                .findByPaciente_IdOrderByDataHoraDesc(pacienteId);
    }

    // Buscar agendamentos de um profissional
    @Transactional(readOnly = true)
    public List<Agendamento> listarPorProfissional(Long profissionalId) {
        return agendamentoRepository
                .findByProfissional_IdOrderByDataHoraAsc(profissionalId);
    }

    // Atualizar status
    @Transactional
    public Agendamento atualizarStatus(Long id, String novoStatus) {

        if (novoStatus == null ||
                !STATUS_PERMITIDOS.contains(novoStatus)) {
            throw new IllegalArgumentException(
                    "Status de agendamento inválido."
            );
        }

        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Agendamento não encontrado."
                ));

        agendamento.setStatus(novoStatus);

        return agendamentoRepository.save(agendamento);
    }

    // Consultar a agenda diária
    @Transactional(readOnly = true)
    public List<Agendamento> visualizarAgendaProfissional(
            Long profissionalId,
            LocalDate data) {

        if (data == null) {
            throw new IllegalArgumentException(
                    "Informe a data da agenda."
            );
        }

        Usuario profissional = usuarioRepository.findById(profissionalId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Profissional não encontrado."
                ));

        if (!profissional.isAtivo()) {
            throw new IllegalArgumentException(
                    "Este profissional não está ativo."
            );
        }

        if (!CARGOS_ATENDIMENTO.contains(profissional.getCargo())) {
            throw new IllegalArgumentException(
                    "Este funcionário não pode realizar atendimentos."
            );
        }

        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.plusDays(1).atStartOfDay();

        return agendamentoRepository
                .findByProfissional_IdAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByDataHoraAsc(
                        profissionalId,
                        inicio,
                        fim
                );
    }

    // Cancelar agendamento
    @Transactional
    public void cancelar(Long id) {

        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Agendamento não encontrado."
                ));

        if ("Concluído".equals(agendamento.getStatus())) {
            throw new IllegalArgumentException(
                    "Não é possível cancelar um atendimento concluído."
            );
        }

        agendamento.setStatus("Cancelado");

        agendamentoRepository.save(agendamento);
    }
}
