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

class ServicioBibliotecaDigitalTest {

    @TempDir
    Path carpeta;

    private ServicioBibliotecaDigital servicio;

    @BeforeEach
    void preparar() throws IOException {
        Path archivo = Files.writeString(carpeta.resolve("biblioteca-digital.json"), "{\"Clean Code\": 10}");
        servicio = new ServicioBibliotecaDigital(archivo);
    }

    private Respuesta consultar(Map<String, String> params) {
        return servicio.ejecutar(Solicitud.nueva("biblioteca", "disponibilidad", params));
    }

    @Test
    void operacionesEsLaMismaQueLaBibliotecaFisica() {
        assertEquals(new ServicioBiblioteca().operaciones(), servicio.operaciones());
        assertEquals(Set.of("disponibilidad"), servicio.operaciones());
    }

    @Test
    void ejecutarDevuelveLicenciasDigitales() {
        Respuesta r = consultar(Map.of("titulo", "clean code"));
        assertTrue(r.exitosa());
        assertEquals("[Biblioteca digital] 'clean code': 10 licencia(s) digital(es) libres", r.resultado());
    }

    @Test
    void ejecutarConLibroDesconocidoDaCero() {
        assertTrue(consultar(Map.of("titulo", "Harry Potter")).resultado().contains("0 licencia(s)"));
    }

    @Test
    void ejecutarRechazaTituloFaltante() {
        assertEquals(Estado.PARAMETRO_INVALIDO, consultar(Map.of()).estado());
    }
}
