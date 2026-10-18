package dev.shiori.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import dev.shiori.service.ObraService;
import dev.shiori.domain.enums.StatusLeitura;
import dev.shiori.dto.ObraCadastroRequest;
import dev.shiori.dto.NovoCicloRequest;
import dev.shiori.dto.AtualizarLeituraRequest;
import dev.shiori.dto.ObraResponse;

@RestController
@RequestMapping("/obras")
public class ObraController {

    private final ObraService obraService;

    public ObraController(ObraService obraService) {
        this.obraService = obraService;
    }

    @PostMapping
    public ResponseEntity<ObraResponse> cadastrar(
            @RequestBody ObraCadastroRequest dados,
            UriComponentsBuilder uriBuilder) {

        ObraResponse response = obraService.cadastrarObra(dados);

        URI uri = uriBuilder.path("/obras/{id}").buildAndExpand(response.id()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PostMapping("/{id}/ciclos")
    public ResponseEntity<ObraResponse> registrarCiclo(
            @PathVariable Long id,
            @RequestBody NovoCicloRequest dados) {

        ObraResponse response = obraService.registrarCiclo(id, dados);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ObraResponse>> listarTodas(
            @RequestParam(required = false) StatusLeitura status) {

        List<ObraResponse> lista = obraService.listarTodas(status);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObraResponse> buscarPorId(@PathVariable Long id) {
        ObraResponse response = obraService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/leitura")
    public ResponseEntity<ObraResponse> atualizarLeitura(
            @PathVariable Long id,
            @RequestBody AtualizarLeituraRequest dados) {

        ObraResponse response = obraService.atualizarLeitura(id, dados);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        obraService.excluirObra(id);
        return ResponseEntity.noContent().build();
    }
}
