package dev.shiori.infra.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SensitiveDataCryptoConverterTest {

    private SensitiveDataCryptoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new SensitiveDataCryptoConverter("ChaveDeTesteSeguraParaSensitiveData123!");
    }

    @Test
    @DisplayName("Deve encriptar e decriptar dado sensível com AES-256-GCM com sucesso")
    void deveEncriptarEDecriptarDadoSensivel() {
        String urlOriginal = "https://scanprivada.dev/capitulos/berserk/150";

        String criptografado = converter.convertToDatabaseColumn(urlOriginal);
        assertThat(criptografado).isNotNull();
        assertThat(criptografado).isNotEqualTo(urlOriginal);

        String decriptado = converter.convertToEntityAttribute(criptografado);
        assertThat(decriptado).isEqualTo(urlOriginal);
    }

    @Test
    @DisplayName("Deve gerar cifras diferentes para o mesmo texto (IV aleatório por operação)")
    void deveGerarCifrasDiferentesComIvAleatorio() {
        String texto = "https://scan.dev/cap1";

        String cifra1 = converter.convertToDatabaseColumn(texto);
        String cifra2 = converter.convertToDatabaseColumn(texto);

        assertThat(cifra1).isNotEqualTo(cifra2);
        assertThat(converter.convertToEntityAttribute(cifra1)).isEqualTo(texto);
        assertThat(converter.convertToEntityAttribute(cifra2)).isEqualTo(texto);
    }

    @Test
    @DisplayName("Deve lidar graciosamente com valores nulos ou vazios")
    void deveLidarComValoresNulosOuVazios() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToDatabaseColumn("")).isEqualTo("");
        assertThat(converter.convertToEntityAttribute(null)).isNull();
        assertThat(converter.convertToEntityAttribute("")).isEqualTo("");
    }
}
