
package com.medcare.repository;

import com.medcare.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import com.medcare.model.Usuario;
import java.util.List;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    List<Usuario> findByAtivoTrueAndCargoIgnoreCase(String cargo);

    List<Usuario> findByAtivoTrueAndCargoIn(
            List<String> cargos
    );

}


