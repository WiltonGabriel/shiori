package dev.shiori.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import dev.shiori.domain.enums.StatusCatalogo;
import dev.shiori.dto.ObraCadastroRequest;
import dev.shiori.dto.ObraResponse;
import dev.shiori.service.ObraService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/obras")
public class ObraController {

    private final ObraService obraService;

    public ObraController(ObraService obraService) {
        this.obraService = obraService;
    }

    @PostMapping
    public ResponseEntity<ObraResponse> cadastrar(
            @RequestBody @Valid ObraCadastroRequest dados,
            UriComponentsBuilder uriBuilder) {

        ObraResponse response = obraService.cadastrarObra(dados);
        URI uri = uriBuilder.path("/obras/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}/submeter")
    public ResponseEntity<ObraResponse> submeterAoCatalogo(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        ObraResponse response = obraService.submeterAoCatalogoGlobal(id, usuarioId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/promover")
    public ResponseEntity<ObraResponse> promoverParaOficial(@PathVariable Long id) {
        ObraResponse response = obraService.promoverParaOficial(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ObraResponse>> listarTodas(
            @RequestParam(required = false) StatusCatalogo status) {

        List<ObraResponse> lista = obraService.listarTodas(status);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/home")
    public ResponseEntity<List<ObraResponse>> listarObrasHome() {
        List<ObraResponse> lista = obraService.listarObrasDaHome();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObraResponse> buscarPorId(@PathVariable Long id) {
        ObraResponse response = obraService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        obraService.excluirObra(id);
        return ResponseEntity.noContent().build();
    }
}
