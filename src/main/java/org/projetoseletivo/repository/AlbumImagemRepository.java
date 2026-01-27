package org.projetoseletivo.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.projetoseletivo.domain.entity.AlbumImagem;

import java.util.List;

/**
 * Repositório para operações de persistência da entidade AlbumImagem.
 */
@ApplicationScoped
public class AlbumImagemRepository implements PanacheRepository<AlbumImagem> {

    /**
     * Busca imagens por ID do álbum.
     */
    public List<AlbumImagem> buscarPorAlbumId(Long albumId) {
        return list("album.id", albumId);
    }

    /**
     * Remove todas as imagens de um álbum.
     */
    public long removerPorAlbumId(Long albumId) {
        return delete("album.id", albumId);
    }
}
