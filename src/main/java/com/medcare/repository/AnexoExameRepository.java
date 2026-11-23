package com.medcare.repository;

import com.medcare.model.AnexoExame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnexoExameRepository extends JpaRepository<AnexoExame, Long> {

    List<AnexoExame> findByExameIdOrderByDataUploadDesc(Long exameId);

    List<AnexoExame> findByProfissionalIdOrderByDataUploadDesc(Long profissionalId);

    Optional<AnexoExame> findByIdAndExameId(Long id, Long exameId);
}