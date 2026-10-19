package dev.shiori.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.model.ConsentimentoUsuario;

public interface ConsentimentoUsuarioRepository extends JpaRepository<ConsentimentoUsuario, Long> {

    List<ConsentimentoUsuario> findByUsuarioIdOrderByDataConsentimentoDesc(Long usuarioId);

    Page<ConsentimentoUsuario> findByUsuarioIdOrderByDataConsentimentoDesc(Long usuarioId, Pageable pageable);

    Optional<ConsentimentoUsuario> findByUsuarioIdAndAtivoTrue(Long usuarioId);

    Optional<ConsentimentoUsuario> findFirstByUsuarioIdOrderByDataConsentimentoDesc(Long usuarioId);
}
