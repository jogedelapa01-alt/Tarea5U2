package broker.comun;

import broker.comun.Respuesta.Estado;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RespuestaTest {

    @Test
    void okEsExitosaYTraeResultado() {
        Respuesta r = Respuesta.ok("1", "datos");
        assertEquals(Estado.EXITO, r.estado());
        assertEquals("datos", r.resultado());
        assertNull(r.error());
        assertTrue(r.exitosa());
    }

    @Test
    void errorNoEsExitosaYTraeMensaje() {
        Respuesta r = Respuesta.error("1", Estado.PARAMETRO_INVALIDO, "mal");
        assertEquals(Estado.PARAMETRO_INVALIDO, r.estado());
        assertNull(r.resultado());
        assertEquals("mal", r.error());
        assertFalse(r.exitosa());
    }

    @Test
    void toStringMuestraEstadoYResultadoOError() {
        assertEquals("[EXITO] datos  (solicitud 1)", Respuesta.ok("1", "datos").toString());
        assertEquals("[ERROR_PROVEEDOR] fallo  (solicitud 2)", Respuesta.error("2", Estado.ERROR_PROVEEDOR, "fallo").toString());
    }

    @Test
    void aJsonYDesdeJsonSonInversos() {
        Respuesta ok = Respuesta.ok("1", "datos");
        Respuesta error = Respuesta.error("2", Estado.SERVICIO_DESCONOCIDO, "no existe");
        assertEquals(ok, Respuesta.desdeJson(ok.aJson()));
        assertEquals(error, Respuesta.desdeJson(error.aJson()));
    }

    @Test
    void desdeJsonRechazaEstadoDesconocido() {
        assertThrows(IllegalArgumentException.class,
                () -> Respuesta.desdeJson("{\"estado\":\"RARO\",\"fecha\":\"2026-11-10T10:00\"}"));
    }
}
