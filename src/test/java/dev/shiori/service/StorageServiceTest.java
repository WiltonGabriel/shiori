package dev.shiori.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import dev.shiori.domain.exception.ArquivoInvalidoException;
import dev.shiori.domain.exception.MidiaNaoSuportadaException;
import dev.shiori.domain.exception.StorageException;
import dev.shiori.infra.config.StorageProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: S3StorageService")
class StorageServiceTest {

    @Mock
    private S3Client s3Client;

    private StorageProperties properties;
    private S3StorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        properties = new StorageProperties();
        properties.setEndpoint("http://localhost:9000");
        properties.setAccessKey("mock-access-key");
        properties.setSecretKey("mock-secret-key");
        properties.setRegion("us-east-1");
        properties.setBucketName("shiori-media");

        storageService = new S3StorageServiceImpl(s3Client, properties);
    }

    @Test
    @DisplayName("Deve fazer upload com sucesso para imagem PNG")
    void deveFazerUploadComSucessoParaImagemPng() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "conteudo-png-fake".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String url = storageService.uploadFile(file, "avatares");

        assertThat(url).isNotNull();
        assertThat(url).startsWith("http://localhost:9000/shiori-media/avatares/");
        assertThat(url).endsWith(".png");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        PutObjectRequest request = captor.getValue();
        assertThat(request.bucket()).isEqualTo("shiori-media");
        assertThat(request.key()).startsWith("avatares/");
        assertThat(request.contentType()).isEqualTo("image/png");
    }

    @Test
    @DisplayName("Deve fazer upload com sucesso para imagem JPEG")
    void deveFazerUploadComSucessoParaImagemJpeg() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.jpeg",
                "image/jpeg",
                "conteudo-jpeg-fake".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String url = storageService.uploadFile(file, "avatares");

        assertThat(url).isNotNull();
        assertThat(url).startsWith("http://localhost:9000/shiori-media/avatares/");
        assertThat(url).endsWith(".jpeg");
    }

    @Test
    @DisplayName("Deve fazer upload com sucesso para imagem WEBP")
    void deveFazerUploadComSucessoParaImagemWebp() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "capa.webp",
                "image/webp",
                "conteudo-webp-fake".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String url = storageService.uploadFile(file, "capas");

        assertThat(url).isNotNull();
        assertThat(url).startsWith("http://localhost:9000/shiori-media/capas/");
        assertThat(url).endsWith(".webp");
    }

    @Test
    @DisplayName("Deve lançar ArquivoInvalidoException se arquivo for nulo ou vazio")
    void deveLancarArquivoInvalidoExceptionSeArquivoNuloOuVazio() {
        assertThatThrownBy(() -> storageService.uploadFile(null, "avatares"))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessageContaining("não pode ser nulo ou vazio");

        MockMultipartFile emptyFile = new MockMultipartFile("file", "teste.png", "image/png", new byte[0]);
        assertThatThrownBy(() -> storageService.uploadFile(emptyFile, "avatares"))
                .isInstanceOf(ArquivoInvalidoException.class)
                .hasMessageContaining("não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar MidiaNaoSuportadaException se extensão não for permitida")
    void deveLancarMidiaNaoSuportadaExceptionSeExtensaoNaoPermitida() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "conteudo-pdf".getBytes()
        );

        assertThatThrownBy(() -> storageService.uploadFile(pdfFile, "avatares"))
                .isInstanceOf(MidiaNaoSuportadaException.class)
                .hasMessageContaining("Extensão de imagem 'pdf' não suportada");
    }

    @Test
    @DisplayName("Deve lançar MidiaNaoSuportadaException se tipo MIME for incompatível")
    void deveLancarMidiaNaoSuportadaExceptionSeMimeTypeIncompativel() {
        MockMultipartFile textFileComExtensaoPng = new MockMultipartFile(
                "file",
                "falso.png",
                "text/plain",
                "conteudo-texto".getBytes()
        );

        assertThatThrownBy(() -> storageService.uploadFile(textFileComExtensaoPng, "avatares"))
                .isInstanceOf(MidiaNaoSuportadaException.class)
                .hasMessageContaining("Tipo de mídia 'text/plain' não suportado");
    }

    @Test
    @DisplayName("Deve lançar StorageException em caso de falha no cliente S3")
    void deveLancarStorageExceptionEmCasoDeFalhaS3() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "dados".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("Erro interno no storage").build());

        assertThatThrownBy(() -> storageService.uploadFile(file, "avatares"))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Falha ao realizar upload");
    }

    @Test
    @DisplayName("Deve sanitizar barras no nome da pasta corretamente")
    void deveSanitizarBarrasNoNomeDaPasta() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "dados".getBytes()
        );

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String url = storageService.uploadFile(file, "/avatares///");

        assertThat(url).contains("/shiori-media/avatares/");
        assertThat(url).doesNotContain("//avatares");
        assertThat(url).doesNotContain("avatares///");
    }
}
