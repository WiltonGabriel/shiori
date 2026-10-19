package dev.shiori.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.model.ProvedorIntegracao;

public interface ProvedorIntegracaoRepository extends JpaRepository<ProvedorIntegracao, Long> {

    Optional<ProvedorIntegracao> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);
}
