package org.projetoseletivo.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.projetoseletivo.domain.entity.Regional;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de persistência da entidade Regional.
 */
@ApplicationScoped
public class RegionalRepository implements PanacheRepositoryBase<Regional, Long> {

    /**
     * Busca todas as regionais ativas.
     */
    public List<Regional> buscarAtivas() {
        return list("ativo", true);
    }

    /**
     * Busca todas as regionais (ativas e inativas).
     */
    public List<Regional> buscarTodas() {
        return listAll();
    }

    /**
     * Busca regional ativa por ID externo (referência da API).
     */
    public Optional<Regional> buscarAtivaPorIdExterno(Long idExterno) {
        return find("idExterno = ?1 AND ativo = true", idExterno).firstResultOptional();
    }

    /**
     * Busca qualquer regional por ID externo (ativa ou inativa).
     */
    public Optional<Regional> buscarPorIdExterno(Long idExterno) {
        return find("idExterno = ?1 ORDER BY criadoEm DESC", idExterno).firstResultOptional();
    }

    /**
     * Inativa todas as regionais com o ID externo especificado.
     */
    public int inativarPorIdExterno(Long idExterno) {
        return update("ativo = false WHERE idExterno = ?1 AND ativo = true", idExterno);
    }

    /**
     * Busca IDs externos de todas as regionais ativas.
     */
    public List<Long> buscarIdsExternosAtivos() {
        return getEntityManager()
                .createQuery("SELECT r.idExterno FROM Regional r WHERE r.ativo = true", Long.class)
                .getResultList();
    }
}
