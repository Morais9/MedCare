
package com.medcare.service;

import com.medcare.model.Paciente;
import com.medcare.repository.PacienteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    // Lista todos os pacientes
    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    // Busca paciente pelo ID
    public Optional<Paciente> buscarPorId(Long id) {
        return pacienteRepository.findById(id);
    }

    // Pesquisa pacientes por nome ou CPF
    public List<Paciente> pesquisar(String termo) {

        if (termo == null || termo.trim().isEmpty()) {
            return listarTodos();
        }

        String busca = termo.trim();

        return pacienteRepository
                .findByNomeContainingIgnoreCaseOrCpfContaining(busca, busca);
    }

    
   // Cadastra um novo paciente
@Transactional
public Paciente cadastrar(Paciente paciente) {

    validarPaciente(paciente);

    String cpf = normalizarCpf(paciente.getCpf());

    if (pacienteRepository.existsByCpf(cpf)) {
        throw new IllegalArgumentException(
                "Já existe um paciente cadastrado com este CPF."
        );
    }

    paciente.setId(null);
    paciente.setCpf(cpf);

    return pacienteRepository.save(paciente);
}
    // Atualiza os dados de um paciente
    @Transactional
    public Paciente atualizar(Long id, Paciente dados) {

        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Paciente não encontrado."
                ));

        validarPaciente(dados);

        String cpf = normalizarCpf(dados.getCpf());

        boolean cpfEmUso = pacienteRepository.existsByCpf(cpf)
                && !paciente.getCpf().equals(cpf);

        if (cpfEmUso) {
            throw new IllegalArgumentException(
                    "Este CPF já pertence a outro paciente."
            );
        }

        paciente.setNome(dados.getNome().trim());
        paciente.setCpf(cpf);
        paciente.setDataNascimento(dados.getDataNascimento());
        paciente.setSexo(dados.getSexo());
        paciente.setTelefone(dados.getTelefone());
        paciente.setEmail(dados.getEmail());
        paciente.setEndereco(dados.getEndereco());
        paciente.setBairro(dados.getBairro());
        paciente.setCidade(dados.getCidade());
        paciente.setCep(dados.getCep());

        return pacienteRepository.save(paciente);
    }

    // Exclui um paciente
    @Transactional
    public void excluir(Long id) {

        if (!pacienteRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Paciente não encontrado."
            );
        }

        pacienteRepository.deleteById(id);
    }

   
  // Valida os dados do paciente
private void validarPaciente(Paciente paciente) {

    if (paciente == null) {
        throw new IllegalArgumentException(
                "Os dados do paciente não foram informados."
        );
    }

    // Nome: obrigatório, com nome e sobrenome
    if (paciente.getNome() == null || paciente.getNome().trim().isEmpty()) {
        throw new IllegalArgumentException(
                "O nome do paciente é obrigatório."
        );
    }

    paciente.setNome(paciente.getNome().trim());

    if (paciente.getNome().split("\\s+").length < 2) {
        throw new IllegalArgumentException(
                "Informe nome e sobrenome."
        );
    }

    // CPF: obrigatório e válido
    if (paciente.getCpf() == null || paciente.getCpf().trim().isEmpty()) {
        throw new IllegalArgumentException(
                "O CPF do paciente é obrigatório."
        );
    }

    if (!validarCPF(normalizarCpf(paciente.getCpf()))) {
        throw new IllegalArgumentException(
                "CPF inválido. Verifique os números informados."
        );
    }

    // Data de nascimento: obrigatória e não pode ser futura
    if (paciente.getDataNascimento() == null) {
        throw new IllegalArgumentException(
                "A data de nascimento é obrigatória."
        );
    }

    if (paciente.getDataNascimento().isAfter(LocalDate.now())) {
        throw new IllegalArgumentException(
                "A data de nascimento não pode ser futura."
        );
    }

    // CEP: opcional, mas se informado precisa ter 8 dígitos
    if (paciente.getCep() != null && !paciente.getCep().isBlank()) {
        String cep = paciente.getCep().replaceAll("\\D", "");

        if (cep.length() != 8) {
            throw new IllegalArgumentException(
                    "CEP inválido."
            );
        }

        paciente.setCep(cep);
    }
}

    

    // Valida os dígitos verificadores do CPF
    private boolean validarCPF(String cpf) {

        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }

        // Rejeita CPFs com todos os números iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int soma = 0;

        // Calcula o primeiro dígito verificador
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }

        int primeiroDigito = (soma * 10) % 11;

        if (primeiroDigito == 10) {
            primeiroDigito = 0;
        }

        if (primeiroDigito != Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }

        soma = 0;

        // Calcula o segundo dígito verificador
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }

        int segundoDigito = (soma * 10) % 11;

        if (segundoDigito == 10) {
            segundoDigito = 0;
        }

        return segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    // Remove pontos, traços e outros caracteres do CPF
    private String normalizarCpf(String cpf) {

        if (cpf == null) {
            return "";
        }

        return cpf.replaceAll("\\D", "");
    }
}
