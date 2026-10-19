package dev.shiori.service;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.shiori.domain.exception.ArquivoInvalidoException;
import dev.shiori.domain.exception.MidiaNaoSuportadaException;
import dev.shiori.domain.exception.StorageException;
import dev.shiori.infra.config.StorageProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class S3StorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageServiceImpl.class);

    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("jpeg", "jpg", "png", "webp");
    private static final Set<String> TIPOS_MIME_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final S3Client s3Client;
    private final StorageProperties properties;

    public S3StorageServiceImpl(S3Client s3Client, StorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new ArquivoInvalidoException("O arquivo de upload não pode ser nulo ou vazio");
        }

        String nomeOriginal = file.getOriginalFilename();
        if (nomeOriginal == null || !nomeOriginal.contains(".")) {
            throw new MidiaNaoSuportadaException("O arquivo deve possuir uma extensão válida (.jpeg, .png, .webp)");
        }

        String extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf(".") + 1).toLowerCase();
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new MidiaNaoSuportadaException("Extensão de imagem '" + extensao + "' não suportada. Aceitas apenas: jpeg, png, webp");
        }

        String contentType = file.getContentType();
        if (contentType != null && !TIPOS_MIME_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new MidiaNaoSuportadaException("Tipo de mídia '" + contentType + "' não suportado. Aceitos apenas: image/jpeg, image/png, image/webp");
        }

        String pastaSanitizada = (folder == null || folder.isBlank())
                ? ""
                : (folder.trim().replaceAll("^/+|/+$", "") + "/");

        String nomeArquivoUnico = pastaSanitizada + UUID.randomUUID() + "." + extensao;
        String resolvedContentType = (contentType != null && !contentType.isBlank())
                ? contentType
                : ("image/" + (extensao.equals("jpg") ? "jpeg" : extensao));

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(nomeArquivoUnico)
                    .contentType(resolvedContentType)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Upload S3 concluído com sucesso: bucket={}, key={}", properties.getBucketName(), nomeArquivoUnico);
        } catch (IOException | S3Exception ex) {
            log.error("Falha ao enviar arquivo para o bucket S3: bucket={}, key={}", properties.getBucketName(), nomeArquivoUnico, ex);
            throw new StorageException("Falha ao realizar upload do arquivo para o armazenamento: " + ex.getMessage(), ex);
        }

        String endpointBase = properties.getEndpoint().replaceAll("/+$", "");
        return String.format("%s/%s/%s", endpointBase, properties.getBucketName(), nomeArquivoUnico);
    }
}
