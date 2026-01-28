package org.projetoseletivo.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.projetoseletivo.dto.request.LoginRequest;
import org.projetoseletivo.dto.response.TokenResponse;
import org.projetoseletivo.service.AuthService;

import java.util.Optional;

/**
 * Resource de autenticação JWT.
 */
@Path("/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticação", description = "Endpoints de autenticação JWT")
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "Realizar login", description = "Autentica o usuário e retorna tokens JWT")
    @APIResponse(responseCode = "200", description = "Login realizado com sucesso", content = @Content(schema = @Schema(implementation = TokenResponse.class)))
    @APIResponse(responseCode = "401", description = "Credenciais inválidas")
    public Response login(@Valid LoginRequest loginRequest) {
        Optional<TokenResponse> tokenOpt = authService.login(loginRequest);

        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"erro\": \"Credenciais inválidas\"}")
                    .build();
        }

        return Response.ok(tokenOpt.get()).build();
    }

    @POST
    @Path("/refresh")
    @PermitAll
    @Operation(summary = "Renovar token", description = "Renova o access token usando o refresh token")
    @APIResponse(responseCode = "200", description = "Token renovado com sucesso", content = @Content(schema = @Schema(implementation = TokenResponse.class)))
    @APIResponse(responseCode = "401", description = "Refresh token inválido")
    public Response refresh(@HeaderParam("X-Refresh-Token") String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"erro\": \"Refresh token é obrigatório\"}")
                    .build();
        }

        // Em produção, validar o refresh token corretamente
        String username = jwt.getName();
        if (username == null) {
            username = "admin"; // fallback para dev
        }

        Optional<TokenResponse> tokenOpt = authService.refresh(refreshToken, username);

        if (tokenOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"erro\": \"Refresh token inválido\"}")
                    .build();
        }

        return Response.ok(tokenOpt.get()).build();
    }
}
