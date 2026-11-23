package org.mackas.service.state;

import org.mackas.model.Pension;

/** State contract for allowed pension transitions. */
public interface EstadoPension {
    String nombre();
    void pagar(Pension pension);
}
