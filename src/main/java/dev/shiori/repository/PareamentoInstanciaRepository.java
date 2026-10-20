package dev.shiori.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.enums.StatusPareamento;
import dev.shiori.domain.model.PareamentoInstancia;

public interface PareamentoInstanciaRepository extends JpaRepository<PareamentoInstancia, Long> {

    Optional<PareamentoInstancia> findByInstanceUuid(String instanceUuid);

    boolean existsByInstanceUuid(String instanceUuid);

    Page<PareamentoInstancia> findByStatus(StatusPareamento status, Pageable pageable);
}
