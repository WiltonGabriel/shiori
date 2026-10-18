package dev.shiori.repository;

import dev.shiori.domain.model.Obra;

import org.springframework.data.jpa.repository.JpaRepository;
import dev.shiori.domain.enums.StatusLeitura;
import java.util.List;

public interface ObraRepository extends JpaRepository<Obra, Long> {
    List<Obra> findByStatusLeitura(StatusLeitura statusLeitura);
}