
package com.medcare.repository;

import com.medcare.model.Exame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExameRepository extends JpaRepository<Exame, Long> {

    // Carregar os relacionamentos necessários para a tela de exames
    @Override
    @EntityGraph(attributePaths = {
            "consulta",
            "consulta.agendamento",
            "consulta.agendamento.paciente"
    })
    List<Exame> findAll();

    // Exames de uma consulta específica
    List<Exame> findByConsultaId(Long consultaId);

    // Exames de um paciente
    List<Exame> findByConsultaAgendamentoPacienteId(Long pacienteId);

    @EntityGraph(attributePaths = {
            "consulta",
            "consulta.agendamento",
            "consulta.agendamento.paciente"
    })
    List<Exame> findByConsultaAgendamentoProfissionalId(Long profissionalId);

    // Filtrar pelo status do exame
    List<Exame> findByStatusIgnoreCase(String status);

    // Filtrar pelo status do laudo
    List<Exame> findByStatusLaudoIgnoreCase(String statusLaudo);

    // Exames de um paciente com determinado status
    List<Exame> findByConsultaAgendamentoPacienteIdAndStatusIgnoreCase(
            Long pacienteId,
            String status
    );

    // Exames solicitados por um profissional com determinado status
    List<Exame> findByConsultaAgendamentoProfissionalIdAndStatusIgnoreCase(
            Long profissionalId,
            String status
    );
}
