package org.projetoseletivo.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.projetoseletivo.domain.entity.Album;
import org.projetoseletivo.domain.entity.AlbumImagem;
import org.projetoseletivo.domain.entity.Artista;
import org.projetoseletivo.dto.request.AlbumRequest;
import org.projetoseletivo.dto.response.AlbumResponse;
import org.projetoseletivo.dto.response.ImagemResponse;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre entidade Album e DTOs.
 */
@Mapper(componentModel = "cdi")
public interface AlbumMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    @Mapping(target = "artistas", ignore = true)
    @Mapping(target = "imagens", ignore = true)
    Album toEntity(AlbumRequest request);

    @Mapping(target = "artistas", source = "artistas")
    @Mapping(target = "imagens", source = "imagens")
    AlbumResponse toResponse(Album album);

    default List<AlbumResponse.ArtistaResumoResponse> mapArtistas(Set<Artista> artistas) {
        if (artistas == null) {
            return null;
        }
        return artistas.stream()
                .map(artista -> AlbumResponse.ArtistaResumoResponse.builder()
                        .id(artista.getId())
                        .nome(artista.getNome())
                        .tipo(artista.getTipo())
                        .build())
                .collect(Collectors.toList());
    }

    default List<ImagemResponse> mapImagens(List<AlbumImagem> imagens) {
        if (imagens == null) {
            return null;
        }
        return imagens.stream()
                .map(img -> ImagemResponse.builder()
                        .id(img.getId())
                        .bucket(img.getBucket())
                        .objectKey(img.getObjectKey())
                        .contentType(img.getContentType())
                        .criadoEm(img.getCriadoEm())
                        .build())
                .collect(Collectors.toList());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    @Mapping(target = "artistas", ignore = true)
    @Mapping(target = "imagens", ignore = true)
    void updateEntity(AlbumRequest request, @MappingTarget Album album);

    List<AlbumResponse> toResponseList(List<Album> albuns);
}
