
package com.medcare.controller;

import com.medcare.model.Agendamento;
import com.medcare.model.Consulta;
import com.medcare.model.Usuario;

import com.medcare.repository.AgendamentoRepository;
import com.medcare.repository.UsuarioRepository;

import com.medcare.service.ConsultaService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;
    private final UsuarioRepository usuarioRepository;
    private final AgendamentoRepository agendamentoRepository;

    public ConsultaController(
            ConsultaService consultaService,
            UsuarioRepository usuarioRepository,
            AgendamentoRepository agendamentoRepository) {

        this.consultaService = consultaService;
        this.usuarioRepository = usuarioRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    // ==========================================
    // LISTAR CONSULTAS
    // ==========================================

    @GetMapping
    public String listarConsultas(
            Authentication authentication,
            Model model) {

        Usuario usuarioLogado = obterUsuarioLogado(authentication);

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("nomeUsuario", usuarioLogado.getNome());
        model.addAttribute("cargoUsuario", usuarioLogado.getCargo());

        model.addAttribute(
                "consultas",
                consultaService.listarPorProfissional(usuarioLogado.getId())
        );

        return "consultas";
    }

    // ==========================================
    // CARREGAR MODAL PELO AGENDAMENTO
    // ==========================================

    @GetMapping("/modal/agendamento/{agendamentoId}")
    public String carregarModal(
            @PathVariable Long agendamentoId,
            Authentication authentication,
            Model model) {

        return prepararModal(agendamentoId, authentication, model);
    }

    // ==========================================
    // INICIAR ATENDIMENTO
    // ==========================================

    @PostMapping("/modal/iniciar/{agendamentoId}")
    public String iniciarConsultaModal(
            @PathVariable Long agendamentoId,
            Authentication authentication,
            Model model) {

        try {

            Usuario usuarioLogado = obterUsuarioLogado(authentication);
            Agendamento agendamento = buscarAgendamento(agendamentoId);

            validarPermissaoProfissional(
                    agendamento.getProfissional(),
                    usuarioLogado
            );

            consultaService.iniciarConsulta(agendamentoId);

        } catch (IllegalArgumentException e) {

            model.addAttribute("modalErro", e.getMessage());
        }

        return prepararModal(agendamentoId, authentication, model);
    }

    // ==========================================
    // ATUALIZAR CONSULTA
    // ==========================================

    @PostMapping("/modal/{id}/atualizar")
    public String atualizarConsultaModal(
            @PathVariable Long id,
            @RequestParam(required = false) String queixaPrincipal,
            @RequestParam(required = false) String anamnese,
            @RequestParam(required = false) String diagnostico,
            @RequestParam(required = false) String conduta,
            Authentication authentication,
            Model model) {

        Consulta consulta = consultaService.buscarPorId(id);

        Long agendamentoId = consulta.getAgendamento().getId();

        try {

            Usuario usuarioLogado = obterUsuarioLogado(authentication);

            validarPermissaoProfissional(
                    consulta.getAgendamento().getProfissional(),
                    usuarioLogado
            );

            consultaService.atualizarConsulta(
                    id,
                    queixaPrincipal,
                    anamnese,
                    diagnostico,
                    conduta
            );

        } catch (IllegalArgumentException e) {

            model.addAttribute("modalErro", e.getMessage());
        }

        return prepararModal(agendamentoId, authentication, model);
    }

    // ==========================================
    // FINALIZAR CONSULTA
    // ==========================================

    @PostMapping("/modal/{id}/finalizar")
    public String finalizarConsultaModal(
            @PathVariable Long id,
            @RequestParam(required = false) String queixaPrincipal,
            @RequestParam(required = false) String anamnese,
            @RequestParam(required = false) String diagnostico,
            @RequestParam(required = false) String conduta,
            Authentication authentication,
            Model model) {

        Consulta consulta = consultaService.buscarPorId(id);

        Long agendamentoId = consulta.getAgendamento().getId();

        try {

            Usuario usuarioLogado = obterUsuarioLogado(authentication);

            validarPermissaoProfissional(
                    consulta.getAgendamento().getProfissional(),
                    usuarioLogado
            );

            consultaService.finalizarConsulta(
                    id,
                    queixaPrincipal,
                    anamnese,
                    diagnostico,
                    conduta
            );

        } catch (IllegalArgumentException e) {

            model.addAttribute("modalErro", e.getMessage());
        }

        return prepararModal(agendamentoId, authentication, model);
    }

    // ==========================================
    // PREPARAR CONTEÚDO DO MODAL
    // ==========================================

    private String prepararModal(
            Long agendamentoId,
            Authentication authentication,
            Model model) {

        Usuario usuarioLogado = obterUsuarioLogado(authentication);

        Agendamento agendamento = buscarAgendamento(agendamentoId);

        validarPermissaoProfissional(
                agendamento.getProfissional(),
                usuarioLogado
        );

        Consulta consulta = consultaService
                .buscarPorAgendamento(agendamentoId)
                .orElse(null);

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("agendamento", agendamento);
        model.addAttribute("consulta", consulta);

        return "fragments/consulta-modal :: modal";
    }

    // ==========================================
    // BUSCAR AGENDAMENTO
    // ==========================================

    private Agendamento buscarAgendamento(Long id) {

        return agendamentoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Agendamento não encontrado."
                        ));
    }

    // ==========================================
    // OBTER USUÁRIO LOGADO
    // ==========================================

    private Usuario obterUsuarioLogado(Authentication authentication) {

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado."
                        ));
    }

    // ==========================================
    // VALIDAR PERMISSÃO DO PROFISSIONAL
    // ==========================================

    private void validarPermissaoProfissional(
            Usuario profissional,
            Usuario usuarioLogado) {

        if (profissional == null ||
                !profissional.getId().equals(usuarioLogado.getId())) {

            throw new AccessDeniedException(
                    "Você não tem permissão para acessar este atendimento."
            );
        }
    }
}
