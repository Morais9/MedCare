package com.medcare.service;

import com.medcare.model.Exame;
import com.medcare.model.HistoricoExame;
import com.medcare.model.Usuario;

import com.medcare.repository.HistoricoExameRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HistoricoExameService {

    private final HistoricoExameRepository historicoRepository;

    public HistoricoExameService(
            HistoricoExameRepository historicoRepository) {

        this.historicoRepository = historicoRepository;
    }

    // Registrar um evento no histórico
    public HistoricoExame registrar(
            Exame exame,
            Usuario profissional,
            String acao,
            String descricao) {

        if (exame == null) {
            throw new IllegalArgumentException(
                    "O exame é obrigatório para registrar o histórico."
            );
        }

        if (profissional == null) {
            throw new IllegalArgumentException(
                    "O profissional é obrigatório para registrar o histórico."
            );
        }

        if (acao == null || acao.isBlank()) {
            throw new IllegalArgumentException(
                    "A ação do histórico é obrigatória."
            );
        }

        HistoricoExame historico = new HistoricoExame();

        historico.setExame(exame);
        historico.setProfissional(profissional);
        historico.setAcao(acao);
        historico.setDescricao(descricao);

        return historicoRepository.save(historico);
    }

    // Buscar histórico completo de um exame
    @Transactional(readOnly = true)
    public List<HistoricoExame> listarPorExame(Long exameId) {

        return historicoRepository
                .findByExameIdOrderByDataEventoAsc(exameId);
    }

    // Buscar histórico de um profissional
    @Transactional(readOnly = true)
    public List<HistoricoExame> listarPorProfissional(
            Long profissionalId) {

        return historicoRepository
                .findByProfissionalIdOrderByDataEventoDesc(
                        profissionalId
                );
    }
}
