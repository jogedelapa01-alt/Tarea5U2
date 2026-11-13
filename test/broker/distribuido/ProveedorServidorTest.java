package broker.distribuido;

import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;
import broker.proveedores.ServicioBiblioteca;
import broker.proveedores.ServicioMaterias;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Prueba ProveedorServidor.atender: lo que hace el proceso proveedor con cada línea JSON que recibe. */
class ProveedorServidorTest {

    private static Respuesta atender(ProveedorServicio proveedor, Solicitud s) {
        return Respuesta.desdeJson(ProveedorServidor.atender("TEST", proveedor, 0, s.aJson()));
    }

    @Test
    void atenderEjecutaElProveedorYDevuelveJson() {
        Solicitud s = Solicitud.nueva("materias", "listar", Map.of("semestre", "1"));
        Respuesta r = atender(new ServicioMaterias(), s);
        assertTrue(r.exitosa());
        assertEquals(s.id(), r.idSolicitud());
    }

    @Test
    void atenderConvierteLaExcepcionEnErrorProveedor() {
        Solicitud s = Solicitud.nueva("biblioteca", "disponibilidad", Map.of("titulo", "x", "simularFalla", "true"));
        Respuesta r = atender(new ServicioBiblioteca(), s);
        assertEquals(Estado.ERROR_PROVEEDOR, r.estado());
        assertTrue(r.error().contains("IllegalStateException"));
    }

    @Test
    void atenderRechazaJsonIlegible() {
        Respuesta r = Respuesta.desdeJson(ProveedorServidor.atender("TEST", new ServicioMaterias(), 0, "basura"));
        assertEquals(Estado.SOLICITUD_INVALIDA, r.estado());
    }
}
