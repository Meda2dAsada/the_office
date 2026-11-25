package office.sync;

import office.model.BufferConfig;

/**
 * Interfaz comun para consultar metricas en vivo de buferes y zonas criticas.
 */
public interface TrackedResource {
    BufferConfig getConfig();
    int getCurrentCount();
    int getWaitingCount();
    void wakeAll();
}
