package com.lprevidente.orgcraft.office.application.handler;

import com.lprevidente.orgcraft.office.application.command.DeleteOffice;
import com.lprevidente.orgcraft.office.domain.Offices;
import com.lprevidente.orgcraft.office.domain.exception.OfficeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.jmolecules.ddd.annotation.Service;

@Service
@RequiredArgsConstructor
public class DeleteOfficeHandler {
  private final Offices offices;

  @CommandHandler
  public void handle(DeleteOffice command) {
    final var office =
        offices
            .findById(command.id())
            .orElseThrow(() -> new OfficeNotFoundException(command.id()));
    office.delete();
    offices.delete(office);
  }
}
