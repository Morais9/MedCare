package com.medcare.repository;

import com.medcare.model.Consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Buscar consulta pelo agendamento
    Optional<Consulta> findByAgendamento_Id(Long agendamentoId);

    // Listar consultas por período
    List<Consulta> findByDataInicioGreaterThanEqualAndDataInicioLessThanOrderByDataInicioAsc(
            LocalDateTime inicio,
            LocalDateTime fim
    );

    // Histórico de consultas de um paciente
    List<Consulta> findByAgendamento_Paciente_IdOrderByDataInicioDesc(
            Long pacienteId
    );

    // Consultas de um profissional
    List<Consulta> findByAgendamento_Profissional_IdOrderByDataInicioDesc(
            Long profissionalId
    );

    // Consultas por status
    List<Consulta> findByStatusOrderByDataInicioAsc(String status);

    // Consultas em andamento de um profissional
    List<Consulta> findByAgendamento_Profissional_IdAndStatusOrderByDataInicioAsc(
            Long profissionalId,
            String status
    );

    // Verificar se já existe consulta para o agendamento
    boolean existsByAgendamento_Id(Long agendamentoId);
}
