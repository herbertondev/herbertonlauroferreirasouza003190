package org.projetoseletivo.mapper;

import org.hibernate.Hibernate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.projetoseletivo.domain.entity.Album;
import org.projetoseletivo.domain.entity.Artista;
import org.projetoseletivo.dto.request.ArtistaRequest;
import org.projetoseletivo.dto.response.ArtistaResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre entidade Artista e DTOs.
 */
@Mapper(componentModel = "cdi")
public interface ArtistaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    @Mapping(target = "albuns", ignore = true)
    Artista toEntity(ArtistaRequest request);

    @Mapping(target = "albuns", source = "albuns")
    ArtistaResponse toResponse(Artista artista);

    default List<ArtistaResponse.AlbumResumoResponse> mapAlbuns(Set<Album> albuns) {
        // Verifica se a coleção é null ou não foi inicializada pelo Hibernate
        if (albuns == null || !Hibernate.isInitialized(albuns)) {
            return new ArrayList<>();
        }
        // Cópia defensiva para evitar ConcurrentModificationException
        return new ArrayList<>(albuns).stream()
                .map(album -> ArtistaResponse.AlbumResumoResponse.builder()
                        .id(album.getId())
                        .titulo(album.getTitulo())
                        .anoLancamento(album.getAnoLancamento())
                        .build())
                .collect(Collectors.toList());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    @Mapping(target = "albuns", ignore = true)
    void updateEntity(ArtistaRequest request, @MappingTarget Artista artista);

    List<ArtistaResponse> toResponseList(List<Artista> artistas);
}
