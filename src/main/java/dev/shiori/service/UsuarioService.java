package dev.shiori.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import dev.shiori.domain.exception.RecursoNaoEncontradoException;
import dev.shiori.domain.model.Usuario;
import dev.shiori.dto.usuario.UsuarioPerfilResponse;
import dev.shiori.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final StorageService storageService;

    public UsuarioService(UsuarioRepository usuarioRepository, StorageService storageService) {
        this.usuarioRepository = usuarioRepository;
        this.storageService = storageService;
    }

    @Transactional
    public UsuarioPerfilResponse atualizarAvatar(Long usuarioId, MultipartFile file) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        String fotoUrl = storageService.uploadFile(file, "avatares");
        usuario.atualizarFotoUrl(fotoUrl);
        usuarioRepository.save(usuario);

        return UsuarioPerfilResponse.aPartirDe(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioPerfilResponse buscarPerfilPorId(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        return UsuarioPerfilResponse.aPartirDe(usuario);
    }
}
