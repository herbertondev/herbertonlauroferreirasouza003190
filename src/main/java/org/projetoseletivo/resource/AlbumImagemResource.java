package org.projetoseletivo.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.projetoseletivo.domain.entity.Album;
import org.projetoseletivo.domain.entity.AlbumImagem;
import org.projetoseletivo.dto.response.ImagemResponse;
import org.projetoseletivo.repository.AlbumImagemRepository;
import org.projetoseletivo.repository.AlbumRepository;
import org.projetoseletivo.service.MinioService;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resource REST para upload de imagens de capa dos álbuns.
 */
@Path("/v1/albuns/{albumId}/imagens")
@Tag(name = "Imagens de Álbuns", description = "Upload e listagem de imagens de capa")
@RolesAllowed({ "USER", "ADMIN" })
public class AlbumImagemResource {

    @Inject
    MinioService minioService;

    @Inject
    AlbumRepository albumRepository;

    @Inject
    AlbumImagemRepository albumImagemRepository;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(summary = "Upload de imagens", description = "Faz upload de uma ou mais imagens de capa do álbum")
    @APIResponse(responseCode = "201", description = "Imagens enviadas com sucesso")
    @APIResponse(responseCode = "404", description = "Álbum não encontrado")
    @APIResponse(responseCode = "400", description = "Nenhuma imagem enviada")
    public Response upload(
            @Parameter(description = "ID do álbum") @PathParam("albumId") Long albumId,
            @RestForm("arquivos") List<FileUpload> arquivos) {

        // Verificar se álbum existe
        Optional<Album> albumOpt = albumRepository.findByIdOptional(albumId);
        if (albumOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"erro\": \"Álbum não encontrado\"}")
                    .build();
        }

        if (arquivos == null || arquivos.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"erro\": \"Nenhuma imagem enviada\"}")
                    .build();
        }

        Album album = albumOpt.get();
        List<ImagemResponse> imagensEnviadas = new ArrayList<>();

        for (FileUpload arquivo : arquivos) {
            try (FileInputStream fis = new FileInputStream(arquivo.uploadedFile().toFile())) {
                // Gerar nome único para o arquivo
                String extensao = getExtensao(arquivo.fileName());
                String objectKey = String.format("album-%d/%s.%s",
                        albumId,
                        UUID.randomUUID().toString(),
                        extensao);

                // Upload para MinIO
                minioService.upload(objectKey, fis, arquivo.size(), arquivo.contentType());

                // Salvar referência no banco
                AlbumImagem imagem = AlbumImagem.builder()
                        .album(album)
                        .bucket(minioService.getBucketName())
                        .objectKey(objectKey)
                        .contentType(arquivo.contentType())
                        .build();

                albumImagemRepository.persist(imagem);

                // Gerar URL pré-assinada
                String url = minioService.gerarUrlPreAssinada(imagem.getBucket(), objectKey);

                imagensEnviadas.add(ImagemResponse.builder()
                        .id(imagem.getId())
                        .bucket(imagem.getBucket())
                        .objectKey(imagem.getObjectKey())
                        .contentType(imagem.getContentType())
                        .url(url)
                        .criadoEm(imagem.getCriadoEm())
                        .build());

            } catch (IOException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"erro\": \"Erro ao processar arquivo: " + arquivo.fileName() + "\"}")
                        .build();
            }
        }

        return Response.status(Response.Status.CREATED).entity(imagensEnviadas).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Listar imagens", description = "Lista imagens do álbum com URLs pré-assinadas (30 min)")
    @APIResponse(responseCode = "200", description = "Lista de imagens com URLs")
    @APIResponse(responseCode = "404", description = "Álbum não encontrado")
    public Response listar(@PathParam("albumId") Long albumId) {
        // Verificar se álbum existe
        if (!albumRepository.findByIdOptional(albumId).isPresent()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"erro\": \"Álbum não encontrado\"}")
                    .build();
        }

        List<AlbumImagem> imagens = albumImagemRepository.buscarPorAlbumId(albumId);

        List<ImagemResponse> responses = imagens.stream()
                .map(img -> ImagemResponse.builder()
                        .id(img.getId())
                        .bucket(img.getBucket())
                        .objectKey(img.getObjectKey())
                        .contentType(img.getContentType())
                        .url(minioService.gerarUrlPreAssinada(img.getBucket(), img.getObjectKey()))
                        .criadoEm(img.getCriadoEm())
                        .build())
                .collect(Collectors.toList());

        return Response.ok(responses).build();
    }

    private String getExtensao(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
