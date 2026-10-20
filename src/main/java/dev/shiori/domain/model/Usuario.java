package dev.shiori.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import dev.shiori.domain.enums.Role;
import dev.shiori.domain.enums.TipoObra;
import dev.shiori.domain.exception.RegraDeNegocioException;
import dev.shiori.infra.crypto.EmailCryptoConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "tb_usuario")
public class Usuario extends EntidadeBase {

    private static final Logger log = LoggerFactory.getLogger(Usuario.class);

    @Convert(converter = EmailCryptoConverter.class)
    @Column(nullable = false, unique = true, length = 500)
    private String email;

    @Column(nullable = false, length = 255)
    private String senha;

    @Column(unique = true, length = 50)
    private String apelido;

    @Column(length = 500)
    private String fotoUrl;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 50)
    private String fusoHorario = "America/Sao_Paulo";

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "ultima_atualizacao", nullable = false)
    private LocalDateTime ultimaAtualizacao;

    @Column(nullable = false)
    private boolean onboardingConcluido = false;

    @Column(nullable = false)
    private boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role = Role.ROLE_USER;

    @Column(nullable = false)
    private Integer pontosConfianca = 0;

    @Column(nullable = false)
    private Integer strikes = 0;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCatalogo> estante = new ArrayList<>();

    @OneToMany(mappedBy = "solicitante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConexaoUsuario> conexoesEnviadas = new ArrayList<>();

    @OneToMany(mappedBy = "recebedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConexaoUsuario> conexoesRecebidas = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsentimentoUsuario> consentimentos = new ArrayList<>();

    @ElementCollection(targetClass = TipoObra.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "tb_usuario_formatos_preferidos",
            joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_obra", nullable = false)
    private Set<TipoObra> formatosPreferidos = new HashSet<>(TipoObra.FORMATOS_OFICIAIS);

    protected Usuario() {
    }

    public Usuario(String email, String senha) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new RegraDeNegocioException("Um e-mail válido é obrigatório");
        }
        if (senha == null || senha.isBlank()) {
            throw new RegraDeNegocioException("A senha é obrigatória");
        }

        this.email = email.trim().toLowerCase();
        this.senha = senha;
        this.fusoHorario = "America/Sao_Paulo";
        this.dataCriacao = LocalDateTime.now();
        this.ultimaAtualizacao = LocalDateTime.now();
        this.onboardingConcluido = false;
        this.ativo = true;
        this.role = Role.ROLE_USER;
        this.pontosConfianca = 0;
        this.formatosPreferidos = new HashSet<>(TipoObra.FORMATOS_OFICIAIS);
    }

    public Usuario(String email, String senha, String apelido) {
        this(email, senha);
        if (apelido != null && !apelido.isBlank()) {
            this.apelido = apelido.trim().toLowerCase();
            this.onboardingConcluido = true;
        }
    }

    public void concluirOnboarding(String apelido, String fusoHorario, String fotoUrl, String descricao, Set<TipoObra> formatos) {
        if (apelido == null || apelido.isBlank()) {
            throw new RegraDeNegocioException("O apelido é obrigatório para concluir o onboarding");
        }
        if (formatos == null || formatos.isEmpty()) {
            throw new RegraDeNegocioException("Selecione ao menos um formato de leitura de seu interesse");
        }
        this.apelido = apelido.trim().toLowerCase();
        if (fusoHorario != null && !fusoHorario.isBlank()) {
            this.fusoHorario = fusoHorario.trim();
        }
        this.fotoUrl = fotoUrl;
        this.descricao = descricao;
        this.formatosPreferidos.clear();
        this.formatosPreferidos.addAll(formatos);
        this.onboardingConcluido = true;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void concluirOnboarding(String apelido, String fusoHorario, String fotoUrl, String descricao) {
        concluirOnboarding(apelido, fusoHorario, fotoUrl, descricao, new HashSet<>(TipoObra.FORMATOS_OFICIAIS));
    }

    public void atualizarFormatosPreferidos(Set<TipoObra> novosFormatos) {
        if (novosFormatos == null || novosFormatos.isEmpty()) {
            throw new RegraDeNegocioException("Pelo menos um formato literário deve ser selecionado nas preferências");
        }
        this.formatosPreferidos.clear();
        this.formatosPreferidos.addAll(novosFormatos);
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public Set<TipoObra> getFormatosPreferidos() {
        if (formatosPreferidos == null || formatosPreferidos.isEmpty()) {
            return new HashSet<>(TipoObra.FORMATOS_OFICIAIS);
        }
        return Collections.unmodifiableSet(formatosPreferidos);
    }

    public void atualizarPerfil(String novoApelido, String novaFotoUrl, String novaDescricao, String novoFusoHorario) {
        if (novoApelido != null && !novoApelido.isBlank()) {
            this.apelido = novoApelido.trim().toLowerCase();
        }
        if (novaFotoUrl != null) {
            this.fotoUrl = novaFotoUrl;
        }
        if (novaDescricao != null) {
            this.descricao = novaDescricao;
        }
        if (novoFusoHorario != null && !novoFusoHorario.isBlank()) {
            this.fusoHorario = novoFusoHorario.trim();
        }
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void atualizarFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void alterarEmail(String novoEmail) {
        if (novoEmail == null || novoEmail.isBlank() || !novoEmail.contains("@")) {
            throw new RegraDeNegocioException("E-mail informado é inválido");
        }
        this.email = novoEmail.trim().toLowerCase();
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public ConexaoUsuario solicitarConexaoCom(Usuario destinatario) {
        if (destinatario == null) {
            throw new RegraDeNegocioException("O destinatário da conexão não pode ser nulo");
        }
        if (this.equals(destinatario)) {
            throw new RegraDeNegocioException("Um usuário não pode solicitar conexão consigo mesmo");
        }

        for (ConexaoUsuario c : this.conexoesEnviadas) {
            if (c.getRecebedor().equals(destinatario)) {
                return c;
            }
        }

        ConexaoUsuario novaConexao = new ConexaoUsuario(this, destinatario);
        this.conexoesEnviadas.add(novaConexao);
        destinatario.conexoesRecebidas.add(novaConexao);
        return novaConexao;
    }

    public boolean isConectadoCom(Usuario outro) {
        if (outro == null) {
            return false;
        }
        for (ConexaoUsuario c : this.conexoesEnviadas) {
            if (c.getRecebedor().equals(outro) && c.isAceita()) {
                return true;
            }
        }
        for (ConexaoUsuario c : this.conexoesRecebidas) {
            if (c.getSolicitante().equals(outro) && c.isAceita()) {
                return true;
            }
        }
        return false;
    }

    public void adicionarItemCatalogo(ItemCatalogo item) {
        if (item == null) {
            throw new RegraDeNegocioException("O item do catálogo não pode ser nulo");
        }
        this.estante.add(item);
    }

    public void removerItemCatalogo(ItemCatalogo item) {
        if (item != null) {
            this.estante.remove(item);
        }
    }

    public void desativarConta() {
        this.ativo = false;
    }

    public void reativarConta() {
        this.ativo = true;
    }

    public void anonimizarParaLGPD() {
        this.ativo = false;
        this.email = "anonimizado_" + (getId() != null ? getId() : System.currentTimeMillis()) + "@anon.shiori.dev";
        this.senha = "CONTA_EXCLUIDA";
        this.apelido = "excluido_" + (getId() != null ? getId() : System.currentTimeMillis());
        this.fotoUrl = null;
        this.descricao = null;
        this.pontosConfianca = 0;
        this.estante.clear();
        this.conexoesEnviadas.clear();
        this.conexoesRecebidas.clear();
        this.consentimentos.clear();
    }

    public ConsentimentoUsuario registrarConsentimento(
            String versaoTermos,
            String versaoPolitica,
            boolean analiticos,
            boolean marketing,
            boolean funcionais,
            String ipOrigem,
            String userAgent) {
        for (ConsentimentoUsuario c : this.consentimentos) {
            c.revogar();
        }
        ConsentimentoUsuario novo = new ConsentimentoUsuario(
                this,
                versaoTermos,
                versaoPolitica,
                analiticos,
                marketing,
                funcionais,
                ipOrigem,
                userAgent);
        this.consentimentos.add(novo);
        return novo;
    }

    public ConsentimentoUsuario getConsentimentoAtual() {
        for (int i = this.consentimentos.size() - 1; i >= 0; i--) {
            ConsentimentoUsuario c = this.consentimentos.get(i);
            if (c.isAtivo()) {
                return c;
            }
        }
        return null;
    }

    public List<ConsentimentoUsuario> getConsentimentos() {
        return Collections.unmodifiableList(consentimentos);
    }

    public void atualizarSenhaComHash(String novaSenhaPura, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        if (novaSenhaPura == null || novaSenhaPura.isBlank()) {
            throw new RegraDeNegocioException("A senha não pode ser vazia");
        }
        if (passwordEncoder == null) {
            throw new RegraDeNegocioException("O codificador de senhas é obrigatório");
        }
        this.senha = passwordEncoder.encode(novaSenhaPura);
    }

    public static Usuario criarComSenhaPura(String email, String senhaPura, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        if (passwordEncoder == null) {
            throw new RegraDeNegocioException("O codificador de senhas é obrigatório");
        }
        return new Usuario(email, passwordEncoder.encode(senhaPura));
    }

    public Map<String, Object> exportarDadosPorAdmin(Usuario adminOperador) {
        if (adminOperador == null || !adminOperador.isAdmin()) {
            throw new RegraDeNegocioException("Apenas administradores podem auditar dados de outros usuários");
        }
        log.info("AUDITORIA_SEGURANCA: Administrador [id={}, apelido={}] acessou dados do usuário [id={}, apelido={}]",
                adminOperador.getId(), adminOperador.getApelidoOuEmail(), getId(), getApelidoOuEmail());
        return exportarDadosEmJson();
    }

    public void registrarAcessoAdministrativo(Usuario adminOperador, String motivo) {
        if (adminOperador == null || !adminOperador.isAdmin()) {
            throw new RegraDeNegocioException("Apenas administradores podem registrar acesso administrativo");
        }
        log.info("AUDITORIA_SEGURANCA: Administrador [id={}, apelido={}] acessou configurações do usuário [id={}, apelido={}], motivo: {}",
                adminOperador.getId(), adminOperador.getApelidoOuEmail(), getId(), getApelidoOuEmail(), motivo);
    }

    public void banirPorAdmin(Usuario adminOperador, String motivo) {
        if (adminOperador == null || !adminOperador.isAdmin()) {
            throw new RegraDeNegocioException("Apenas administradores podem banir usuários");
        }
        if (this.isAdmin()) {
            throw new RegraDeNegocioException("Não é permitido banir outro administrador");
        }
        this.ativo = false;
        log.warn("AUDITORIA_SEGURANCA: Administrador [id={}, apelido={}] baniu o usuário [id={}, apelido={}], motivo: {}",
                adminOperador.getId(), adminOperador.getApelidoOuEmail(), getId(), getApelidoOuEmail(), motivo);
    }

    public void alterarRolePorAdmin(Usuario adminOperador, Role novaRole) {
        if (adminOperador == null || !adminOperador.isAdmin()) {
            throw new RegraDeNegocioException("Apenas administradores podem alterar a role de outros usuários");
        }
        if (novaRole == null) {
            throw new RegraDeNegocioException("A nova role não pode ser nula");
        }
        if (getId() != null && getId().equals(adminOperador.getId()) && novaRole != Role.ROLE_ADMIN) {
            throw new RegraDeNegocioException("O administrador não pode revogar o próprio papel de administrador");
        }
        Role roleAntiga = this.role;
        this.role = novaRole;
        log.warn("AUDITORIA_SEGURANCA: Administrador [id={}, apelido={}] alterou a role do usuário [id={}, apelido={}] de {} para {}",
                adminOperador.getId(), adminOperador.getApelidoOuEmail(), getId(), getApelidoOuEmail(), roleAntiga, novaRole);
    }

    public void alterarStatusAtivoPorAdmin(Usuario adminOperador, boolean novoStatus, String motivo) {
        if (adminOperador == null || !adminOperador.isAdmin()) {
            throw new RegraDeNegocioException("Apenas administradores podem alterar o status de contas");
        }
        if (getId() != null && getId().equals(adminOperador.getId()) && !novoStatus) {
            throw new RegraDeNegocioException("O administrador não pode inativar a própria conta");
        }
        this.ativo = novoStatus;
        log.warn("AUDITORIA_SEGURANCA: Administrador [id={}, apelido={}] alterou status da conta do usuário [id={}, apelido={}] para ativo={}, motivo: {}",
                adminOperador.getId(), adminOperador.getApelidoOuEmail(), getId(), getApelidoOuEmail(), novoStatus, motivo);
    }

    public int aplicarStrikePorAdmin(Usuario adminOperador, String motivo) {
        if (adminOperador == null || !adminOperador.isAdmin()) {
            throw new RegraDeNegocioException("Apenas administradores podem aplicar strikes a usuários");
        }
        if (this.isAdmin()) {
            throw new RegraDeNegocioException("Não é permitido aplicar strike em outro administrador");
        }
        if (this.strikes == null) {
            this.strikes = 0;
        }
        this.strikes++;
        boolean banidoAutomatico = false;
        if (this.strikes >= 3) {
            this.ativo = false;
            banidoAutomatico = true;
        }
        log.warn("AUDITORIA_SEGURANCA: Administrador [id={}, apelido={}] aplicou STRIKE no usuário [id={}, apelido={}]. Total de strikes: {}. Banido automaticamente: {}. Motivo: {}",
                adminOperador.getId(), adminOperador.getApelidoOuEmail(), getId(), getApelidoOuEmail(), this.strikes, banidoAutomatico, motivo);
        return this.strikes;
    }

    public void zerarOuRemoverStrikePorAdmin(Usuario adminOperador, String motivo) {
        if (adminOperador == null || !adminOperador.isAdmin()) {
            throw new RegraDeNegocioException("Apenas administradores podem remover strikes");
        }
        if (this.strikes != null && this.strikes > 0) {
            this.strikes--;
        }
        log.warn("AUDITORIA_SEGURANCA: Administrador [id={}, apelido={}] removeu strike do usuário [id={}, apelido={}]. Total restante: {}. Motivo: {}",
                adminOperador.getId(), adminOperador.getApelidoOuEmail(), getId(), getApelidoOuEmail(), this.strikes, motivo);
    }

    public Integer getStrikes() {
        return strikes != null ? strikes : 0;
    }

    public boolean isAdmin() {
        return Role.ROLE_ADMIN.equals(this.role);
    }

    public boolean isCurador() {
        return Role.ROLE_CURADOR.equals(this.role) || isAdmin();
    }

    public void promoverPara(Role novaRole) {
        if (novaRole == null) {
            throw new RegraDeNegocioException("A nova role não pode ser nula");
        }
        this.role = novaRole;
    }

    public void adicionarPontosConfianca(int pontos) {
        if (pontos < 0) {
            throw new RegraDeNegocioException("Não é possível adicionar pontos negativos");
        }
        if (this.pontosConfianca == null) {
            this.pontosConfianca = 0;
        }
        this.pontosConfianca += pontos;
    }

    public Map<String, Object> exportarDadosEmJson() {
        Map<String, Object> dados = new HashMap<>();
        dados.put("id", getId());
        dados.put("email", this.email);
        dados.put("apelido", this.apelido);
        dados.put("fotoUrl", this.fotoUrl);
        dados.put("descricao", this.descricao);
        dados.put("fusoHorario", this.fusoHorario);
        dados.put("dataCriacao", this.dataCriacao.toString());
        dados.put("onboardingConcluido", this.onboardingConcluido);
        dados.put("role", this.role != null ? this.role.name() : Role.ROLE_USER.name());
        dados.put("pontosConfianca", this.pontosConfianca);
        dados.put("totalItensEstante", this.estante.size());
        dados.put("totalConexoesEnviadas", this.conexoesEnviadas.size());
        dados.put("totalConexoesRecebidas", this.conexoesRecebidas.size());
        dados.put("formatosPreferidos", this.getFormatosPreferidos().stream().map(tipo -> tipo.name()).toList());
        return dados;
    }

    public String getApelidoOuEmail() {
        return (this.apelido != null && !this.apelido.isBlank()) ? this.apelido : this.email;
    }

    public List<ItemCatalogo> getEstante() {
        return Collections.unmodifiableList(estante);
    }

    public List<ConexaoUsuario> getConexoesEnviadas() {
        return Collections.unmodifiableList(conexoesEnviadas);
    }

    public List<ConexaoUsuario> getConexoesRecebidas() {
        return Collections.unmodifiableList(conexoesRecebidas);
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getApelido() {
        return apelido;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getFusoHorario() {
        return fusoHorario;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public boolean isOnboardingConcluido() {
        return onboardingConcluido;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Role getRole() {
        return role;
    }

    public Integer getPontosConfianca() {
        return pontosConfianca;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        if (getId() != null && usuario.getId() != null) {
            return getId().equals(usuario.getId());
        }
        return Objects.equals(email, usuario.email);
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        }
        return Objects.hashCode(email);
    }
}
