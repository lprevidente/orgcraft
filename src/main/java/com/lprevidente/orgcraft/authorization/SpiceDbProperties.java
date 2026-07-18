package com.lprevidente.orgcraft.authorization;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("orgcraft.spicedb")
public record SpiceDbProperties(
    boolean enabled, //
    @NotBlank String endpoint,
    @NotBlank String presharedKey,
    boolean plaintext) {}
