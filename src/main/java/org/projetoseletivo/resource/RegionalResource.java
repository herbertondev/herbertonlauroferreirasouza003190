package org.projetoseletivo.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.projetoseletivo.domain.entity.Regional;
import org.projetoseletivo.service.RegionalService;

import java.util.List;


@Path("/v1/regionais")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Regionais", description = "Sincronização e listagem de regionais")
@RolesAllowed({ "USER", "ADMIN" })
public class RegionalResource {

    @Inject
    RegionalService regionalService;

    @GET
    @Operation(summary = "Listar regionais", description = "Lista todas as regionais ativas")
    @APIResponse(responseCode = "200", description = "Lista de regionais ativas")
    public Response listar(@QueryParam("incluirInativas") @DefaultValue("false") boolean incluirInativas) {
        List<Regional> regionais = incluirInativas
                ? regionalService.listarTodas()
                : regionalService.listarAtivas();
        return Response.ok(regionais).build();
    }

    @POST
    @Path("/sincronizar")
    @Operation(summary = "Sincronizar regionais", description = "Sincroniza regionais com a API externa. Novo → inserir, Ausente → inativar, Alterado → versionar")
    @APIResponse(responseCode = "200", description = "Sincronização realizada")
    @APIResponse(responseCode = "500", description = "Erro ao sincronizar")
    @RolesAllowed("ADMIN")
    public Response sincronizar() {
        try {
            RegionalService.SincronizacaoResultado resultado = regionalService.sincronizar();
            return Response.ok(resultado).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"erro\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}
