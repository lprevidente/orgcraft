package com.lprevidente.orgcraft.organization.application.command;

import jakarta.validation.constraints.NotBlank;
import org.jmolecules.architecture.cqrs.Command;

@Command
public record RegisterOrganization(
    @NotBlank String name,
    @NotBlank String slug,
    @NotBlank String founderFirstName,
    @NotBlank String founderLastName,
    @NotBlank String founderEmail,
    @NotBlank String founderPassword) {}
