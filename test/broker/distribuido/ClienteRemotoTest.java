package broker.distribuido;

import broker.comun.Json;
import broker.comun.Red;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba ClienteRemoto.enviarAlBroker. Usa el puerto real del Broker (5000):
 * el Broker de verdad NO debe estar corriendo mientras se ejecutan los tests.
 */
class ClienteRemotoTest {

    @Test
    void enviarAlBrokerConBrokerEncendidoYApagado() throws IOException {
        Solicitud s = Solicitud.nueva("materias", "listar", Map.of());

        // Broker falso: comprueba que llega un sobre "solicitud" y contesta OK
        try (ServerSocket brokerFalso = ServidorDePrueba.iniciar(Red.BROKER_PUERTO, linea -> {
            Map<String, Object> sobre = Json.leer(linea);
            return Respuesta.ok(Solicitud.desdeMapa((Map<?, ?>) sobre.get("solicitud")).id(), "tipo=" + sobre.get("tipo")).aJson();
        })) {
            Respuesta r = ClienteRemoto.enviarAlBroker(s);
            assertTrue(r.exitosa());
            assertEquals("tipo=solicitud", r.resultado());
            assertEquals(s.id(), r.idSolicitud());
        }

        // Broker apagado: el cliente no lanza excepción, devuelve un estado de error
        assertEquals(Estado.BROKER_NO_DISPONIBLE, ClienteRemoto.enviarAlBroker(s).estado());
    }
}
