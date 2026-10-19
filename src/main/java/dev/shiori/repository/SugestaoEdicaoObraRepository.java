package dev.shiori.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.enums.StatusSugestao;
import dev.shiori.domain.model.SugestaoEdicaoObra;

public interface SugestaoEdicaoObraRepository extends JpaRepository<SugestaoEdicaoObra, Long> {

    @EntityGraph(attributePaths = {"obra", "usuarioSugeridor", "adminAvaliador"})
    List<SugestaoEdicaoObra> findByObraIdAndStatus(Long obraId, StatusSugestao status);

    @EntityGraph(attributePaths = {"obra", "usuarioSugeridor", "adminAvaliador"})
    Page<SugestaoEdicaoObra> findByStatus(StatusSugestao status, Pageable pageable);

    @EntityGraph(attributePaths = {"obra", "usuarioSugeridor", "adminAvaliador"})
    Page<SugestaoEdicaoObra> findByUsuarioSugeridorId(Long usuarioSugeridorId, Pageable pageable);
}
