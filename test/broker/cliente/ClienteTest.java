package broker.cliente;

import broker.comun.Respuesta;
import broker.comun.Solicitud;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    void solicitarArmaLaSolicitudYDevuelveLaRespuestaDelBroker() {
        AtomicReference<Solicitud> recibida = new AtomicReference<>();
        // Broker falso: guarda lo que recibe y contesta OK. El cliente no sabe que es falso.
        Cliente cliente = new Cliente(s -> {
            recibida.set(s);
            return Respuesta.ok(s.id(), "listo");
        });

        Respuesta r = cliente.solicitar("materias", "listar", Map.of("semestre", "1"));

        assertEquals("materias", recibida.get().servicio());
        assertEquals("listar", recibida.get().operacion());
        assertEquals("1", recibida.get().parametro("semestre"));
        assertEquals(recibida.get().id(), r.idSolicitud());
        assertEquals("listo", r.resultado());
    }
}
