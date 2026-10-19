package dev.shiori.infra.integration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import dev.shiori.domain.exception.RegraDeNegocioException;

@Component
public class ProvedorIntegracaoFactory {

    private final Map<String, ClienteIntegracaoExterna> clientesPorCodigo;

    public ProvedorIntegracaoFactory(List<ClienteIntegracaoExterna> clientes) {
        this.clientesPorCodigo = clientes.stream()
                .collect(Collectors.toMap(c -> c.getCodigoProvedor().toUpperCase(), c -> c));
    }

    public ClienteIntegracaoExterna obterCliente(String codigoProvedor) {
        if (codigoProvedor == null) {
            throw new RegraDeNegocioException("Código de provedor não informado");
        }
        ClienteIntegracaoExterna cliente = clientesPorCodigo.get(codigoProvedor.trim().toUpperCase());
        if (cliente == null) {
            throw new RegraDeNegocioException("Provedor de integração não suportado ou não implementado: " + codigoProvedor);
        }
        return cliente;
    }

    public boolean suportaProvedor(String codigoProvedor) {
        return codigoProvedor != null && clientesPorCodigo.containsKey(codigoProvedor.trim().toUpperCase());
    }
}
