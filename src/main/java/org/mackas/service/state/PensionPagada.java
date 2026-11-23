package org.mackas.service.state;

import org.mackas.model.Pension;

public class PensionPagada implements EstadoPension {
    @Override
    public String nombre() {
        return "PAGADO";
    }

    @Override
    public void pagar(Pension pension) {
        throw new IllegalStateException("La pensión ya fue pagada.");
    }
}
