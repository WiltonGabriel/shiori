package dev.shiori.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.shiori.domain.model.HistoricoLeitura;

public interface HistoricoLeituraRepository extends JpaRepository<HistoricoLeitura, Long> {

    @EntityGraph(attributePaths = {"itemCatalogo", "itemCatalogo.obra"})
    List<HistoricoLeitura> findByItemCatalogoId(Long itemCatalogoId);

    @EntityGraph(attributePaths = {"itemCatalogo", "itemCatalogo.obra"})
    Page<HistoricoLeitura> findByItemCatalogoId(Long itemCatalogoId, Pageable pageable);

    @EntityGraph(attributePaths = {"itemCatalogo", "itemCatalogo.obra"})
    @Query("SELECT h FROM HistoricoLeitura h WHERE h.itemCatalogo.usuario.id = :usuarioId ORDER BY h.dataLeitura DESC")
    Page<HistoricoLeitura> findByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT h FROM HistoricoLeitura h JOIN h.itemCatalogo i WHERE i.usuario.id = :usuarioId AND YEAR(h.dataLeitura) = :ano ORDER BY h.dataLeitura ASC")
    List<HistoricoLeitura> findByUsuarioIdEAno(@Param("usuarioId") Long usuarioId, @Param("ano") int ano);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM HistoricoLeitura h WHERE h.itemCatalogo.id IN (SELECT i.id FROM ItemCatalogo i WHERE i.usuario.id = :usuarioId)")
    void deletarPorUsuarioId(@Param("usuarioId") Long usuarioId);
}
