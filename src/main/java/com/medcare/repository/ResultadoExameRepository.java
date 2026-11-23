package com.medcare.repository;

import com.medcare.model.ResultadoExame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultadoExameRepository extends JpaRepository<ResultadoExame, Long> {

    // Buscar resultado de um exame específico
    Optional<ResultadoExame> findByExameId(Long exameId);

    // Listar resultados registrados por um profissional
    List<ResultadoExame> findByProfissionalId(Long profissionalId);

    // Buscar resultados pelo status do laudo
    List<ResultadoExame> findByStatusLaudoIgnoreCase(String statusLaudo);

    // Listar resultados de um paciente
    List<ResultadoExame> findByExameConsultaAgendamentoPacienteId(Long pacienteId);

}
