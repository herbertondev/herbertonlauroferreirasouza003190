package org.projetoseletivo.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.projetoseletivo.domain.entity.Album;
import org.projetoseletivo.domain.entity.Artista;
import org.projetoseletivo.domain.enums.Ordem;
import org.projetoseletivo.domain.enums.TipoArtista;
import org.projetoseletivo.dto.request.ArtistaRequest;
import org.projetoseletivo.dto.response.ArtistaResponse;
import org.projetoseletivo.dto.response.PaginacaoResponse;
import org.projetoseletivo.mapper.ArtistaMapper;
import org.projetoseletivo.repository.AlbumRepository;
import org.projetoseletivo.repository.ArtistaRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Serviço para operações de negócio da entidade Artista.
 */
@ApplicationScoped
public class ArtistaService {

    @Inject
    ArtistaRepository artistaRepository;

    @Inject
    AlbumRepository albumRepository;

    @Inject
    ArtistaMapper artistaMapper;

    /**
     * Lista artistas com paginação e ordenação.
     */
    public PaginacaoResponse<ArtistaResponse> listar(String nome, TipoArtista tipo, Ordem ordem, int pagina,
            int tamanhoPagina) {
        List<Artista> artistas;
        long total;

        if (nome != null && !nome.isBlank()) {
            artistas = artistaRepository.buscarPorNome(nome, ordem, pagina, tamanhoPagina);
            total = artistaRepository.contarPorNome(nome);
        } else if (tipo != null) {
            artistas = artistaRepository.buscarPorTipo(tipo, ordem, pagina, tamanhoPagina);
            total = artistaRepository.contarPorTipo(tipo);
        } else {
            artistas = artistaRepository.listarTodos(ordem, pagina, tamanhoPagina);
            total = artistaRepository.contarTodos();
        }

        List<ArtistaResponse> responses = artistaMapper.toResponseList(artistas);
        return PaginacaoResponse.of(responses, pagina, tamanhoPagina, total);
    }

    /**
     * Busca artista por ID com álbuns.
     */
    public Optional<ArtistaResponse> buscarPorId(Long id) {
        return artistaRepository.buscarPorIdComAlbuns(id)
                .map(artistaMapper::toResponse);
    }

    /**
     * Cria um novo artista.
     */
    @Transactional
    public ArtistaResponse criar(ArtistaRequest request) {
        Artista artista = artistaMapper.toEntity(request);

        // Associar álbuns se informados
        if (request.getAlbumIds() != null && !request.getAlbumIds().isEmpty()) {
            Set<Album> albuns = new HashSet<>();
            for (Long albumId : request.getAlbumIds()) {
                albumRepository.findByIdOptional(albumId)
                        .ifPresent(albuns::add);
            }
            artista.setAlbuns(albuns);
        }

        artistaRepository.persist(artista);
        return artistaMapper.toResponse(artista);
    }

    /**
     * Atualiza um artista existente.
     */
    @Transactional
    public Optional<ArtistaResponse> atualizar(Long id, ArtistaRequest request) {
        Optional<Artista> artistaOpt = artistaRepository.findByIdOptional(id);

        if (artistaOpt.isEmpty()) {
            return Optional.empty();
        }

        Artista artista = artistaOpt.get();
        artistaMapper.updateEntity(request, artista);

        // Atualizar álbuns se informados
        if (request.getAlbumIds() != null) {
            Set<Album> albuns = new HashSet<>();
            for (Long albumId : request.getAlbumIds()) {
                albumRepository.findByIdOptional(albumId)
                        .ifPresent(albuns::add);
            }
            artista.setAlbuns(albuns);
        }

        return Optional.of(artistaMapper.toResponse(artista));
    }

    /**
     * Remove um artista.
     */
    @Transactional
    public boolean remover(Long id) {
        return artistaRepository.deleteById(id);
    }
}
