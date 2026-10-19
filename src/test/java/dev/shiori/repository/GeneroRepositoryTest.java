package dev.shiori.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.model.Genero;

@SpringBootTest
@Transactional
class GeneroRepositoryTest {

    @Autowired
    private GeneroRepository generoRepository;

    @Test
    @DisplayName("Deve salvar gênero e buscar por nome ignorando maiúsculas e minúsculas")
    void deveSalvarEBuscarPorNomeIgnoreCase() {
        Genero genero = new Genero("Aventura", "Gênero de aventura");
        generoRepository.save(genero);

        Optional<Genero> encontrado = generoRepository.findByNomeIgnoreCase("aventura");
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Aventura");
        assertThat(encontrado.get().getSlug()).isEqualTo("aventura");

        assertThat(generoRepository.existsByNomeIgnoreCase("AVENTURA")).isTrue();
        assertThat(generoRepository.existsByNomeIgnoreCase("Inexistente")).isFalse();
    }

    @Test
    @DisplayName("Deve permitir persistência de gênero vindo de integração externa")
    void deveSalvarGeneroDeIntegracaoExterna() {
        Genero genero = Genero.criarDeIntegracaoExterna("Slice of Life");
        generoRepository.save(genero);

        Optional<Genero> encontrado = generoRepository.findByNomeIgnoreCase("slice of life");
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getSlug()).isEqualTo("slice-of-life");
    }
}
