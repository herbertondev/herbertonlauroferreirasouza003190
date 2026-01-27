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
import org.projetoseletivo.domain.enums.Ordem;
import org.projetoseletivo.domain.enums.TipoArtista;
import org.projetoseletivo.dto.request.ArtistaRequest;
import org.projetoseletivo.dto.response.ArtistaResponse;
import org.projetoseletivo.dto.response.PaginacaoResponse;
import org.projetoseletivo.service.ArtistaService;

import java.util.Optional;

/**
 * Resource REST para operações de Artistas.
 */
@Path("/v1/artistas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Artistas", description = "CRUD de artistas musicais")
@RolesAllowed({ "USER", "ADMIN" })
public class ArtistaResource {

    @Inject
    ArtistaService artistaService;

    @GET
    @Operation(summary = "Listar artistas", description = "Lista artistas com paginação e filtros")
    @APIResponse(responseCode = "200", description = "Lista de artistas", content = @Content(schema = @Schema(implementation = PaginacaoResponse.class)))
    public Response listar(
            @Parameter(description = "Filtrar por nome (parcial)") @QueryParam("nome") String nome,
            @Parameter(description = "Filtrar por tipo (SOLO ou BANDA)") @QueryParam("tipo") TipoArtista tipo,
            @Parameter(description = "Ordenação (ASC ou DESC)") @QueryParam("ordem") @DefaultValue("ASC") Ordem ordem,
            @Parameter(description = "Número da página (0-indexed)") @QueryParam("pagina") @DefaultValue("0") int pagina,
            @Parameter(description = "Tamanho da página") @QueryParam("tamanho") @DefaultValue("10") int tamanho) {

        PaginacaoResponse<ArtistaResponse> response = artistaService.listar(nome, tipo, ordem, pagina, tamanho);
        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar artista por ID", description = "Retorna um artista específico com seus álbuns")
    @APIResponse(responseCode = "200", description = "Artista encontrado")
    @APIResponse(responseCode = "404", description = "Artista não encontrado")
    public Response buscarPorId(@PathParam("id") Long id) {
        Optional<ArtistaResponse> response = artistaService.buscarPorId(id);

        if (response.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"erro\": \"Artista não encontrado\"}")
                    .build();
        }

        return Response.ok(response.get()).build();
    }

    @POST
    @Operation(summary = "Criar artista", description = "Cria um novo artista")
    @APIResponse(responseCode = "201", description = "Artista criado")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    public Response criar(@Valid ArtistaRequest request) {
        ArtistaResponse response = artistaService.criar(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Atualizar artista", description = "Atualiza um artista existente")
    @APIResponse(responseCode = "200", description = "Artista atualizado")
    @APIResponse(responseCode = "404", description = "Artista não encontrado")
    public Response atualizar(@PathParam("id") Long id, @Valid ArtistaRequest request) {
        Optional<ArtistaResponse> response = artistaService.atualizar(id, request);

        if (response.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"erro\": \"Artista não encontrado\"}")
                    .build();
        }

        return Response.ok(response.get()).build();
    }
}
