package dev.shiori.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.Role;
import dev.shiori.domain.enums.StatusSugestao;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.model.Obra;
import dev.shiori.domain.model.SugestaoEdicaoObra;
import dev.shiori.domain.model.Usuario;

@SpringBootTest
@Transactional
class SugestaoEdicaoObraRepositoryTest {

    @Autowired
    private SugestaoEdicaoObraRepository sugestaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObraRepository obraRepository;

    private Usuario leitor;
    private Usuario admin;
    private Obra obra;

    @BeforeEach
    void setup() {
        leitor = usuarioRepository.save(new Usuario("leitor.sugestao@shiori.dev", "senha123"));
        Usuario adminEntity = new Usuario("admin.sugestao@shiori.dev", "senha123");
        adminEntity.promoverPara(Role.ROLE_ADMIN);
        admin = usuarioRepository.save(adminEntity);
        obra = obraRepository.save(new Obra("Berserk", "ベルセルク", null, "Sinopse", 364, TipoObra.MANGA));
    }

    @Test
    @DisplayName("Deve salvar sugestão e recuperar por obra e status")
    void deveSalvarERecuperarPorObraEStatus() {
        SugestaoEdicaoObra sugestao = new SugestaoEdicaoObra(
                obra,
                leitor,
                "Berserk Definitive Edition",
                null,
                null,
                null,
                null,
                null,
                "Título oficial atualizado");
        sugestaoRepository.save(sugestao);

        List<SugestaoEdicaoObra> pendentes = sugestaoRepository.findByObraIdAndStatus(obra.getId(), StatusSugestao.PENDENTE);
        assertThat(pendentes).hasSize(1);
        assertThat(pendentes.get(0).getTituloPrincipalSugerido()).isEqualTo("Berserk Definitive Edition");
        assertThat(pendentes.get(0).getUsuarioSugeridor().getId()).isEqualTo(leitor.getId());

        sugestao.aprovar(admin, "Aprovado oficialmente");
        sugestaoRepository.save(sugestao);
        assertThat(sugestao.getAdminAvaliador().getId()).isEqualTo(admin.getId());
        assertThat(sugestao.getStatus()).isEqualTo(StatusSugestao.APROVADA);
    }

    @Test
    @DisplayName("Deve paginar sugestões por status e por usuário sugeridor")
    void devePaginarSugestoes() {
        SugestaoEdicaoObra sugestao = new SugestaoEdicaoObra(
                obra,
                leitor,
                null,
                null,
                "https://cdn.shiori.dev/capas/berserk.jpg",
                null,
                null,
                null,
                "Nova capa em alta resolução");
        sugestaoRepository.save(sugestao);

        Page<SugestaoEdicaoObra> porStatus = sugestaoRepository.findByStatus(StatusSugestao.PENDENTE, PageRequest.of(0, 10));
        assertThat(porStatus.getContent()).hasSize(1);

        Page<SugestaoEdicaoObra> porUsuario = sugestaoRepository.findByUsuarioSugeridorId(leitor.getId(), PageRequest.of(0, 10));
        assertThat(porUsuario.getContent()).hasSize(1);
    }
}
