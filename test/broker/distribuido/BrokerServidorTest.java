package broker.distribuido;

import broker.comun.Json;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;
import broker.nucleo.Broker;
import broker.nucleo.RegistroServicios;
import broker.proveedores.ServicioMaterias;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Prueba BrokerServidor.manejar: cómo el Broker interpreta cada mensaje JSON que le llega. */
class BrokerServidorTest {

    private final RegistroServicios registro = new RegistroServicios();
    private final Broker broker = new Broker(registro);

    private Respuesta manejar(String json) {
        return Respuesta.desdeJson(BrokerServidor.manejar(json, registro, broker));
    }

    private static final String ALTA =
            "{\"tipo\":\"registro\",\"servicio\":\"materias\",\"host\":\"localhost\",\"puerto\":\"6002\",\"operaciones\":\"listar\"}";

    @Test
    void manejarRegistroDaDeAltaUnProveedorRemoto() {
        assertTrue(manejar(ALTA).exitosa());
        assertEquals(new ProveedorRemoto("localhost", 6002, Set.of("listar")), registro.buscar("materias").orElseThrow());
    }

    @Test
    void manejarBajaEliminaAlProveedor() {
        manejar(ALTA);
        assertTrue(manejar(ALTA.replace("registro", "baja")).exitosa());
        assertTrue(registro.buscar("materias").isEmpty());
    }

    @Test
    void manejarSolicitudLaPasaAlBroker() {
        registro.registrar("materias", new ServicioMaterias()); // proveedor local: no hace falta red
        Solicitud s = Solicitud.nueva("materias", "listar", Map.of("semestre", "2"));
        Respuesta r = manejar(Json.escribir(Map.of("tipo", "solicitud", "solicitud", s.aMapa())));
        assertEquals(Estado.EXITO, r.estado());
        assertEquals(s.id(), r.idSolicitud());
    }

    @Test
    void manejarSolicitudDeServicioDesconocido() {
        Solicitud s = Solicitud.nueva("cafeteria", "menu", Map.of());
        assertEquals(Estado.SERVICIO_DESCONOCIDO,
                manejar(Json.escribir(Map.of("tipo", "solicitud", "solicitud", s.aMapa()))).estado());
    }

    @Test
    void manejarRechazaJsonInvalido() {
        assertEquals(Estado.SOLICITUD_INVALIDA, manejar("esto no es json").estado());
    }

    @Test
    void manejarRechazaTipoDesconocido() {
        assertEquals(Estado.SOLICITUD_INVALIDA, manejar("{\"tipo\":\"saludo\"}").estado());
    }

    @Test
    void manejarRechazaMensajesIncompletos() {
        assertEquals(Estado.SOLICITUD_INVALIDA, manejar("{\"tipo\":\"registro\",\"servicio\":\"x\"}").estado());
        assertEquals(Estado.SOLICITUD_INVALIDA, manejar("{\"tipo\":\"solicitud\"}").estado());
    }
}
