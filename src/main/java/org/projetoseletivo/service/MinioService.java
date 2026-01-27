package org.projetoseletivo.service;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;


@ApplicationScoped
public class MinioService {

    private static final Logger LOG = Logger.getLogger(MinioService.class);

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "minio.bucket.capas", defaultValue = "album-capas")
    String bucketName;

    @ConfigProperty(name = "minio.presigned.url.expiration.minutes", defaultValue = "30")
    int urlExpirationMinutes;

    @ConfigProperty(name = "quarkus.minio.external-host", defaultValue = "")
    String externalHost;

    @ConfigProperty(name = "quarkus.minio.access-key", defaultValue = "minioadmin")
    String accessKey;

    @ConfigProperty(name = "quarkus.minio.secret-key", defaultValue = "minioadmin")
    String secretKey;

    // Client para gerar URLs com external-host
    private MinioClient urlClient;

    /**
     * Inicializa o bucket e o client para URLs.
     */
    @PostConstruct
    void init() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
                LOG.infov("Bucket '{0}' criado com sucesso", bucketName);
            }
        } catch (Exception e) {
            LOG.warnv("Não foi possível verificar/criar bucket: {0}", e.getMessage());
        }

        // Criar client para URLs usando external-host se configurado
        if (externalHost != null && !externalHost.isBlank()) {
            urlClient = MinioClient.builder()
                    .endpoint(externalHost)
                    .credentials(accessKey, secretKey)
                    .region("us-east-1") // Region fixa para evitar chamada HTTP
                    .build();
            LOG.infov("URL Client configurado com external-host: {0}", externalHost);
        } else {
            urlClient = minioClient;
        }
    }

    /**
     * Faz upload de um arquivo para o MinIO.
     */
    public String upload(String objectKey, InputStream inputStream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build());

            LOG.infov("Arquivo '{0}' enviado para bucket '{1}'", objectKey, bucketName);
            return objectKey;
        } catch (Exception e) {
            LOG.errorv(e, "Erro ao fazer upload do arquivo: {0}", objectKey);
            throw new RuntimeException("Erro ao fazer upload: " + e.getMessage(), e);
        }
    }

    /**
     * Gera URL pré-assinada para acesso ao arquivo.
     * Usa o urlClient configurado com external-host para gerar URLs válidas
     * externamente.
     */
    public String gerarUrlPreAssinada(String bucket, String objectKey) {
        String bucketToUse = bucket != null ? bucket : bucketName;

        try {
            return urlClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketToUse)
                            .object(objectKey)
                            .expiry(urlExpirationMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            LOG.errorv(e, "Erro ao gerar URL pré-assinada: {0}", objectKey);
            return null;
        }
    }

    /**
     * Remove um arquivo do MinIO.
     */
    public void remover(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build());
            LOG.infov("Arquivo '{0}' removido do bucket '{1}'", objectKey, bucketName);
        } catch (Exception e) {
            LOG.errorv(e, "Erro ao remover arquivo: {0}", objectKey);
        }
    }

    /**
     * Retorna o nome do bucket padrão.
     */
    public String getBucketName() {
        return bucketName;
    }
}
