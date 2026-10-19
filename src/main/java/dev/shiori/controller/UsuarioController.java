package dev.shiori.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dev.shiori.dto.usuario.UsuarioPerfilResponse;
import dev.shiori.infra.security.CurrentUser;
import dev.shiori.infra.security.UsuarioPrincipal;
import dev.shiori.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioPerfilResponse> uploadAvatar(
            @CurrentUser UsuarioPrincipal usuarioLogado,
            @RequestParam("file") MultipartFile file) {
        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UsuarioPerfilResponse perfilAtualizado = usuarioService.atualizarAvatar(usuarioLogado.getId(), file);
        return ResponseEntity.ok(perfilAtualizado);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioPerfilResponse> obterMeuPerfil(@CurrentUser UsuarioPrincipal usuarioLogado) {
        if (usuarioLogado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UsuarioPerfilResponse perfil = usuarioService.buscarPerfilPorId(usuarioLogado.getId());
        return ResponseEntity.ok(perfil);
    }
}
