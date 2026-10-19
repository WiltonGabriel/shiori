package dev.shiori.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.domain.enums.Visibilidade;
import dev.shiori.domain.model.ItemCatalogo;

public interface ItemCatalogoRepository extends JpaRepository<ItemCatalogo, Long> {

    Optional<ItemCatalogo> findByUsuarioIdAndObraId(Long usuarioId, Long obraId);

    boolean existsByUsuarioIdAndObraId(Long usuarioId, Long obraId);

    @EntityGraph(attributePaths = {"obra", "historicosLeitura"})
    Optional<ItemCatalogo> findComDetalhesById(Long id);

    @EntityGraph(attributePaths = {"obra"})
    Page<ItemCatalogo> findByUsuarioId(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"obra"})
    Page<ItemCatalogo> findByUsuarioIdAndObraTipoObraIn(Long usuarioId, Collection<dev.shiori.domain.enums.TipoObra> tipos, Pageable pageable);

    @EntityGraph(attributePaths = {"obra"})
    Page<ItemCatalogo> findByUsuarioIdAndStatusLeitura(Long usuarioId, StatusLeitura statusLeitura, Pageable pageable);

    @EntityGraph(attributePaths = {"obra"})
    Page<ItemCatalogo> findByUsuarioIdAndVisibilidade(Long usuarioId, Visibilidade visibilidade, Pageable pageable);

    @EntityGraph(attributePaths = {"obra"})
    Page<ItemCatalogo> findByUsuarioIdAndVisibilidadeIn(Long usuarioId, Collection<Visibilidade> visibilidades, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "historicosLeitura"})
    List<ItemCatalogo> findByObraId(Long obraId);

    long countByUsuarioId(Long usuarioId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ItemCatalogo i WHERE i.usuario.id = :usuarioId")
    void deletarPorUsuarioId(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);
}
