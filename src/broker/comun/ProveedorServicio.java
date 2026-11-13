package broker.comun;

import java.util.Set;

/**
 * Interfaz común que implementan todos los proveedores.
 * El Broker solo conoce esta interfaz; nunca las clases concretas.
 */
public interface ProveedorServicio {

    /** Operaciones que ofrece este proveedor (el Broker valida contra ellas antes de reenviar). */
    Set<String> operaciones();

    /** Procesa la solicitud y devuelve la respuesta. */
    Respuesta ejecutar(Solicitud solicitud);

    /** Nombre legible para la bitácora. */
    default String descripcion() {
        return getClass().getSimpleName();
    }
}
