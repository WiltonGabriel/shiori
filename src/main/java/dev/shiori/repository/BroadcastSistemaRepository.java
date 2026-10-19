package dev.shiori.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.model.BroadcastSistema;

public interface BroadcastSistemaRepository extends JpaRepository<BroadcastSistema, Long> {

    List<BroadcastSistema> findByAtivoTrueOrderByDataCriacaoDesc();

    Page<BroadcastSistema> findAllByOrderByDataCriacaoDesc(Pageable pageable);
}
