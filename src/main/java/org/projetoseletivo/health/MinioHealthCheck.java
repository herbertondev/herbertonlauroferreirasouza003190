package org.projetoseletivo.health;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

/**
 * Health Check customizado para verificar conectividade com o MinIO.
 */
@Readiness
@ApplicationScoped
public class MinioHealthCheck implements HealthCheck {

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "minio.bucket.capas", defaultValue = "album-capas")
    String bucketName;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("MinIO");

        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());

            return builder
                    .up()
                    .withData("bucket", bucketName)
                    .withData("bucketExists", exists)
                    .build();
        } catch (Exception e) {
            return builder
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
