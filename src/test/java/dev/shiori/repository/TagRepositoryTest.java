package dev.shiori.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.model.Tag;

@SpringBootTest
@Transactional
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("Deve buscar tag por nome ignorando case e verificar existência")
    void deveBuscarPorNomeIgnoreCase() {
        tagRepository.save(new Tag("Cyberpunk", 1L));

        Optional<Tag> tag = tagRepository.findByNomeIgnoreCase("cyberpunk");
        assertThat(tag).isPresent();
        assertThat(tag.get().getNome()).isEqualTo("Cyberpunk");

        assertThat(tagRepository.existsByNomeIgnoreCase("CYBERPUNK")).isTrue();
        assertThat(tagRepository.existsByNomeIgnoreCase("Inexistente")).isFalse();
    }

    @Test
    @DisplayName("Deve buscar tags contendo termo")
    void deveBuscarPorTermo() {
        tagRepository.save(new Tag("Sci-Fi Clássico", 1L));
        tagRepository.save(new Tag("Hard Sci-Fi", 1L));

        List<Tag> tags = tagRepository.findByNomeContainingIgnoreCase("sci-fi");
        assertThat(tags).hasSize(2);
    }
}
