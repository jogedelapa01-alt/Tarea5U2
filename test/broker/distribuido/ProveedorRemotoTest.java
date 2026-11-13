package broker.distribuido;

import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProveedorRemotoTest {

    private final Solicitud solicitud = Solicitud.nueva("materias", "listar", Map.of("semestre", "1"));

    @Test
    void ejecutarEnviaPorSocketYDevuelveLaRespuestaDelProveedor() throws IOException {
        // Proveedor falso en red: lee la solicitud JSON y contesta OK con el mismo id
        try (ServerSocket servidor = ServidorDePrueba.iniciar(0,
                linea -> Respuesta.ok(Solicitud.desdeJson(linea).id(), "desde la red").aJson())) {
            ProveedorRemoto remoto = new ProveedorRemoto("localhost", servidor.getLocalPort(), Set.of("listar"));
            Respuesta r = remoto.ejecutar(solicitud);
            assertEquals(Estado.EXITO, r.estado());
            assertEquals(solicitud.id(), r.idSolicitud());
            assertEquals("desde la red", r.resultado());
        }
    }

    @Test
    void ejecutarConProveedorCaidoReintentaYDevuelveNoDisponible() throws IOException {
        ProveedorRemoto remoto = new ProveedorRemoto("localhost", ServidorDePrueba.puertoSinNadie(), Set.of("listar"));
        Respuesta r = remoto.ejecutar(solicitud);
        assertEquals(Estado.PROVEEDOR_NO_DISPONIBLE, r.estado());
        assertTrue(r.error().contains(ProveedorRemoto.INTENTOS + " intentos"));
    }

    @Test
    void descripcionMuestraHostYPuerto() {
        assertEquals("remoto@localhost:6001", new ProveedorRemoto("localhost", 6001, Set.of()).descripcion());
    }

    @Test
    void operacionesSonLasRecibidasAlRegistrarse() {
        assertEquals(Set.of("listar"), new ProveedorRemoto("localhost", 6001, Set.of("listar")).operaciones());
    }
}
