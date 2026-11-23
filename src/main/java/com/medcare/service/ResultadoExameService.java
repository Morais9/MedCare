
package com.medcare.service;

import com.medcare.model.Exame;
import com.medcare.model.ResultadoExame;
import com.medcare.model.Usuario;

import com.medcare.repository.ExameRepository;
import com.medcare.repository.ResultadoExameRepository;
import com.medcare.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ResultadoExameService {

    private final ResultadoExameRepository resultadoRepository;
    private final ExameRepository exameRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoExameService historicoExameService;

    public ResultadoExameService(
            ResultadoExameRepository resultadoRepository,
            ExameRepository exameRepository,
            UsuarioRepository usuarioRepository,
            HistoricoExameService historicoExameService) {

        this.resultadoRepository = resultadoRepository;
        this.exameRepository = exameRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoExameService = historicoExameService;
    }

    // ==========================================
    // REGISTRAR OU ATUALIZAR RESULTADO
    // ==========================================

    public ResultadoExame registrarResultado(
            Long exameId,
            Long profissionalId,
            String descricaoResultado) {

        if (descricaoResultado == null ||
                descricaoResultado.isBlank()) {

            throw new IllegalArgumentException(
                    "A descrição do resultado é obrigatória."
            );
        }

        Exame exame = buscarExame(exameId);

        if ("Cancelado".equalsIgnoreCase(exame.getStatus())) {
            throw new IllegalArgumentException(
                    "Não é possível registrar resultado para um exame cancelado."
            );
        }

        Usuario profissional = buscarProfissional(profissionalId);

        boolean atualizacao = resultadoRepository
                .findByExameId(exameId)
                .isPresent();

        ResultadoExame resultado = resultadoRepository
                .findByExameId(exameId)
                .orElseGet(ResultadoExame::new);

        resultado.setExame(exame);
        resultado.setProfissional(profissional);
        resultado.setDescricaoResultado(
                descricaoResultado.trim()
        );

        if (resultado.getStatusLaudo() == null ||
                resultado.getStatusLaudo().isBlank()) {

            resultado.setStatusLaudo("Pendente");
        }

        exame.setStatus("Realizado");
        exame.setDataRealizacao(LocalDateTime.now());
        exame.setStatusLaudo(resultado.getStatusLaudo());

        exameRepository.save(exame);

        ResultadoExame resultadoSalvo =
                resultadoRepository.save(resultado);

        historicoExameService.registrar(
                exame,
                profissional,
                atualizacao
                        ? "Resultado atualizado"
                        : "Resultado registrado",
                atualizacao
                        ? "O resultado do exame foi atualizado."
                        : "O resultado do exame foi registrado."
        );

        return resultadoSalvo;
    }

    // ==========================================
    // INICIAR EMISSÃO DO LAUDO
    // ==========================================

    public ResultadoExame iniciarEmissaoLaudo(
            Long exameId,
            Long profissionalId) {

        Exame exame = buscarExame(exameId);

        validarExameRealizado(exame);

        Usuario profissional = buscarProfissional(profissionalId);

        ResultadoExame resultado = buscarResultado(exameId);

        if ("Emitido".equalsIgnoreCase(resultado.getStatusLaudo())) {
            throw new IllegalArgumentException(
                    "Este laudo já foi emitido."
            );
        }

        resultado.setStatusLaudo("Em emissão");
        exame.setStatusLaudo("Em emissão");

        exameRepository.save(exame);

        ResultadoExame resultadoSalvo =
                resultadoRepository.save(resultado);

        historicoExameService.registrar(
                exame,
                profissional,
                "Emissão de laudo iniciada",
                "A emissão do laudo foi iniciada."
        );

        return resultadoSalvo;
    }

    // ==========================================
    // EMITIR LAUDO
    // ==========================================

    public ResultadoExame emitirLaudo(
            Long exameId,
            Long profissionalId,
            String descricaoLaudo) {

        if (descricaoLaudo == null || descricaoLaudo.isBlank()) {
            throw new IllegalArgumentException(
                    "A descrição do laudo é obrigatória."
            );
        }

        Exame exame = buscarExame(exameId);

        validarExameRealizado(exame);

        Usuario profissional = buscarProfissional(profissionalId);

        ResultadoExame resultado = buscarResultado(exameId);

        if ("Emitido".equalsIgnoreCase(resultado.getStatusLaudo())) {
            throw new IllegalArgumentException(
                    "Este laudo já foi emitido."
            );
        }

        resultado.setDescricaoLaudo(descricaoLaudo.trim());
        resultado.setStatusLaudo("Emitido");
        resultado.setDataEmissaoLaudo(LocalDateTime.now());

        exame.setStatusLaudo("Emitido");

        exameRepository.save(exame);

        ResultadoExame resultadoSalvo =
                resultadoRepository.save(resultado);

        historicoExameService.registrar(
                exame,
                profissional,
                "Laudo emitido",
                "O laudo do exame foi emitido."
        );

        return resultadoSalvo;
    }

    // ==========================================
    // BUSCAR RESULTADO
    // ==========================================

    @Transactional(readOnly = true)
    public ResultadoExame buscarPorExame(Long exameId) {
        return buscarResultado(exameId);
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    private Exame buscarExame(Long exameId) {

        return exameRepository.findById(exameId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Exame não encontrado."
                ));
    }

    private Usuario buscarProfissional(Long profissionalId) {

        return usuarioRepository.findById(profissionalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Profissional não encontrado."
                ));
    }

    private ResultadoExame buscarResultado(Long exameId) {

        return resultadoRepository.findByExameId(exameId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resultado não encontrado para este exame."
                ));
    }

    private void validarExameRealizado(Exame exame) {

        if (!"Realizado".equalsIgnoreCase(exame.getStatus())) {
            throw new IllegalArgumentException(
                    "O exame precisa estar realizado antes da emissão do laudo."
            );
        }
    }
}
