package dev.shiori.infra.bootstrap;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.shiori.domain.enums.Role;
import dev.shiori.domain.model.FeatureFlag;
import dev.shiori.domain.model.ProvedorIntegracao;
import dev.shiori.domain.model.Usuario;
import dev.shiori.repository.FeatureFlagRepository;
import dev.shiori.repository.ProvedorIntegracaoRepository;
import dev.shiori.repository.UsuarioRepository;

@Component
@Order(1)
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);
    public static final String MASTER_ADMIN_APELIDO = "sys_overseer";
    public static final String MASTER_ADMIN_EMAIL = "sys_overseer@shiori.dev";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final FeatureFlagRepository featureFlagRepository;
    private final ProvedorIntegracaoRepository provedorIntegracaoRepository;
    private final String adminDefaultPassword;

    public AdminBootstrapRunner(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            FeatureFlagRepository featureFlagRepository,
            ProvedorIntegracaoRepository provedorIntegracaoRepository,
            @Value("${shiori.admin.default-password:}") String adminDefaultPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.featureFlagRepository = featureFlagRepository;
        this.provedorIntegracaoRepository = provedorIntegracaoRepository;
        this.adminDefaultPassword = adminDefaultPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        provisionarAdminMaster();
        provisionarFeatureFlags();
        provisionarProvedoresIntegracao();
    }

    private void provisionarAdminMaster() {
        if (usuarioRepository.existsByApelido(MASTER_ADMIN_APELIDO) || usuarioRepository.existsByEmail(MASTER_ADMIN_EMAIL)) {
            log.info("BOOTSTRAP_ADMIN: Usuário Master '{}' já existe no sistema.", MASTER_ADMIN_APELIDO);
            return;
        }

        String rawPassword = adminDefaultPassword;
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            log.warn("BOOTSTRAP_ADMIN: ADMIN_DEFAULT_PASSWORD não foi informada. Gerada senha temporária de emergência para inicialização.");
        }

        Usuario overseer = Usuario.criarComSenhaPura(MASTER_ADMIN_EMAIL, rawPassword, passwordEncoder);
        overseer.atualizarPerfil(MASTER_ADMIN_APELIDO, null, "Administrador Master do Sistema Shiori", "America/Sao_Paulo");
        overseer.promoverPara(Role.ROLE_ADMIN);
        overseer.adicionarPontosConfianca(1000);

        usuarioRepository.save(overseer);
        log.warn("AUDITORIA_SEGURANCA: Administrador Master '{}' provisionado com sucesso com role ROLE_ADMIN.", MASTER_ADMIN_APELIDO);
    }

    private void provisionarFeatureFlags() {
        if (!featureFlagRepository.existsByChaveIgnoreCase("MODO_MANUTENCAO")) {
            featureFlagRepository.save(new FeatureFlag("MODO_MANUTENCAO", "Modo Manutenção", "Restringe rotas da plataforma para manutenções programadas", false));
        }
        if (!featureFlagRepository.existsByChaveIgnoreCase("BLOQUEAR_NOVOS_CADASTROS")) {
            featureFlagRepository.save(new FeatureFlag("BLOQUEAR_NOVOS_CADASTROS", "Bloquear Novos Cadastros", "Impede novos registros de contas na plataforma", false));
        }
        if (!featureFlagRepository.existsByChaveIgnoreCase("PERMITIR_UPLOADS_COMUNIDADE")) {
            featureFlagRepository.save(new FeatureFlag("PERMITIR_UPLOADS_COMUNIDADE", "Uploads da Comunidade", "Permite que membros enviem imagens de capas e avatares", true));
        }
    }

    private void provisionarProvedoresIntegracao() {
        if (!provedorIntegracaoRepository.existsByCodigoIgnoreCase("ANILIST")) {
            provedorIntegracaoRepository.save(new ProvedorIntegracao("ANILIST", "AniList API v2", "https://graphql.anilist.co", null, 10000L));
        }
        if (!provedorIntegracaoRepository.existsByCodigoIgnoreCase("MANGADEX")) {
            provedorIntegracaoRepository.save(new ProvedorIntegracao("MANGADEX", "MangaDex API v5", "https://api.mangadex.org", null, 15000L));
        }
        if (!provedorIntegracaoRepository.existsByCodigoIgnoreCase("OPENLIBRARY")) {
            provedorIntegracaoRepository.save(new ProvedorIntegracao("OPENLIBRARY", "Open Library API", "https://openlibrary.org", null, 20000L));
        }
        if (!provedorIntegracaoRepository.existsByCodigoIgnoreCase("KITSU")) {
            provedorIntegracaoRepository.save(new ProvedorIntegracao("KITSU", "Kitsu API (Novels & Mangá)", "https://kitsu.io/api/edge", null, 15000L));
        }
    }
}
