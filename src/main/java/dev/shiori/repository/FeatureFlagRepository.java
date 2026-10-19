package dev.shiori.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.model.FeatureFlag;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {

    Optional<FeatureFlag> findByChaveIgnoreCase(String chave);

    boolean existsByChaveIgnoreCase(String chave);
}
