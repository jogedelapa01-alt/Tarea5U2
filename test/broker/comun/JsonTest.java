package broker.comun;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonTest {

    // ---------------------------------------------------------------- escribir

    @Test
    void escribirObjetoSimple() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("a", "1");
        m.put("b", "dos");
        assertEquals("{\"a\":\"1\",\"b\":\"dos\"}", Json.escribir(m));
    }

    @Test
    void escribirNullYObjetoAnidado() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("x", null);
        m.put("y", Map.of("z", "w"));
        assertEquals("{\"x\":null,\"y\":{\"z\":\"w\"}}", Json.escribir(m));
    }

    @Test
    void escribirEscapaComillasYSaltosDeLinea() {
        String json = Json.escribir(Map.of("t", "di \"hola\"\nadiós"));
        assertEquals("{\"t\":\"di \\\"hola\\\"\\nadiós\"}", json);
        assertFalse(json.contains("\n"), "un mensaje debe caber en una sola línea");
    }

    // ---------------------------------------------------------------- leer

    @Test
    void leerDevuelveLoQueSeEscribio() {
        Map<String, Object> original = new LinkedHashMap<>();
        original.put("texto", "a \"b\" \\ c\td");
        original.put("vacio", null);
        original.put("hijo", Map.of("k", "v"));
        assertEquals(original, Json.leer(Json.escribir(original)));
    }

    @Test
    void leerAceptaEspaciosNumerosYUnicode() {
        Map<String, Object> m = Json.leer(" { \"n\" : 42 , \"s\" : \"\\u00e1\" } ");
        assertEquals("42", m.get("n"));
        assertEquals("á", m.get("s"));
    }

    @Test
    void leerListas() {
        Map<String, Object> m = Json.leer("{\"l\": [\"a\", 2, {\"k\":\"v\"}], \"vacia\": []}");
        assertEquals(java.util.List.of("a", "2", Map.of("k", "v")), m.get("l"));
        assertEquals(java.util.List.of(), m.get("vacia"));
    }

    @Test
    void leerObjetoVacio() {
        assertTrue(Json.leer("{}").isEmpty());
    }

    @Test
    void leerRechazaTextoQueNoEsJson() {
        assertThrows(IllegalArgumentException.class, () -> Json.leer("hola"));
        assertThrows(IllegalArgumentException.class, () -> Json.leer("{\"a\":\"1\""));   // incompleto
        assertThrows(IllegalArgumentException.class, () -> Json.leer("{\"a\":\"1\"} x")); // sobrante
        assertThrows(IllegalArgumentException.class, () -> Json.leer(null));
    }
}
