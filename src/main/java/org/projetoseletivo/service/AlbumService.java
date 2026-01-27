package org.projetoseletivo.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.projetoseletivo.domain.entity.Album;
import org.projetoseletivo.domain.entity.Artista;
import org.projetoseletivo.domain.enums.TipoArtista;
import org.projetoseletivo.dto.request.AlbumRequest;
import org.projetoseletivo.dto.response.AlbumResponse;
import org.projetoseletivo.dto.response.ImagemResponse;
import org.projetoseletivo.dto.response.PaginacaoResponse;
import org.projetoseletivo.mapper.AlbumMapper;
import org.projetoseletivo.repository.AlbumRepository;
import org.projetoseletivo.repository.ArtistaRepository;
import org.projetoseletivo.websocket.AlbumNotificacaoSocket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço para operações de negócio da entidade Album.
 */
@ApplicationScoped
public class AlbumService {

    @Inject
    AlbumRepository albumRepository;

    @Inject
    ArtistaRepository artistaRepository;

    @Inject
    AlbumMapper albumMapper;

    @Inject
    MinioService minioService;

    @Inject
    AlbumNotificacaoSocket notificacaoSocket;

    /**
     * Lista álbuns com paginação e filtros, ordenados alfabeticamente por título.
     */
    public PaginacaoResponse<AlbumResponse> listar(
            String titulo,
            String nomeArtista,
            TipoArtista tipoArtista,
            int pagina,
            int tamanhoPagina) {

        List<Album> albuns;
        long total;

        if (nomeArtista != null && !nomeArtista.isBlank()) {
            albuns = albumRepository.buscarPorNomeArtista(nomeArtista, pagina, tamanhoPagina);
            total = albumRepository.contarPorNomeArtista(nomeArtista);
        } else if (tipoArtista != null) {
            albuns = albumRepository.buscarPorTipoArtista(tipoArtista, pagina, tamanhoPagina);
            total = albumRepository.contarPorTipoArtista(tipoArtista);
        } else if (titulo != null && !titulo.isBlank()) {
            albuns = albumRepository.buscarPorTitulo(titulo, pagina, tamanhoPagina);
            total = albumRepository.contarPorTitulo(titulo);
        } else {
            albuns = albumRepository.listarTodos(pagina, tamanhoPagina);
            total = albumRepository.contarTodos();
        }

        List<AlbumResponse> responses = albuns.stream()
                .map(this::toResponseComUrls)
                .collect(Collectors.toList());

        return PaginacaoResponse.of(responses, pagina, tamanhoPagina, total);
    }

    /**
     * Busca álbum por ID com artistas e imagens.
     */
    public Optional<AlbumResponse> buscarPorId(Long id) {
        return albumRepository.buscarPorIdCompleto(id)
                .map(this::toResponseComUrls);
    }

    /**
     * Cria um novo álbum.
     */
    @Transactional
    public AlbumResponse criar(AlbumRequest request) {
        Album album = albumMapper.toEntity(request);

        // Associar artistas se informados
        if (request.getArtistaIds() != null && !request.getArtistaIds().isEmpty()) {
            Set<Artista> artistas = buscarEValidarArtistas(request.getArtistaIds());
            for (Artista artista : artistas) {
                album.getArtistas().add(artista);
                artista.getAlbuns().add(album);
            }
        }

        albumRepository.persist(album);

        // Notificar clientes WebSocket
        AlbumResponse response = albumMapper.toResponse(album);
        notificacaoSocket.notificarNovoAlbum(response);

        return response;
    }

    /**
     * Atualiza um álbum existente.
     */
    @Transactional
    public Optional<AlbumResponse> atualizar(Long id, AlbumRequest request) {
        Optional<Album> albumOpt = albumRepository.findByIdOptional(id);

        if (albumOpt.isEmpty()) {
            return Optional.empty();
        }

        Album album = albumOpt.get();
        albumMapper.updateEntity(request, album);

        // Atualizar artistas se informados
        if (request.getArtistaIds() != null) {
            // Remover associações antigas
            for (Artista artista : album.getArtistas()) {
                artista.getAlbuns().remove(album);
            }
            album.getArtistas().clear();

            // Adicionar novas associações (se a lista não estiver vazia, valida os IDs)
            if (!request.getArtistaIds().isEmpty()) {
                Set<Artista> artistas = buscarEValidarArtistas(request.getArtistaIds());
                for (Artista artista : artistas) {
                    album.getArtistas().add(artista);
                    artista.getAlbuns().add(album);
                }
            }
        }

        return Optional.of(albumMapper.toResponse(album));
    }

    /**
     * Remove um álbum.
     */
    @Transactional
    public boolean remover(Long id) {
        return albumRepository.deleteById(id);
    }

    /**
     * Busca e valida se todos os artistas existem.
     * Lança IllegalArgumentException se algum artista não for encontrado.
     */
    private Set<Artista> buscarEValidarArtistas(List<Long> artistaIds) {
        Set<Artista> artistas = new HashSet<>();
        List<Long> idsNaoEncontrados = new ArrayList<>();

        for (Long artistaId : artistaIds) {
            Optional<Artista> artistaOpt = artistaRepository.findByIdOptional(artistaId);
            if (artistaOpt.isPresent()) {
                artistas.add(artistaOpt.get());
            } else {
                idsNaoEncontrados.add(artistaId);
            }
        }

        if (!idsNaoEncontrados.isEmpty()) {
            throw new IllegalArgumentException(
                    "Artista(s) não encontrado(s) com ID(s): " + idsNaoEncontrados);
        }

        return artistas;
    }

    /**
     * Converte álbum para response incluindo URLs pré-assinadas das imagens.
     */
    private AlbumResponse toResponseComUrls(Album album) {
        AlbumResponse response = albumMapper.toResponse(album);

        // Gerar URLs pré-assinadas para as imagens
        if (response.getImagens() != null) {
            response.setImagens(response.getImagens().stream()
                    .map(img -> {
                        String url = minioService.gerarUrlPreAssinada(img.getBucket(), img.getObjectKey());
                        return ImagemResponse.builder()
                                .id(img.getId())
                                .bucket(img.getBucket())
                                .objectKey(img.getObjectKey())
                                .contentType(img.getContentType())
                                .url(url)
                                .criadoEm(img.getCriadoEm())
                                .build();
                    })
                    .collect(Collectors.toList()));
        }

        return response;
    }
}
