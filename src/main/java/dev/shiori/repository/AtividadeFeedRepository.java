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
}
