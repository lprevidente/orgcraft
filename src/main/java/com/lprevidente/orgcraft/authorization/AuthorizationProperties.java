package com.lprevidente.orgcraft.authorization;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("orgcraft.spicedb")
public record AuthorizationProperties(
    boolean enabled, //
    String endpoint, String presharedKey, boolean plaintext) {}
