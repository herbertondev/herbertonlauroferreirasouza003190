package org.projetoseletivo.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.projetoseletivo.domain.enums.TipoArtista;
import org.projetoseletivo.dto.request.AlbumRequest;
import org.projetoseletivo.dto.response.AlbumResponse;
import org.projetoseletivo.dto.response.PaginacaoResponse;
import org.projetoseletivo.service.AlbumService;

import java.util.Optional;

/**
 * Resource REST para operações de Álbuns.
 */
@Path("/v1/albuns")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Álbuns", description = "CRUD de álbuns musicais")
@RolesAllowed({ "USER", "ADMIN" })
public class AlbumResource {

    @Inject
    AlbumService albumService;

    @GET
    @Operation(summary = "Listar álbuns", description = "Lista álbuns com paginação e filtros parametrizados")
    @APIResponse(responseCode = "200", description = "Lista de álbuns", content = @Content(schema = @Schema(implementation = PaginacaoResponse.class)))
    public Response listar(
            @Parameter(description = "Filtrar por título (parcial)") @QueryParam("titulo") String titulo,
            @Parameter(description = "Filtrar por nome do artista") @QueryParam("artista") String nomeArtista,
            @Parameter(description = "Filtrar por tipo de artista (SOLO ou BANDA)") @QueryParam("tipo") TipoArtista tipoArtista,
            @Parameter(description = "Número da página (0-indexed)") @QueryParam("pagina") @DefaultValue("0") int pagina,
            @Parameter(description = "Tamanho da página") @QueryParam("tamanho") @DefaultValue("10") int tamanho) {

        PaginacaoResponse<AlbumResponse> response = albumService.listar(titulo, nomeArtista, tipoArtista, pagina,
                tamanho);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar álbum por ID", description = "Retorna um álbum específico com artistas e imagens")
    @APIResponse(responseCode = "200", description = "Álbum encontrado")
    @APIResponse(responseCode = "404", description = "Álbum não encontrado")
    public Response buscarPorId(@PathParam("id") Long id) {
        Optional<AlbumResponse> response = albumService.buscarPorId(id);

        if (response.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"erro\": \"Álbum não encontrado\"}")
                    .build();
        }

        return Response.ok(response.get()).build();
    }

    @POST
    @Operation(summary = "Criar álbum", description = "Cria um novo álbum")
    @APIResponse(responseCode = "201", description = "Álbum criado")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    public Response criar(@Valid AlbumRequest request) {
        AlbumResponse response = albumService.criar(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualizar álbum", description = "Atualiza um álbum existente")
    @APIResponse(responseCode = "200", description = "Álbum atualizado")
    @APIResponse(responseCode = "404", description = "Álbum não encontrado")
    public Response atualizar(@PathParam("id") Long id, @Valid AlbumRequest request) {
        Optional<AlbumResponse> response = albumService.atualizar(id, request);

        if (response.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"erro\": \"Álbum não encontrado\"}")
                    .build();
        }

        return Response.ok(response.get()).build();
    }
}
