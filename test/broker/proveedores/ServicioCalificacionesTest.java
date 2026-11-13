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

class ServicioCalificacionesTest {

    @TempDir
    Path carpeta;

    private Path archivo;
    private ServicioCalificaciones servicio;

    @BeforeEach
    void preparar() throws IOException {
        archivo = Files.writeString(carpeta.resolve("calificaciones.json"),
                "{\"A001\": {\"Redes\": 90, \"Bases de Datos\": 80}}");
        servicio = new ServicioCalificaciones(archivo);
    }

    private Respuesta consultar(Map<String, String> params) {
        return servicio.ejecutar(Solicitud.nueva("calificaciones", "consultar", params));
    }

    @Test
    void operacionesOfreceConsultar() {
        assertEquals(Set.of("consultar"), servicio.operaciones());
    }

    @Test
    void ejecutarDevuelveCalificacionesYPromedio() {
        Respuesta r = consultar(Map.of("matricula", "A001"));
        assertTrue(r.exitosa());
        assertEquals("Alumno A001: {Bases de Datos=80, Redes=90} | promedio 85.0", r.resultado());
    }

    @Test
    void ejecutarLeeLosCambiosDelArchivoSinReiniciar() throws IOException {
        Files.writeString(archivo, "{\"A001\": {\"Redes\": 100}}");
        assertTrue(consultar(Map.of("matricula", "A001")).resultado().contains("Redes=100"));
    }

    @Test
    void ejecutarRechazaMatriculaConFormatoInvalido() {
        assertEquals(Estado.PARAMETRO_INVALIDO, consultar(Map.of("matricula", "xyz")).estado());
    }

    @Test
    void ejecutarRechazaMatriculaFaltante() {
        assertEquals(Estado.PARAMETRO_INVALIDO, consultar(Map.of()).estado());
    }

    @Test
    void ejecutarRechazaAlumnoInexistente() {
        Respuesta r = consultar(Map.of("matricula", "A999"));
        assertEquals(Estado.PARAMETRO_INVALIDO, r.estado());
        assertTrue(r.error().contains("No existe"));
    }

    @Test
    void ejecutarSinArchivoLanzaExcepcion() {
        ServicioCalificaciones sinDatos = new ServicioCalificaciones(carpeta.resolve("no-existe.json"));
        assertThrows(IllegalStateException.class,
                () -> sinDatos.ejecutar(Solicitud.nueva("calificaciones", "consultar", Map.of("matricula", "A001"))));
    }
}
