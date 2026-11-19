
package com.medcare.controller;

import com.medcare.model.Usuario;
import com.medcare.repository.UsuarioRepository;
import com.medcare.service.UsuarioService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public HomeController(
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository) {

        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping({"/", "/index", "/login"})
    public String login(
            Model model,
            @RequestParam(value = "aguardandoAprovacao", required = false)
            String aguardandoAprovacao) {

        model.addAttribute("usuario", new Usuario());

        model.addAttribute(
                "aguardandoAprovacao",
                aguardandoAprovacao != null
        );

        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Usuário não encontrado."));

        model.addAttribute("usuarioLogado", usuario);
        model.addAttribute("nomeUsuario", usuario.getNome());
        model.addAttribute("cargoUsuario", usuario.getCargo());

        String nome = usuario.getNome() == null
                ? "U"
                : usuario.getNome().trim();

        String[] partes = nome.split("\\s+");

        String iniciais = partes.length > 1
                ? partes[0].substring(0, 1) +
                partes[partes.length - 1].substring(0, 1)
                : nome.substring(0, 1);

        model.addAttribute("iniciaisUsuario", iniciais.toUpperCase());

        return "dashboard";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(
            @ModelAttribute("usuario") Usuario usuario,
            @RequestParam("confirmarSenha") String confirmarSenha,
            Model model) {

        try {
            usuarioService.cadastrar(usuario, confirmarSenha);
            return "redirect:/index?cadastroSucesso=true";

        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("usuario", usuario);
            return "cadastro";
        }
    }

    @GetMapping("/agendamentos")
    public String agendamentos(
            Authentication authentication,
            Model model) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Usuário não encontrado."));

        // Disponibiliza o usuário completo para o Thymeleaf
        model.addAttribute("usuarioLogado", usuario);

        model.addAttribute("nomeUsuario", usuario.getNome());
        model.addAttribute("cargoUsuario", usuario.getCargo());

        // Gera as iniciais do usuário
        String nome = usuario.getNome() == null
                ? "U"
                : usuario.getNome().trim();

        String[] nomes = nome.split("\\s+");

        String iniciais = nomes.length > 1
                ? nomes[0].substring(0, 1)
                + nomes[nomes.length - 1].substring(0, 1)
                : nome.substring(0, 1);

        model.addAttribute("iniciaisUsuario", iniciais.toUpperCase());

        return "agendamentos";
    }
}
