package org.projetoseletivo.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.projetoseletivo.client.RegionalClient;
import org.projetoseletivo.domain.entity.Regional;
import org.projetoseletivo.repository.RegionalRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço para sincronização de regionais com a API externa.
 * 
 * Lógica de sincronização conforme requisitos:
 * 1) Novo no endpoint → inserir
 * 2) Ausente no endpoint → inativar
 * 3) Atributo alterado → inativar antigo e criar NOVO registro
 */
@ApplicationScoped
public class RegionalService {

    private static final Logger LOG = Logger.getLogger(RegionalService.class);

    @Inject
    @RestClient
    RegionalClient regionalClient;

    @Inject
    RegionalRepository regionalRepository;

    /**
     * Sincroniza regionais com a API externa.
     */
    @Transactional
    public SincronizacaoResultado sincronizar() {
        int inseridos = 0;
        int inativados = 0;
        int atualizados = 0;

        try {
            // Buscar regionais da API externa
            List<RegionalClient.RegionalExterna> regionaisExternas = regionalClient.listar();

            // Buscar regionais ativas no banco
            List<Regional> regionaisAtivas = regionalRepository.buscarAtivas();
            Map<Long, Regional> mapaAtivas = regionaisAtivas.stream()
                    .collect(Collectors.toMap(Regional::getIdExterno, r -> r));

            // IDs externos das regionais da API
            Set<Long> idsExternos = regionaisExternas.stream()
                    .map(RegionalClient.RegionalExterna::id)
                    .collect(Collectors.toSet());

            // 1. Inativar regionais que não estão mais na API (requisito 2)
            for (Regional regional : regionaisAtivas) {
                if (!idsExternos.contains(regional.getIdExterno())) {
                    regionalRepository.inativarPorIdExterno(regional.getIdExterno());
                    inativados++;
                    LOG.infov("Regional inativada (ausente na API): id_externo={0}, nome={1}",
                            regional.getIdExterno(), regional.getNome());
                }
            }

            // 2. Processar regionais da API
            for (RegionalClient.RegionalExterna externa : regionaisExternas) {
                Regional existenteAtiva = mapaAtivas.get(externa.id());

                if (existenteAtiva == null) {
                    // Não existe ativa - inserir nova (requisito 1)
                    Regional nova = Regional.builder()
                            .idExterno(externa.id())
                            .nome(externa.nome())
                            .ativo(true)
                            .build();
                    regionalRepository.persist(nova);
                    inseridos++;
                    LOG.infov("Regional inserida: id_externo={0}, nome={1}", externa.id(), externa.nome());

                } else if (!existenteAtiva.getNome().equals(externa.nome())) {
                    // Nome alterado - inativar antiga e criar NOVA (requisito 3)
                    regionalRepository.inativarPorIdExterno(externa.id());

                    Regional nova = Regional.builder()
                            .idExterno(externa.id())
                            .nome(externa.nome())
                            .ativo(true)
                            .build();
                    regionalRepository.persist(nova); // Cria novo registro com novo ID
                    atualizados++;
                    LOG.infov("Regional atualizada: id_externo={0}, nome antigo={1}, nome novo={2}",
                            externa.id(), existenteAtiva.getNome(), externa.nome());
                }
                // Se existente com mesmo nome, não faz nada
            }

        } catch (Exception e) {
            LOG.errorv(e, "Erro ao sincronizar regionais");
            throw new RuntimeException("Erro ao sincronizar regionais: " + e.getMessage(), e);
        }

        return new SincronizacaoResultado(inseridos, inativados, atualizados);
    }

    /**
     * Lista todas as regionais ativas.
     */
    public List<Regional> listarAtivas() {
        return regionalRepository.buscarAtivas();
    }

    /**
     * Lista todas as regionais (ativas e inativas).
     */
    public List<Regional> listarTodas() {
        return regionalRepository.buscarTodas();
    }

    /**
     * Resultado da sincronização.
     */
    public record SincronizacaoResultado(int inseridos, int inativados, int atualizados) {
    }
}
