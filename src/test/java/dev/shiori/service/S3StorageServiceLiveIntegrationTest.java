package dev.shiori.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.net.URI;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.mock.web.MockMultipartFile;

import dev.shiori.infra.config.StorageProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Tag("live")
@EnabledIfEnvironmentVariable(named = "RUN_LIVE_S3_TESTS", matches = "true")
@DisplayName("Teste de Integração Real: MinIO / S3")
class S3StorageServiceLiveIntegrationTest {

    @Test
    @DisplayName("Deve fazer upload de arquivo real para o MinIO / S3 e validar URL pública")
    void deveFazerUploadRealParaMinio() throws Exception {
        String endpoint = System.getenv().getOrDefault("S3_ENDPOINT", "http://localhost:9000");
        String accessKey = System.getenv().getOrDefault("S3_ACCESS_KEY", "sua_access_key");
        String secretKey = System.getenv().getOrDefault("S3_SECRET_KEY", "sua_secret_key");
        String region = System.getenv().getOrDefault("S3_REGION", "us-east-1");
        String bucket = System.getenv().getOrDefault("S3_BUCKET_NAME", "shiori-media");

        StorageProperties properties = new StorageProperties();
        properties.setEndpoint(endpoint);
        properties.setAccessKey(accessKey);
        properties.setSecretKey(secretKey);
        properties.setRegion(region);
        properties.setBucketName(bucket);

        S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
                .region(Region.of(properties.getRegion()))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();

        S3StorageServiceImpl storageService = new S3StorageServiceImpl(s3Client, properties);

        byte[] fakePngBytes = new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0 };
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "smoke-test-avatar.png",
                "image/png",
                fakePngBytes
        );

        String publicUrl = storageService.uploadFile(file, "smoke-tests");

        assertThat(publicUrl).startsWith(endpoint + "/" + bucket + "/smoke-tests/");
        assertThat(publicUrl).endsWith(".png");

        // Valida que o arquivo é publicamente acessível via HTTP GET
        try (InputStream in = URI.create(publicUrl).toURL().openStream()) {
            byte[] downloaded = in.readAllBytes();
            assertThat(downloaded).isEqualTo(fakePngBytes);
        }

        // Limpeza do objeto de teste para manter o bucket limpo
        String key = publicUrl.substring(publicUrl.indexOf("smoke-tests/"));
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(key)
                .build());
    }
}
