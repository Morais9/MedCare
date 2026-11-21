
package com.medcare.controller;

import com.medcare.dto.AgendamentoDTO;
import com.medcare.model.Agendamento;
import com.medcare.model.Usuario;
import com.medcare.repository.UsuarioRepository;
import com.medcare.service.AgendamentoService;
import com.medcare.service.PacienteService;
import com.medcare.service.UsuarioService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private static final Logger logger =
            LoggerFactory.getLogger(AgendamentoController.class);

    private final AgendamentoService agendamentoService;
    private final PacienteService pacienteService;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public AgendamentoController(
            AgendamentoService agendamentoService,
            PacienteService pacienteService,
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository) {

        this.agendamentoService = agendamentoService;
        this.pacienteService = pacienteService;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    // Abrir página de agendamentos
    @GetMapping
    public String listar(Authentication authentication, Model model) {

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado."
                ));

        model.addAttribute("usuarioLogado", usuario);
        model.addAttribute("nomeUsuario", usuario.getNome());
        model.addAttribute("cargoUsuario", usuario.getCargo());

        String nome = usuario.getNome().trim();
        String[] partes = nome.split("\\s+");

        String iniciais = partes.length > 1
                ? partes[0].substring(0, 1)
                + partes[partes.length - 1].substring(0, 1)
                : nome.substring(0, 1);

        model.addAttribute("iniciaisUsuario", iniciais.toUpperCase());

        model.addAttribute("pacientes", pacienteService.listarTodos());
        model.addAttribute("profissionais", usuarioService.listarProfissionaisAtivos());
        model.addAttribute("agendamentos", agendamentoService.listarTodos());

        return "agendamentos";
    }

    // Cadastrar novo agendamento
    @PostMapping("/cadastrar")
    public String cadastrar(
            @RequestParam Long pacienteId,
            @RequestParam Long profissionalId,
            @RequestParam LocalDate data,
            @RequestParam String hora,
            @RequestParam(required = false) String observacoes,
            RedirectAttributes redirectAttributes) {

        LocalDateTime dataHora = LocalDateTime.of(
                data,
                LocalTime.parse(hora)
        );

        agendamentoService.cadastrar(
                pacienteId,
                profissionalId,
                dataHora,
                observacoes
        );

        redirectAttributes.addFlashAttribute(
                "sucesso",
                "Agendamento realizado com sucesso!"
        );

        return "redirect:/agendamentos";
    }


    // Consultar agenda de um profissional em determinada data
    @GetMapping("/agenda-profissional")
    @ResponseBody
    public List<AgendamentoDTO> visualizarAgendaProfissional(
            @RequestParam Long profissionalId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate data) {

        List<Agendamento> agendamentos =
                agendamentoService.visualizarAgendaProfissional(
                        profissionalId,
                        data
                );

        return agendamentos.stream()
                .map(agendamento -> new AgendamentoDTO(
                        agendamento.getId(),
                        agendamento.getPaciente().getNome(),
                        agendamento.getProfissional().getNome(),
                        agendamento.getDataHora(),
                        agendamento.getStatus(),
                        agendamento.getObservacoes()
                ))
                .toList();
    }

    // Atualizar status
    @PostMapping("/{id}/status")
    public String atualizarStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        agendamentoService.atualizarStatus(id, status);

        redirectAttributes.addFlashAttribute(
                "sucesso",
                "Status atualizado com sucesso!"
        );

        return "redirect:/agendamentos";
    }


    // Cancelar agendamento
    @PostMapping("/{id}/cancelar")
    public String cancelar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            agendamentoService.cancelar(id);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Agendamento cancelado com sucesso!"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    e.getMessage()
            );
        }
        return "redirect:/agendamentos";
    }
}
