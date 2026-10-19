package dev.shiori.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.model.Comentario;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    @EntityGraph(attributePaths = {"autor"})
    Page<Comentario> findByObraIdAndAtivoTrueOrderByDataCriacaoDesc(Long obraId, Pageable pageable);

    @EntityGraph(attributePaths = {"autor"})
    Page<Comentario> findByHistoricoLeituraIdAndAtivoTrueOrderByDataCriacaoDesc(Long historicoLeituraId, Pageable pageable);

    @EntityGraph(attributePaths = {"autor", "obra", "historicoLeitura"})
    Optional<Comentario> findComDetalhesById(Long id);

    long countByObraIdAndAtivoTrue(Long obraId);

    long countByHistoricoLeituraIdAndAtivoTrue(Long historicoLeituraId);
}
