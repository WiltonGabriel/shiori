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

    long countByStatus(StatusSugestao status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE SugestaoEdicaoObra s SET s.usuarioSugeridor = null WHERE s.usuarioSugeridor.id = :usuarioId")
    void desvincularUsuarioSugeridor(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE SugestaoEdicaoObra s SET s.obra.id = :obraPrincipalId WHERE s.obra.id = :obraDuplicadaId")
    int transferirObra(@org.springframework.data.repository.query.Param("obraDuplicadaId") Long obraDuplicadaId, @org.springframework.data.repository.query.Param("obraPrincipalId") Long obraPrincipalId);
}
