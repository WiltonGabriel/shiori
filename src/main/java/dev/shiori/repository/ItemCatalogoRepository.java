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
    @org.springframework.data.jpa.repository.Query("SELECT i FROM ItemCatalogo i WHERE i.usuario.id = :usuarioId AND (i.tipoObra IN :tipos OR (i.tipoObra IS NULL AND i.obra.tipoObra IN :tipos))")
    Page<ItemCatalogo> findByUsuarioIdAndObraTipoObraIn(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId, @org.springframework.data.repository.query.Param("tipos") Collection<dev.shiori.domain.enums.TipoObra> tipos, Pageable pageable);

    @EntityGraph(attributePaths = {"obra"})
    Page<ItemCatalogo> findByUsuarioIdAndStatusLeitura(Long usuarioId, StatusLeitura statusLeitura, Pageable pageable);

    @EntityGraph(attributePaths = {"obra"})
    Page<ItemCatalogo> findByUsuarioIdAndVisibilidade(Long usuarioId, Visibilidade visibilidade, Pageable pageable);

    @EntityGraph(attributePaths = {"obra"})
    Page<ItemCatalogo> findByUsuarioIdAndVisibilidadeIn(Long usuarioId, Collection<Visibilidade> visibilidades, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "historicosLeitura"})
    List<ItemCatalogo> findByObraId(Long obraId);

    List<ItemCatalogo> findByUsuarioId(Long usuarioId);

    long countByUsuarioId(Long usuarioId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE ItemCatalogo i SET i.obra = null, i.sincronizarMetadados = false WHERE i.obra.id = :obraId")
    void desvincularObra(@org.springframework.data.repository.query.Param("obraId") Long obraId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ItemCatalogo i WHERE i.usuario.id = :usuarioId")
    void deletarPorUsuarioId(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);
}
