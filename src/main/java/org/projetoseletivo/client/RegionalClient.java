package org.projetoseletivo.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/v1/regionais")
@RegisterRestClient(configKey = "regional-api")
public interface RegionalClient {


    record RegionalExterna(Long id, String nome) {
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<RegionalExterna> listar();
}
