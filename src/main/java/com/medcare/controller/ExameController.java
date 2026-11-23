
package com.medcare.controller;

import com.medcare.model.AnexoExame;
import com.medcare.model.Consulta;
import com.medcare.model.Exame;
import com.medcare.model.HistoricoExame;
import com.medcare.model.ResultadoExame;
import com.medcare.model.Usuario;

import com.medcare.repository.UsuarioRepository;

import com.medcare.service.AnexoExameService;
import com.medcare.service.ConsultaService;
import com.medcare.service.ExameService;
import com.medcare.service.HistoricoExameService;
import com.medcare.service.ResultadoExameService;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/exames")
public class ExameController {

    private final ExameService exameService;
    private final ResultadoExameService resultadoExameService;
    private final ConsultaService consultaService;
    private final HistoricoExameService historicoExameService;
    private final AnexoExameService anexoExameService;
    private final UsuarioRepository usuarioRepository;

    public ExameController(
            ExameService exameService,
            ResultadoExameService resultadoExameService,
            ConsultaService consultaService,
            HistoricoExameService historicoExameService,
            AnexoExameService anexoExameService,
            UsuarioRepository usuarioRepository) {

        this.exameService = exameService;
        this.resultadoExameService = resultadoExameService;
        this.consultaService = consultaService;
        this.historicoExameService = historicoExameService;
        this.anexoExameService = anexoExameService;
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================
    // LISTAR EXAMES
    // ==========================================

    @GetMapping
    public String listarExames(
            Authentication authentication,
            Model model,
            @RequestParam(required = false) Long abrir) {

        Usuario usuarioLogado = obterUsuarioLogado(authentication);

        List<Exame> exames =
                exameService.listarPorProfissional(usuarioLogado.getId());

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("nomeUsuario", usuarioLogado.getNome());
        model.addAttribute("cargoUsuario", usuarioLogado.getCargo());

        model.addAttribute("exames", exames);

        model.addAttribute(
                "consultas",
                consultaService.listarPorProfissional(usuarioLogado.getId())
        );

        Map<Long, ResultadoExame> resultadosPorExame = new HashMap<>();
        Map<Long, List<HistoricoExame>> historicosPorExame = new HashMap<>();
        Map<Long, List<AnexoExame>> anexosPorExame = new HashMap<>();

        for (Exame exame : exames) {

            Long exameId = exame.getId();

            try {
                resultadosPorExame.put(
                        exameId,
                        resultadoExameService.buscarPorExame(exameId)
                );
            } catch (IllegalArgumentException ignored) {
                // Este exame ainda não possui resultado.
            }

            historicosPorExame.put(
                    exameId,
                    historicoExameService.listarPorExame(exameId)
            );

            anexosPorExame.put(
                    exameId,
                    anexoExameService.listarPorExame(exameId)
            );
        }

        model.addAttribute("resultadosPorExame", resultadosPorExame);
        model.addAttribute("historicosPorExame", historicosPorExame);
        model.addAttribute("anexosPorExame", anexosPorExame);

        model.addAttribute("exameParaAbrir", abrir);

        return "exames";
    }

    // ==========================================
    // ABRIR FORMULÁRIO DE SOLICITAÇÃO
    // ==========================================

    @GetMapping("/novo/{consultaId}")
    public String novoExame(
            @PathVariable Long consultaId,
            Authentication authentication,
            Model model) {

        Usuario usuarioLogado = obterUsuarioLogado(authentication);

        Consulta consulta = consultaService.buscarPorId(consultaId);

        validarPermissaoProfissional(
                consulta.getAgendamento().getProfissional(),
                usuarioLogado
        );

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("consulta", consulta);

        return "exame-form";
    }

    // ==========================================
    // SOLICITAR EXAME
    // ==========================================

    @PostMapping("/solicitar/{consultaId}")
    public String solicitarExame(
            @PathVariable Long consultaId,
            @RequestParam String tipoExame,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String convenio,
            @RequestParam(required = false) String prioridade,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = obterUsuarioLogado(authentication);

        try {

            exameService.solicitarExame(
                    consultaId,
                    usuarioLogado,
                    tipoExame,
                    descricao,
                    convenio,
                    prioridade
            );

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Exame solicitado com sucesso."
            );

            return "redirect:/exames";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute("erro", e.getMessage());

            return "redirect:/exames/novo/" + consultaId;
        }
    }

    // ==========================================
    // REGISTRAR RESULTADO
    // ==========================================

