package dev.shiori.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.shiori.domain.enums.StatusCatalogo;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.model.Obra;

public interface ObraRepository extends JpaRepository<Obra, Long> {

    @Override
    @EntityGraph(attributePaths = {"generos", "tags"})
    List<Obra> findAll();

    @Override
    @EntityGraph(attributePaths = {"generos", "tags"})
    Optional<Obra> findById(Long id);

    @EntityGraph(attributePaths = {"generos", "tags"})
    List<Obra> findByStatusCatalogo(StatusCatalogo statusCatalogo);

    @EntityGraph(attributePaths = {"generos", "tags"})
    List<Obra> findByStatusCatalogoIn(Collection<StatusCatalogo> statusCatalogos);

    @EntityGraph(attributePaths = {"generos", "tags"})
    List<Obra> findByTipoObra(TipoObra tipoObra);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Obra o SET o.criadoPorUsuarioId = null WHERE o.criadoPorUsuarioId = :usuarioId")
    void desvincularCriador(@org.springframework.data.repository.query.Param("usuarioId") Long usuarioId);
}