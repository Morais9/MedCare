package com.medcare.repository;

import com.medcare.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    // Verifica se já existe um paciente com determinado CPF
    boolean existsByCpf(String cpf);

    // Busca pacientes pelo nome ou CPF
    List<Paciente> findByNomeContainingIgnoreCaseOrCpfContaining(
            String nome,
            String cpf
    );

}
