
package com.medcare.repository;

import com.medcare.model.Agendamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository
        extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findAllByOrderByDataHoraAsc();

    List<Agendamento> findByDataHoraBetweenOrderByDataHoraAsc(
            LocalDateTime inicio,
            LocalDateTime fim
    );

    // Verifica sobreposição de horários considerando 30 minutos
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Agendamento a
        WHERE a.profissional.id = :profissionalId
          AND a.status <> 'Cancelado'
          AND a.dataHora < :fim
          AND a.dataHora > :inicio
    """)
    boolean existeConflitoDeHorario(
            @Param("profissionalId") Long profissionalId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    List<Agendamento> findByPaciente_IdOrderByDataHoraDesc(
            Long pacienteId
    );

    List<Agendamento> findByProfissional_IdOrderByDataHoraAsc(
            Long profissionalId
    );

    // Agenda de um profissional em determinado dia
    List<Agendamento>
    findByProfissional_IdAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByDataHoraAsc(
            Long profissionalId,
            LocalDateTime inicio,
            LocalDateTime fim
    );
}
