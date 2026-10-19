package dev.shiori.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import dev.shiori.domain.enums.StatusConexao;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.domain.model.ConexaoUsuario;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.conexao.ConexaoResponse;
import dev.shiori.repository.ConexaoUsuarioRepository;
import dev.shiori.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: ConexaoUsuarioService")
class ConexaoUsuarioServiceTest {

    @Mock
    private ConexaoUsuarioRepository conexaoUsuarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ConexaoUsuarioService conexaoUsuarioService;

    private Usuario solicitante;
    private Usuario destinatario;

    @BeforeEach
    void setUp() {
        solicitante = new Usuario("solicitante@shiori.dev", "senha", "solicitante");
        ReflectionTestUtils.setField(solicitante, "id", 1L);

        destinatario = new Usuario("destinatario@shiori.dev", "senha", "destinatario");
        ReflectionTestUtils.setField(destinatario, "id", 2L);
    }

    @Test
    @DisplayName("Deve solicitar conexão com sucesso gerando status PENDENTE")
    void deveSolicitarConexaoComSucesso() {
        when(conexaoUsuarioRepository.findConexaoEntre(1L, 2L)).thenReturn(Optional.empty());
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(solicitante));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(destinatario));

        when(conexaoUsuarioRepository.save(any(ConexaoUsuario.class))).thenAnswer(invocation -> {
            ConexaoUsuario c = invocation.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 10L);
            return c;
        });

        ConexaoResponse response = conexaoUsuarioService.solicitarConexao(1L, 2L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.solicitanteId()).isEqualTo(1L);
        assertThat(response.recebedorId()).isEqualTo(2L);
        assertThat(response.status()).isEqualTo(StatusConexao.PENDENTE);
    }

    @Test
    @DisplayName("Não deve permitir solicitar conexão consigo mesmo")
    void naoDevePermitirConexaoConsigoMesmo() {
        assertThatThrownBy(() -> conexaoUsuarioService.solicitarConexao(1L, 1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Não é possível solicitar conexão consigo mesmo");
    }

    @Test
    @DisplayName("Não deve permitir solicitar conexão se já existe status ACEITA")
    void naoDevePermitirSeJaEstaConectado() {
        ConexaoUsuario existente = new ConexaoUsuario(solicitante, destinatario);
        existente.aceitar();

        when(conexaoUsuarioRepository.findConexaoEntre(1L, 2L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> conexaoUsuarioService.solicitarConexao(1L, 2L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Você já está conectado com este usuário");
    }

    @Test
    @DisplayName("Não deve permitir solicitar conexão se já existe status PENDENTE")
    void naoDevePermitirSeJaExistePendente() {
        ConexaoUsuario existente = new ConexaoUsuario(solicitante, destinatario);

        when(conexaoUsuarioRepository.findConexaoEntre(1L, 2L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> conexaoUsuarioService.solicitarConexao(1L, 2L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Já existe uma solicitação de conexão pendente");
    }

    @Test
    @DisplayName("Não deve permitir solicitar conexão se está BLOQUEADA")
    void naoDevePermitirSeEstaBloqueada() {
        ConexaoUsuario existente = new ConexaoUsuario(solicitante, destinatario);
        existente.bloquear();

        when(conexaoUsuarioRepository.findConexaoEntre(1L, 2L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> conexaoUsuarioService.solicitarConexao(1L, 2L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Não é possível solicitar conexão com este usuário");
    }

    @Test
    @DisplayName("Destinatário deve aceitar conexão com sucesso mudando para ACEITA")
    void destinatarioDeveAceitarConexaoComSucesso() {
        ConexaoUsuario conexao = new ConexaoUsuario(solicitante, destinatario);
        ReflectionTestUtils.setField(conexao, "id", 10L);

        when(conexaoUsuarioRepository.findById(10L)).thenReturn(Optional.of(conexao));
        when(conexaoUsuarioRepository.save(conexao)).thenReturn(conexao);

        ConexaoResponse response = conexaoUsuarioService.aceitarConexao(2L, 10L);

        assertThat(response.status()).isEqualTo(StatusConexao.ACEITA);
        assertThat(conexao.isAceita()).isTrue();
    }

    @Test
    @DisplayName("Solicitante NÃO pode aceitar a própria solicitação de conexão")
    void solicitanteNaoPodeAceitarPropriaSolicitacao() {
        ConexaoUsuario conexao = new ConexaoUsuario(solicitante, destinatario);
        ReflectionTestUtils.setField(conexao, "id", 10L);

        when(conexaoUsuarioRepository.findById(10L)).thenReturn(Optional.of(conexao));

        assertThatThrownBy(() -> conexaoUsuarioService.aceitarConexao(1L, 10L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Apenas o destinatário da solicitação pode aceitar");
    }

    @Test
    @DisplayName("Usuário participante deve conseguir bloquear conexão com sucesso")
    void participanteDeveBloquearConexao() {
        ConexaoUsuario conexao = new ConexaoUsuario(solicitante, destinatario);
        ReflectionTestUtils.setField(conexao, "id", 10L);

        when(conexaoUsuarioRepository.findById(10L)).thenReturn(Optional.of(conexao));
        when(conexaoUsuarioRepository.save(conexao)).thenReturn(conexao);

        ConexaoResponse response = conexaoUsuarioService.bloquearConexao(1L, 10L);

        assertThat(response.status()).isEqualTo(StatusConexao.BLOQUEADA);
        assertThat(conexao.isBloqueada()).isTrue();
    }

    @Test
    @DisplayName("Usuário terceiro estranho NÃO pode bloquear conexão alheia")
    void usuarioTerceiroNaoPodeBloquearConexaoAlheia() {
        ConexaoUsuario conexao = new ConexaoUsuario(solicitante, destinatario);
        ReflectionTestUtils.setField(conexao, "id", 10L);

        when(conexaoUsuarioRepository.findById(10L)).thenReturn(Optional.of(conexao));

        assertThatThrownBy(() -> conexaoUsuarioService.bloquearConexao(999L, 10L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Você não tem permissão para alterar esta conexão");
    }

    @Test
    @DisplayName("Deve listar solicitações pendentes do usuário recebedor")
    void deveListarSolicitacoesPendentes() {
        ConexaoUsuario conexao = new ConexaoUsuario(solicitante, destinatario);
        ReflectionTestUtils.setField(conexao, "id", 10L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ConexaoUsuario> pagina = new PageImpl<>(List.of(conexao), pageable, 1);

        when(conexaoUsuarioRepository.findByRecebedorIdAndStatus(2L, StatusConexao.PENDENTE, pageable))
                .thenReturn(pagina);

        Page<ConexaoResponse> resultado = conexaoUsuarioService.listarSolicitacoesPendentes(2L, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).status()).isEqualTo(StatusConexao.PENDENTE);
    }

    @Test
    @DisplayName("Deve listar conexões aceitas do usuário")
    void deveListarConexoesAceitas() {
        ConexaoUsuario conexao = new ConexaoUsuario(solicitante, destinatario);
        conexao.aceitar();
        ReflectionTestUtils.setField(conexao, "id", 10L);

        when(conexaoUsuarioRepository.findConexoesAceitas(1L)).thenReturn(List.of(conexao));

        List<ConexaoResponse> resultado = conexaoUsuarioService.listarConexoesAceitas(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).status()).isEqualTo(StatusConexao.ACEITA);
    }
}
