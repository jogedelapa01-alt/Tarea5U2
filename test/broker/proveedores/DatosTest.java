package broker.proveedores;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DatosTest {

    @TempDir
    Path carpeta;

    @Test
    void leerConvierteElArchivoEnMapa() throws IOException {
        Path archivo = Files.writeString(carpeta.resolve("x.json"), "{\"Clean Code\": 3}");
        assertEquals(Map.of("Clean Code", "3"), Datos.leer(archivo));
    }

    @Test
    void leerArchivoInexistenteLanzaIllegalState() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> Datos.leer(carpeta.resolve("no.json")));
        assertTrue(e.getMessage().contains("no.json"));
    }

    @Test
    void leerJsonMalEscritoLanzaIllegalState() throws IOException {
        Path archivo = Files.writeString(carpeta.resolve("roto.json"), "{\"a\": ");
        assertThrows(IllegalStateException.class, () -> Datos.leer(archivo));
    }

    @Test
    void buscarSinMayusculasEncuentraLaClave() {
        Map<String, Object> datos = Map.of("Clean Code", "3");
        assertEquals("3", Datos.buscarSinMayusculas(datos, "clean CODE"));
        assertNull(Datos.buscarSinMayusculas(datos, "otro"));
    }

    @Test
    void losArchivosRealesDeDatosSonJsonValido() {
        for (String nombre : new String[]{"calificaciones", "materias", "biblioteca", "biblioteca-digital"}) {
            assertFalse(Datos.leer(Datos.CARPETA.resolve(nombre + ".json")).isEmpty(), nombre + ".json está vacío o falta");
        }
    }
}
