
package com.medcare.service;

import com.medcare.model.Usuario;
import com.medcare.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Set<String> CARGOS_PERMITIDOS = Set.of(
            "Recepcionista",
            "Medico",
            "Enfermeira",
            "Radiologista",
            "Outro profissional"
    );

    private static final Set<String> CARGOS_ATENDIMENTO = Set.of(
            "Medico",
            "Enfermeira",
            "Radiologista",
            "Outro profissional"
    );

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrar(Usuario usuario, String confirmarSenha) {

        // Validação do nome
        if (usuario.getNome() == null ||
                usuario.getNome().isBlank()) {
            throw new IllegalArgumentException(
                    "Informe seu nome completo."
            );
        }

        usuario.setNome(usuario.getNome().trim());

        // Validação do CPF
        String cpf = usuario.getCpf();

        if (cpf == null) {
            throw new IllegalArgumentException(
                    "Informe seu CPF."
            );
        }

        cpf = cpf.replaceAll("\\D", "");

        if (!validarCPF(cpf)) {
            throw new IllegalArgumentException(
                    "Informe um CPF válido."
            );
        }

        if (usuarioRepository.existsByCpf(cpf)) {
            throw new IllegalArgumentException(
                    "Este CPF já está cadastrado."
            );
        }

        usuario.setCpf(cpf);

        // Validação do e-mail
        if (usuario.getEmail() == null ||
                usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "Informe seu e-mail."
            );
        }

        String email = usuario.getEmail()
                .trim()
                .toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Este e-mail já está cadastrado."
            );
        }

        usuario.setEmail(email);

        // Validação do cargo
        if (usuario.getCargo() == null ||
                !CARGOS_PERMITIDOS.contains(usuario.getCargo())) {
            throw new IllegalArgumentException(
                    "Selecione um cargo válido."
            );
        }

        // Validação da especialidade
        if (CARGOS_ATENDIMENTO.contains(usuario.getCargo())) {

            if (usuario.getEspecialidade() == null ||
                    usuario.getEspecialidade().isBlank()) {
                throw new IllegalArgumentException(
                        "Informe sua especialidade ou área de atuação."
                );
            }

            usuario.setEspecialidade(
                    usuario.getEspecialidade().trim()
            );

        } else {
            // Recepcionistas não possuem especialidade de atendimento
            usuario.setEspecialidade(null);
        }

        // Validação da senha
        if (usuario.getSenha() == null ||
                usuario.getSenha().length() < 8) {
            throw new IllegalArgumentException(
                    "A senha deve possuir pelo menos 8 caracteres."
            );
        }

        // Confirmação da senha
        if (confirmarSenha == null ||
                !usuario.getSenha().equals(confirmarSenha)) {
            throw new IllegalArgumentException(
                    "As senhas não conferem."
            );
        }

        // Criptografia da senha
        usuario.setSenha(
                passwordEncoder.encode(usuario.getSenha())
        );

        // Novos cadastros aguardam aprovação
        usuario.setAtivo(false);

        // Segurança
        usuario.setId(null);
        usuario.setDataCadastro(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    // Validação matemática do CPF
    private boolean validarCPF(String cpf) {

        if (cpf == null || cpf.length() != 11 ||
                cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int soma = 0;

        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i))
                    * (10 - i);
        }

        int primeiroDigito = (soma * 10) % 11;

        if (primeiroDigito == 10) {
            primeiroDigito = 0;
        }

        if (primeiroDigito !=
                Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }

        soma = 0;

        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i))
                    * (11 - i);
        }

        int segundoDigito = (soma * 10) % 11;

        if (segundoDigito == 10) {
            segundoDigito = 0;
        }

        return segundoDigito ==
                Character.getNumericValue(cpf.charAt(10));
    }

    // Lista profissionais ativos para os agendamentos
    public List<Usuario> listarProfissionaisAtivos() {

        return usuarioRepository.findByAtivoTrueAndCargoIn(
                List.of(
                        "Medico",
                        "Enfermeira",
                        "Radiologista",
                        "Outro profissional"
                )
        );
    }
}
