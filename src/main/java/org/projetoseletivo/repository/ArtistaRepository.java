package org.projetoseletivo.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.projetoseletivo.domain.entity.Artista;
import org.projetoseletivo.domain.enums.Ordem;
import org.projetoseletivo.domain.enums.TipoArtista;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de persistência da entidade Artista.
 */
@ApplicationScoped
public class ArtistaRepository implements PanacheRepository<Artista> {

    /**
     * Busca artistas por nome (parcial, case-insensitive) com ordenação e álbuns.
     */
    public List<Artista> buscarPorNome(String nome, Ordem ordem, int pagina, int tamanhoPagina) {
        String orderDirection = ordem == Ordem.DESC ? "DESC" : "ASC";
        String query = "SELECT DISTINCT a FROM Artista a LEFT JOIN FETCH a.albuns WHERE LOWER(a.nome) LIKE LOWER(?1) ORDER BY a.nome "
                + orderDirection;

        return getEntityManager().createQuery(query, Artista.class)
                .setParameter(1, "%" + nome + "%")
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    /**
     * Busca todos os artistas com paginação, ordenação e álbuns.
     */
    public List<Artista> listarTodos(Ordem ordem, int pagina, int tamanhoPagina) {
        String orderDirection = ordem == Ordem.DESC ? "DESC" : "ASC";
        String query = "SELECT DISTINCT a FROM Artista a LEFT JOIN FETCH a.albuns ORDER BY a.nome " + orderDirection;

        return getEntityManager().createQuery(query, Artista.class)
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    /**
     * Busca artistas por tipo (SOLO ou BANDA) com álbuns.
     */
    public List<Artista> buscarPorTipo(TipoArtista tipo, Ordem ordem, int pagina, int tamanhoPagina) {
        String orderDirection = ordem == Ordem.DESC ? "DESC" : "ASC";
        String query = "SELECT DISTINCT a FROM Artista a LEFT JOIN FETCH a.albuns WHERE a.tipo = ?1 ORDER BY a.nome "
                + orderDirection;

        return getEntityManager().createQuery(query, Artista.class)
                .setParameter(1, tipo)
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    /**
     * Conta o total de artistas.
     */
    public long contarTodos() {
        return count();
    }

    /**
     * Conta artistas por nome.
     */
    public long contarPorNome(String nome) {
        return count("LOWER(nome) LIKE LOWER(?1)", "%" + nome + "%");
    }

    /**
     * Conta artistas por tipo.
     */
    public long contarPorTipo(TipoArtista tipo) {
        return count("tipo", tipo);
    }

    /**
     * Busca artista por ID com álbuns carregados.
     */
    public Optional<Artista> buscarPorIdComAlbuns(Long id) {
        return find("SELECT a FROM Artista a LEFT JOIN FETCH a.albuns WHERE a.id = ?1", id)
                .firstResultOptional();
    }
}
