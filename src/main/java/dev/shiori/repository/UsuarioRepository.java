package dev.shiori.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.enums.Role;
import dev.shiori.domain.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByApelido(String apelido);

    boolean existsByEmail(String email);

    boolean existsByApelido(String apelido);

    Optional<Usuario> findByEmailAndAtivoTrue(String email);

    Optional<Usuario> findByApelidoAndAtivoTrue(String apelido);

    Page<Usuario> findByRole(Role role, Pageable pageable);

    Page<Usuario> findByAtivoTrue(Pageable pageable);

    long countByAtivoTrue();

    @EntityGraph(attributePaths = {"consentimentos"})
    Optional<Usuario> findComConsentimentosById(Long id);
}
