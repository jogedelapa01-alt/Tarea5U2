package broker.nucleo;

import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BrokerTest {

    private RegistroServicios registro;
    private Broker broker;

    @BeforeEach
    void preparar() {
        registro = new RegistroServicios();
        broker = new Broker(registro);
    }

    private Respuesta pedir(String servicio, String operacion) {
        return broker.procesar(Solicitud.nueva(servicio, operacion, Map.of()));
    }

    @Test
    void procesarReenviaAlProveedorYDevuelveSuRespuesta() {
        registro.registrar("eco", new ProveedorFalso(s -> Respuesta.ok(s.id(), "hola")));
        Respuesta r = pedir("eco", "op");
        assertEquals(Estado.EXITO, r.estado());
        assertEquals("hola", r.resultado());
    }

    @Test
    void procesarRechazaSolicitudSinServicio() {
        assertEquals(Estado.SOLICITUD_INVALIDA, pedir(null, "op").estado());
    }

    @Test
    void procesarRechazaServicioDesconocido() {
        assertEquals(Estado.SERVICIO_DESCONOCIDO, pedir("cafeteria", "op").estado());
    }

    @Test
    void procesarRechazaOperacionQueElProveedorNoOfrece() {
        registro.registrar("eco", new ProveedorFalso(s -> Respuesta.ok(s.id(), "hola")));
        assertEquals(Estado.OPERACION_DESCONOCIDA, pedir("eco", "borrar").estado());
    }

    @Test
    void procesarConvierteLaExcepcionDelProveedorEnError() {
        registro.registrar("roto", new ProveedorFalso(s -> { throw new IllegalStateException("se cayó"); }));
        Respuesta r = pedir("roto", "op");
        assertEquals(Estado.ERROR_PROVEEDOR, r.estado());
        assertTrue(r.error().contains("se cayó"));
    }

    @Test
    void procesarConvierteRespuestaNulaEnError() {
        registro.registrar("nulo", new ProveedorFalso(s -> null));
        assertEquals(Estado.ERROR_PROVEEDOR, pedir("nulo", "op").estado());
    }
}
