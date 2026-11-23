
package com.medcare.service;

import com.medcare.model.Consulta;
import com.medcare.model.Exame;
import com.medcare.model.Usuario;

import com.medcare.repository.ConsultaRepository;
import com.medcare.repository.ExameRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ExameService {

    private final ExameRepository exameRepository;
    private final ConsultaRepository consultaRepository;
    private final HistoricoExameService historicoExameService;

    public ExameService(
            ExameRepository exameRepository,
            ConsultaRepository consultaRepository,
            HistoricoExameService historicoExameService) {

        this.exameRepository = exameRepository;
        this.consultaRepository = consultaRepository;
        this.historicoExameService = historicoExameService;
    }

    // ==========================================
    // SOLICITAR EXAME
    // ==========================================

    public Exame solicitarExame(
            Long consultaId,
            Usuario profissional,
            String tipoExame,
            String descricao,
            String convenio,
            String prioridade) {

        if (tipoExame == null || tipoExame.isBlank()) {
            throw new IllegalArgumentException(
                    "O tipo do exame é obrigatório."
            );
        }

        if (profissional == null) {
            throw new IllegalArgumentException(
                    "Profissional não informado."
            );
        }

        if (prioridade == null || prioridade.isBlank()) {
            prioridade = "Normal";
        }

        if (!List.of("Normal", "Urgente", "Emergencial")
                .contains(prioridade)) {

            throw new IllegalArgumentException(
                    "Prioridade inválida."
            );
        }

        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Consulta não encontrada."
                ));

        if (consulta.getAgendamento().getProfissional() == null ||
                !consulta.getAgendamento().getProfissional()
                        .getId().equals(profissional.getId())) {

            throw new IllegalArgumentException(
                    "Você não tem permissão para solicitar este exame."
            );
        }

        Exame exame = new Exame();

        exame.setConsulta(consulta);
        exame.setTipoExame(tipoExame.trim());
        exame.setDescricao(descricao);
        exame.setConvenio(convenio);
        exame.setPrioridade(prioridade);
        exame.setStatus("Pendente");
        exame.setStatusLaudo("Não iniciado");

        Exame exameSalvo = exameRepository.save(exame);

        historicoExameService.registrar(
                exameSalvo,
                profissional,
                "Exame solicitado",
                "Exame solicitado: " + exameSalvo.getTipoExame()
        );

        return exameSalvo;
    }

    // ==========================================
    // LISTAR TODOS OS EXAMES
    // ==========================================

    @Transactional(readOnly = true)
    public List<Exame> listarTodos() {
        return exameRepository.findAll();
    }

    // ==========================================
    // BUSCAR POR ID
    // ==========================================

    @Transactional(readOnly = true)
    public Exame buscarPorId(Long id) {
        return exameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Exame não encontrado."
                ));
    }

    // ==========================================
    // LISTAR POR CONSULTA
    // ==========================================

    @Transactional(readOnly = true)
    public List<Exame> listarPorConsulta(Long consultaId) {
        return exameRepository.findByConsultaId(consultaId);
    }

    // ==========================================
    // LISTAR POR PACIENTE
    // ==========================================

    @Transactional(readOnly = true)
    public List<Exame> listarPorPaciente(Long pacienteId) {
        return exameRepository
                .findByConsultaAgendamentoPacienteId(pacienteId);
    }

    // ==========================================
    // LISTAR POR PROFISSIONAL
    // ==========================================

    @Transactional(readOnly = true)
    public List<Exame> listarPorProfissional(Long profissionalId) {
        return exameRepository
                .findByConsultaAgendamentoProfissionalId(profissionalId);
    }

    // ==========================================
    // FILTRAR POR STATUS
    // ==========================================

    @Transactional(readOnly = true)
    public List<Exame> listarPorStatus(String status) {
        return exameRepository.findByStatusIgnoreCase(status);
    }

    // ==========================================
    // FILTRAR POR STATUS DO LAUDO
    // ==========================================

    @Transactional(readOnly = true)
    public List<Exame> listarPorStatusLaudo(String statusLaudo) {
        return exameRepository.findByStatusLaudoIgnoreCase(statusLaudo);
    }

    // ==========================================
    // CANCELAR EXAME
    // ==========================================

    public Exame cancelarExame(
            Long exameId,
            Usuario profissional) {

        Exame exame = buscarPorId(exameId);

        if (profissional == null ||
                !exame.getConsulta().getAgendamento()
                        .getProfissional().getId()
                        .equals(profissional.getId())) {

            throw new IllegalArgumentException(
                    "Você não tem permissão para cancelar este exame."
            );
        }

        if ("Realizado".equalsIgnoreCase(exame.getStatus())) {
            throw new IllegalArgumentException(
                    "Não é possível cancelar um exame já realizado."
            );
        }

        if ("Cancelado".equalsIgnoreCase(exame.getStatus())) {
            throw new IllegalArgumentException(
                    "Este exame já está cancelado."
            );
        }

        exame.setStatus("Cancelado");

        Exame exameSalvo = exameRepository.save(exame);

        historicoExameService.registrar(
                exameSalvo,
                profissional,
                "Exame cancelado",
                "O exame " + exameSalvo.getTipoExame() +
                        " foi cancelado."
        );

        return exameSalvo;
    }
}
