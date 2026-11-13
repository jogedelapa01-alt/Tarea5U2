package broker.proveedores;

import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ServicioBibliotecaTest {

    @TempDir
    Path carpeta;

    private ServicioBiblioteca servicio;

    @BeforeEach
    void preparar() throws IOException {
        Path archivo = Files.writeString(carpeta.resolve("biblioteca.json"), "{\"Clean Code\": 3, \"Patrones\": 0}");
        servicio = new ServicioBiblioteca(archivo);
    }

    private Respuesta consultar(Map<String, String> params) {
        return servicio.ejecutar(Solicitud.nueva("biblioteca", "disponibilidad", params));
    }

    @Test
    void operacionesOfreceDisponibilidad() {
        assertEquals(Set.of("disponibilidad"), servicio.operaciones());
    }

    @Test
    void ejecutarIndicaEjemplaresSinImportarMayusculas() {
        Respuesta r = consultar(Map.of("titulo", "CLEAN CODE"));
        assertTrue(r.exitosa());
        assertTrue(r.resultado().contains("3 ejemplar"));
    }

    @Test
    void ejecutarIndicaCuandoNoHayEjemplares() {
        assertTrue(consultar(Map.of("titulo", "Patrones")).resultado().contains("sin ejemplares"));
    }

    @Test
    void ejecutarIndicaLibroFueraDelCatalogo() {
        Respuesta r = consultar(Map.of("titulo", "Harry Potter"));
        assertTrue(r.exitosa());
        assertTrue(r.resultado().contains("no está en el catálogo"));
    }

    @Test
    void ejecutarRechazaTituloFaltante() {
        assertEquals(Estado.PARAMETRO_INVALIDO, consultar(Map.of()).estado());
    }

    @Test
    void ejecutarLanzaExcepcionConFallaSimulada() {
        assertThrows(IllegalStateException.class, () -> consultar(Map.of("titulo", "Clean Code", "simularFalla", "true")));
    }
}
