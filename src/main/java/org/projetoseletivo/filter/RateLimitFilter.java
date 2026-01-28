package org.projetoseletivo.filter;

import io.github.bucket4j.Bucket;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Provider
@ApplicationScoped
@Priority(Priorities.AUTHENTICATION + 1)
public class RateLimitFilter implements ContainerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @ConfigProperty(name = "rate.limit.requests.per.minute", defaultValue = "10")
    int requestsPerMinute;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Não aplicar rate limit em endpoints públicos
        String path = requestContext.getUriInfo().getPath();
        if (path.contains("/auth/login") || path.contains("/q/health") || path.contains("/openapi")
                || path.contains("/swagger")) {
            return;
        }

        String clientId = getClientId(requestContext);
        Bucket bucket = buckets.computeIfAbsent(clientId, this::createBucket);

        if (!bucket.tryConsume(1)) {
            requestContext.abortWith(
                    Response.status(Response.Status.TOO_MANY_REQUESTS)
                            .entity("{\"erro\": \"Limite de requisições excedido. Tente novamente em 1 minuto.\"}")
                            .header("Retry-After", "60")
                            .build());
        }
    }

    /**
     * Obtém o identificador do cliente (usuário autenticado ou IP).
     */
    private String getClientId(ContainerRequestContext requestContext) {
        // Tenta obter do token JWT
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Usa o hash do token como identificador (simplificado)
            return "user:" + authHeader.hashCode();
        }

        // Fallback para IP
        String forwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (forwardedFor != null) {
            return "ip:" + forwardedFor.split(",")[0].trim();
        }

        return "ip:unknown";
    }

    /**
     * Cria um bucket com limite de N requisições por minuto.
     */
    private Bucket createBucket(String clientId) {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(requestsPerMinute)
                        .refillGreedy(requestsPerMinute, Duration.ofMinutes(1)))
                .build();
    }
}
