package dev.shiori.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.model.RegistroAuditoria;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {

    Page<RegistroAuditoria> findAllByOrderByDataHoraDesc(Pageable pageable);

    Page<RegistroAuditoria> findByAdminIdOrderByDataHoraDesc(Long adminId, Pageable pageable);

    Page<RegistroAuditoria> findByEntidadeAfetadaOrderByDataHoraDesc(String entidadeAfetada, Pageable pageable);
}