    @PostMapping("/{id}/resultado")
    public String registrarResultado(
            @PathVariable Long id,
            @RequestParam String descricaoResultado,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = obterUsuarioLogado(authentication);

        try {

            Exame exame = exameService.buscarPorId(id);

            validarPermissaoProfissional(
                    exame.getConsulta().getAgendamento().getProfissional(),
                    usuarioLogado
            );

            resultadoExameService.registrarResultado(
                    id,
                    usuarioLogado.getId(),
                    descricaoResultado
            );

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Resultado registrado com sucesso."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/exames?abrir=" + id;
    }

    // ==========================================
    // INICIAR EMISSÃO DO LAUDO
    // ==========================================

    @PostMapping("/{id}/laudo/iniciar")
    public String iniciarEmissaoLaudo(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = obterUsuarioLogado(authentication);

        try {

            Exame exame = exameService.buscarPorId(id);

            validarPermissaoProfissional(
                    exame.getConsulta().getAgendamento().getProfissional(),
                    usuarioLogado
            );

            resultadoExameService.iniciarEmissaoLaudo(
                    id,
                    usuarioLogado.getId()
            );

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Emissão do laudo iniciada."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/exames?abrir=" + id;
    }

    // ==========================================
    // EMITIR LAUDO
    // ==========================================

    @PostMapping("/{id}/laudo/emitir")
    public String emitirLaudo(
            @PathVariable Long id,
            @RequestParam String descricaoLaudo,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = obterUsuarioLogado(authentication);

        try {

            Exame exame = exameService.buscarPorId(id);

            validarPermissaoProfissional(
                    exame.getConsulta().getAgendamento().getProfissional(),
                    usuarioLogado
            );

            resultadoExameService.emitirLaudo(
                    id,
                    usuarioLogado.getId(),
                    descricaoLaudo
            );

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Laudo emitido com sucesso."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/exames?abrir=" + id;
    }

    // ==========================================
    // ENVIAR ANEXO
    // ==========================================

    @PostMapping("/{id}/anexos")
    public String enviarAnexo(
            @PathVariable Long id,
            @RequestParam("arquivo") MultipartFile arquivo,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = obterUsuarioLogado(authentication);

        try {

            Exame exame = exameService.buscarPorId(id);

            validarPermissaoProfissional(
                    exame.getConsulta().getAgendamento().getProfissional(),
                    usuarioLogado
            );

            anexoExameService.salvar(id, usuarioLogado, arquivo);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Arquivo enviado com sucesso."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/exames?abrir=" + id;
    }

    // ==========================================
    // CANCELAR EXAME
    // ==========================================

    @PostMapping("/{id}/cancelar")
    public String cancelarExame(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = obterUsuarioLogado(authentication);

        try {

            exameService.cancelarExame(id, usuarioLogado);

            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Exame cancelado com sucesso."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/exames?abrir=" + id;
    }

    // ==========================================
    // BAIXAR ANEXO
    // ==========================================

    @GetMapping("/{exameId}/anexos/{anexoId}/download")
    public ResponseEntity<FileSystemResource> baixarAnexo(
            @PathVariable Long exameId,
            @PathVariable Long anexoId,
            Authentication authentication) {

        Usuario usuarioLogado = obterUsuarioLogado(authentication);

        Exame exame = exameService.buscarPorId(exameId);

        validarPermissaoProfissional(
                exame.getConsulta().getAgendamento().getProfissional(),
                usuarioLogado
        );

        File arquivo = anexoExameService.buscarArquivo(anexoId, exameId);

        String tipoConteudo = anexoExameService
                .buscarPorIdEExame(anexoId, exameId)
                .getTipoConteudo();

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        if (tipoConteudo != null) {
            try {
                mediaType = MediaType.parseMediaType(tipoConteudo);
            } catch (Exception ignored) {
                // Mantém o tipo genérico.
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + arquivo.getName() + "\""
                )
                .body(new FileSystemResource(arquivo));
    }

    // ==========================================
    // OBTER USUÁRIO LOGADO
    // ==========================================

    private Usuario obterUsuarioLogado(Authentication authentication) {

        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Usuário não encontrado.")
                );
    }

    // ==========================================
    // VALIDAR PERMISSÃO
    // ==========================================

    private void validarPermissaoProfissional(
            Usuario profissional,
            Usuario usuarioLogado) {

        if (profissional == null ||
                !profissional.getId().equals(usuarioLogado.getId())) {

            throw new AccessDeniedException(
                    "Você não tem permissão para acessar este exame."
            );
        }
    }
}
