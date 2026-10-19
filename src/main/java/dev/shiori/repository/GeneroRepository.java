package dev.shiori.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.model.Genero;

public interface GeneroRepository extends JpaRepository<Genero, Long> {

    Optional<Genero> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}
