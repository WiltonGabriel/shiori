package dev.shiori.repository;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.model.AtividadeFeed;

public interface AtividadeFeedRepository extends JpaRepository<AtividadeFeed, Long> {

    @EntityGraph(attributePaths = {"usuario", "obra", "itemCatalogo"})
    Page<AtividadeFeed> findByUsuarioIdInOrderByDataCriacaoDesc(Collection<Long> usuarioIds, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "obra", "itemCatalogo"})
    Page<AtividadeFeed> findByUsuarioIdOrderByDataCriacaoDesc(Long usuarioId, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM AtividadeFeed a WHERE a.usuario.id = :usuarioId")
    void deletarPorUsuarioId(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE AtividadeFeed a SET a.itemCatalogo = null WHERE a.itemCatalogo.id IN (SELECT i.id FROM ItemCatalogo i WHERE i.usuario.id = :usuarioId)")
    void desvincularItemCatalogoPorUsuarioId(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE AtividadeFeed a SET a.obra.id = :obraPrincipalId WHERE a.obra.id = :obraDuplicadaId")
    int transferirObra(@org.springframework.data.repository.query.Param("obraDuplicadaId") Long obraDuplicadaId, @org.springframework.data.repository.query.Param("obraPrincipalId") Long obraPrincipalId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE AtividadeFeed a SET a.obra = null WHERE a.obra.id = :obraId")
    void desvincularObra(@org.springframework.data.repository.query.Param("obraId") Long obraId);
}
