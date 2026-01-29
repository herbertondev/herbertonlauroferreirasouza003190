package org.projetoseletivo.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

/**
 * Health Check customizado para verificar conectividade com o banco de dados.
 */
@Readiness
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    EntityManager entityManager;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("Database");

        try {
            // Executa uma query simples para verificar conexão
            entityManager.createNativeQuery("SELECT 1").getSingleResult();

            return builder
                    .up()
                    .withData("database", "PostgreSQL")
                    .withData("status", "connected")
                    .build();
        } catch (Exception e) {
            return builder
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
