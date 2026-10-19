package dev.shiori.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.enums.StatusDenuncia;
import dev.shiori.domain.model.DenunciaObra;

public interface DenunciaObraRepository extends JpaRepository<DenunciaObra, Long> {

    @EntityGraph(attributePaths = {"obra", "usuarioDenunciante"})
    Page<DenunciaObra> findByStatus(StatusDenuncia status, Pageable pageable);

    long countByStatus(StatusDenuncia status);
}
