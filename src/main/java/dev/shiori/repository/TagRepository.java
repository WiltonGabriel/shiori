package dev.shiori.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    List<Tag> findByNomeContainingIgnoreCase(String termo);

    Page<Tag> findByNomeContainingIgnoreCase(String termo, Pageable pageable);
}
