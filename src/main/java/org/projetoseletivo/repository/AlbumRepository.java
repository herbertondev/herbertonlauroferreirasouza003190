package org.projetoseletivo.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.projetoseletivo.domain.entity.Album;
import org.projetoseletivo.domain.enums.TipoArtista;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de persistência da entidade Album.
 */
@ApplicationScoped
public class AlbumRepository implements PanacheRepository<Album> {

        /**
         * Lista álbuns com paginação, ordenados alfabeticamente por título.
         */
        public List<Album> listarTodos(int pagina, int tamanhoPagina) {
                return findAll(Sort.ascending("titulo"))
                                .page(Page.of(pagina, tamanhoPagina))
                                .list();
        }

        /**
         * Busca álbuns por título (parcial, case-insensitive), ordenados
         * alfabeticamente.
         */
        public List<Album> buscarPorTitulo(String titulo, int pagina, int tamanhoPagina) {
                return find("LOWER(titulo) LIKE LOWER(?1)", Sort.ascending("titulo"), "%" + titulo + "%")
                                .page(Page.of(pagina, tamanhoPagina))
                                .list();
        }

        /**
         * Busca álbuns por nome do artista, ordenados alfabeticamente por título.
         */
        public List<Album> buscarPorNomeArtista(String nomeArtista, int pagina, int tamanhoPagina) {
                return find("SELECT DISTINCT a FROM Album a JOIN a.artistas art WHERE LOWER(art.nome) LIKE LOWER(?1)",
                                Sort.ascending("titulo"), "%" + nomeArtista + "%")
                                .page(Page.of(pagina, tamanhoPagina))
                                .list();
        }

        /**
         * Busca álbuns por tipo de artista (SOLO ou BANDA), ordenados alfabeticamente
         * por título.
         */
        public List<Album> buscarPorTipoArtista(TipoArtista tipo, int pagina, int tamanhoPagina) {
                return find("SELECT DISTINCT a FROM Album a JOIN a.artistas art WHERE art.tipo = ?1",
                                Sort.ascending("titulo"), tipo)
                                .page(Page.of(pagina, tamanhoPagina))
                                .list();
        }

        /**
         * Busca álbum por ID com artistas e imagens carregados.
         */
        public Optional<Album> buscarPorIdCompleto(Long id) {
                return find("SELECT a FROM Album a LEFT JOIN FETCH a.artistas LEFT JOIN FETCH a.imagens WHERE a.id = ?1",
                                id)
                                .firstResultOptional();
        }

        /**
         * Conta o total de álbuns.
         */
        public long contarTodos() {
                return count();
        }

        /**
         * Conta álbuns por título.
         */
        public long contarPorTitulo(String titulo) {
                return count("LOWER(titulo) LIKE LOWER(?1)", "%" + titulo + "%");
        }

        /**
         * Conta álbuns por tipo de artista.
         */
        public long contarPorTipoArtista(TipoArtista tipo) {
                return getEntityManager()
                                .createQuery("SELECT COUNT(DISTINCT a) FROM Album a JOIN a.artistas art WHERE art.tipo = :tipo",
                                                Long.class)
                                .setParameter("tipo", tipo)
                                .getSingleResult();
        }

        /**
         * Conta álbuns por nome do artista.
         */
        public long contarPorNomeArtista(String nomeArtista) {
                return getEntityManager()
                                .createQuery(
                                                "SELECT COUNT(DISTINCT a) FROM Album a JOIN a.artistas art WHERE LOWER(art.nome) LIKE LOWER(:nome)",
                                                Long.class)
                                .setParameter("nome", "%" + nomeArtista + "%")
                                .getSingleResult();
        }
}
