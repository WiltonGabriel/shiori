package dev.shiori.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.model.NoFederado;

public interface NoFederadoRepository extends JpaRepository<NoFederado, Long> {

    List<NoFederado> findByAtivoTrue();

    Optional<NoFederado> findByUrlHub(String urlHub);

    Optional<NoFederado> findBySegredoCompartilhado(String segredoCompartilhado);
}
