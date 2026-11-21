package com.medcare.exception;

import com.medcare.controller.AgendamentoController;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@ControllerAdvice(assignableTypes = AgendamentoController.class)
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Recurso não encontrado
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public Object tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        logger.warn("Recurso não encontrado: {}", ex.getMessage());

        return tratarErro(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request,
                redirectAttributes
        );
    }

    // Horário indisponível
    @ExceptionHandler(HorarioIndisponivelException.class)
    public Object tratarHorarioIndisponivel(
            HorarioIndisponivelException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        logger.warn("Horário indisponível: {}", ex.getMessage());

        return tratarErro(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request,
                redirectAttributes
        );
    }

    // Dados inválidos
    @ExceptionHandler(IllegalArgumentException.class)
    public Object tratarDadosInvalidos(
            IllegalArgumentException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        logger.warn("Operação inválida: {}", ex.getMessage());

        return tratarErro(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request,
                redirectAttributes
        );
    }

    // Erros inesperados
    @ExceptionHandler(Exception.class)
    public Object tratarErroGeral(
            Exception ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        logger.error("Erro inesperado na aplicação", ex);

        return tratarErro(
                "Ocorreu um erro inesperado. Tente novamente.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request,
                redirectAttributes
        );
    }

    // Centralizar resposta dos erros
    private Object tratarErro(
            String mensagem,
            HttpStatus status,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String uri = request.getRequestURI();

        // Resposta para consulta da agenda via JSON
        if (uri.endsWith("/agenda-profissional")) {

            return ResponseEntity
                    .status(status)
                    .body(Map.of("erro", mensagem));
        }

        // Resposta para páginas com redirecionamento
        redirectAttributes.addFlashAttribute("erro", mensagem);

        return "redirect:/agendamentos";
    }
}
