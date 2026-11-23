package org.mackas.service.state;

import java.time.LocalDateTime;
import org.mackas.model.Pension;

public class PensionPendiente implements EstadoPension {
    @Override
    public String nombre() {
        return "PENDIENTE";
    }

    @Override
    public void pagar(Pension pension) {
        pension.setFechaPago(LocalDateTime.now());
        pension.cambiarEstado(new PensionPagada());
    }
}
