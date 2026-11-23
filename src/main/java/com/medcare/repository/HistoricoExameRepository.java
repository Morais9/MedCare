package com.medcare.repository;

import com.medcare.model.HistoricoExame;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoExameRepository
        extends JpaRepository<HistoricoExame, Long> {

    // Histórico completo de um exame, do evento mais antigo ao mais recente
    List<HistoricoExame> findByExameIdOrderByDataEventoAsc(Long exameId);

    // Histórico de ações realizadas por um profissional
    List<HistoricoExame> findByProfissionalIdOrderByDataEventoDesc(
            Long profissionalId
    );
}
