package dev.shiori.dto.admin;

import dev.shiori.domain.enums.Role;
import jakarta.validation.constraints.NotNull;

public record AlterarRoleUsuarioRequest(
        @NotNull(message = "A role é obrigatória")
        Role role
) {}
