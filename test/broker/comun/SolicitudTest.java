package broker.comun;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SolicitudTest {

    @Test
    void nuevaGeneraIdYFecha() {
        Solicitud s = Solicitud.nueva("materias", "listar", Map.of("semestre", "1"));
        assertEquals(8, s.id().length());
        assertNotNull(s.fecha());
        assertEquals("materias", s.servicio());
        assertEquals("listar", s.operacion());
    }

    @Test
    void nuevaGeneraIdsDistintos() {
        assertNotEquals(Solicitud.nueva("a", "b", Map.of()).id(), Solicitud.nueva("a", "b", Map.of()).id());
    }

    @Test
    void constructorConParametrosYFechaNulos() {
        Solicitud s = new Solicitud("1", "a", "b", null, null);
        assertTrue(s.parametros().isEmpty());
        assertNotNull(s.fecha());
    }

    @Test
    void parametroDevuelveValorONull() {
        Solicitud s = Solicitud.nueva("a", "b", Map.of("k", "v"));
        assertEquals("v", s.parametro("k"));
        assertNull(s.parametro("otro"));
    }

    @Test
    void aMapaContieneTodosLosCampos() {
        Solicitud s = Solicitud.nueva("a", "b", Map.of("k", "v"));
        assertEquals(s.id(), s.aMapa().get("id"));
        assertEquals("a", s.aMapa().get("servicio"));
        assertEquals("b", s.aMapa().get("operacion"));
        assertEquals(Map.of("k", "v"), s.aMapa().get("parametros"));
        assertEquals(s.fecha().toString(), s.aMapa().get("fecha"));
    }

    @Test
    void aJsonYDesdeJsonSonInversos() {
        Solicitud s = Solicitud.nueva("biblioteca", "disponibilidad", Map.of("titulo", "Clean Code"));
        assertEquals(s, Solicitud.desdeJson(s.aJson()));
    }

    @Test
    void desdeMapaSinFechaUsaLaHoraActual() {
        Solicitud s = Solicitud.desdeMapa(Map.of("id", "1", "servicio", "a", "operacion", "b"));
        assertTrue(s.fecha().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(s.parametros().isEmpty());
    }
}
