package dev.shiori.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.shiori.domain.enums.StatusConexao;
import dev.shiori.domain.model.ConexaoUsuario;

public interface ConexaoUsuarioRepository extends JpaRepository<ConexaoUsuario, Long> {

    Optional<ConexaoUsuario> findBySolicitanteIdAndRecebedorId(Long solicitanteId, Long recebedorId);

    @Query("SELECT c FROM ConexaoUsuario c WHERE (c.solicitante.id = :u1 AND c.recebedor.id = :u2) OR (c.solicitante.id = :u2 AND c.recebedor.id = :u1)")
    Optional<ConexaoUsuario> findConexaoEntre(@Param("u1") Long usuarioId1, @Param("u2") Long usuarioId2);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ConexaoUsuario c WHERE ((c.solicitante.id = :u1 AND c.recebedor.id = :u2) OR (c.solicitante.id = :u2 AND c.recebedor.id = :u1)) AND c.status = dev.shiori.domain.enums.StatusConexao.ACEITA")
    boolean saoConexoes(@Param("u1") Long usuarioId1, @Param("u2") Long usuarioId2);

    @EntityGraph(attributePaths = {"solicitante", "recebedor"})
    @Query("SELECT c FROM ConexaoUsuario c WHERE (c.solicitante.id = :usuarioId OR c.recebedor.id = :usuarioId) AND c.status = dev.shiori.domain.enums.StatusConexao.ACEITA")
    List<ConexaoUsuario> findConexoesAceitas(@Param("usuarioId") Long usuarioId);

    @EntityGraph(attributePaths = {"solicitante"})
    Page<ConexaoUsuario> findByRecebedorIdAndStatus(Long recebedorId, StatusConexao status, Pageable pageable);

    @EntityGraph(attributePaths = {"recebedor"})
    Page<ConexaoUsuario> findBySolicitanteIdAndStatus(Long solicitanteId, StatusConexao status, Pageable pageable);

    @EntityGraph(attributePaths = {"solicitante"})
    List<ConexaoUsuario> findByRecebedorIdAndStatus(Long recebedorId, StatusConexao status);

    @EntityGraph(attributePaths = {"recebedor"})
    List<ConexaoUsuario> findBySolicitanteIdAndStatus(Long solicitanteId, StatusConexao status);
}
