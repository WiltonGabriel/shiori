package dev.shiori.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.model.Comentario;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.Usuario;

@SpringBootTest
@Transactional
class ComentarioRepositoryTest {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObraRepository obraRepository;

    private Usuario autor;
    private Obra obra;

    @BeforeEach
    void setup() {
        autor = usuarioRepository.save(new Usuario("comentarista@shiori.dev", "senha123"));
        obra = obraRepository.save(new Obra("Pluto", null, null, "Ficção", 65, TipoObra.MANGA));
    }

    @Test
    @DisplayName("Deve salvar comentário e paginar comentários ativos da obra")
    void deveSalvarEPaginarComentariosAtivos() {
        Comentario c1 = new Comentario(autor, obra, null, "História fantástica!", false);
        Comentario c2 = new Comentario(autor, obra, null, "Final emocionante", true);
        comentarioRepository.save(c1);
        comentarioRepository.save(c2);

        Page<Comentario> comentarios = comentarioRepository.findByObraIdAndAtivoTrueOrderByDataCriacaoDesc(
                obra.getId(),
                PageRequest.of(0, 10));

        assertThat(comentarios.getContent()).hasSize(2);
        assertThat(comentarioRepository.countByObraIdAndAtivoTrue(obra.getId())).isEqualTo(2);
    }
}
