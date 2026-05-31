package in.vis.dto.v1;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String name,
        String email,
        String phone
) {}
