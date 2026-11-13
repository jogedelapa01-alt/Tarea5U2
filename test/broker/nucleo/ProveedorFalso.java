package broker.nucleo;

import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Solicitud;

import java.util.Set;
import java.util.function.Function;

/** Proveedor de prueba: ofrece la operación "op" y responde lo que diga la función que recibe. */
class ProveedorFalso implements ProveedorServicio {

    private final Function<Solicitud, Respuesta> comportamiento;

    ProveedorFalso(Function<Solicitud, Respuesta> comportamiento) {
        this.comportamiento = comportamiento;
    }

    @Override
    public Set<String> operaciones() {
        return Set.of("op");
    }

    @Override
    public Respuesta ejecutar(Solicitud solicitud) {
        return comportamiento.apply(solicitud);
    }
}
