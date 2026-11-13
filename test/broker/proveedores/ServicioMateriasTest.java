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

class ServicioMateriasTest {

    @TempDir
    Path carpeta;

    private ServicioMaterias servicio;

    @BeforeEach
    void preparar() throws IOException {
        Path archivo = Files.writeString(carpeta.resolve("materias.json"), "{\"1\": [\"Álgebra\", \"Cálculo\"]}");
        servicio = new ServicioMaterias(archivo);
    }

    private Respuesta listar(String semestre) {
        return servicio.ejecutar(Solicitud.nueva("materias", "listar", Map.of("semestre", semestre)));
    }

    @Test
    void operacionesOfreceListar() {
        assertEquals(Set.of("listar"), servicio.operaciones());
    }

    @Test
    void ejecutarDevuelveLasMateriasDelSemestre() {
        Respuesta r = listar("1");
        assertTrue(r.exitosa());
        assertEquals("Semestre 1: [Álgebra, Cálculo]", r.resultado());
    }

    @Test
    void ejecutarRechazaSemestreNoNumerico() {
        assertEquals(Estado.PARAMETRO_INVALIDO, listar("tres").estado());
    }

    @Test
    void ejecutarRechazaSemestreSinMaterias() {
        assertEquals(Estado.PARAMETRO_INVALIDO, listar("9").estado());
    }
}
