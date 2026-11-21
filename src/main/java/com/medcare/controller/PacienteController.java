
package com.medcare.controller;

import com.medcare.model.Paciente;
import com.medcare.service.PacienteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pacientes")
public class PacienteController {

    private static final Logger logger =
            LoggerFactory.getLogger(PacienteController.class);

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    public String cadastrar(
            @ModelAttribute Paciente paciente,
            RedirectAttributes redirectAttributes) {

        try {
            pacienteService.cadastrar(paciente);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Paciente cadastrado com sucesso!"
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "erro",
                    e.getMessage()
            );

        } catch (Exception e) {
            logger.error("Erro ao cadastrar paciente", e);

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Não foi possível cadastrar o paciente."
            );
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable Long id,
            @ModelAttribute Paciente paciente,
            RedirectAttributes redirectAttributes) {

        try {
            pacienteService.atualizar(id, paciente);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Dados do paciente atualizados com sucesso!"
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "erro",
                    e.getMessage()
            );

        } catch (Exception e) {
            logger.error("Erro ao atualizar paciente ID {}", id, e);

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Não foi possível atualizar o paciente."
            );
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            pacienteService.excluir(id);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Paciente excluído com sucesso!"
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "erro",
                    e.getMessage()
            );

        } catch (Exception e) {
            logger.error("Erro ao excluir paciente ID {}", id, e);

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Não foi possível excluir o paciente."
            );
        }

        return "redirect:/dashboard";
    }
}