package broker.proveedores;

import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;

import java.nio.file.Path;
import java.util.Set;

/**
 * Proveedor alternativo para el servicio "biblioteca": consulta el repositorio digital.
 * Se usa para demostrar la SUSTITUCIÓN de un proveedor: ofrece la misma operación con otra
 * implementación y otros datos, y el cliente no cambia ni una línea.
 * Datos en datos/biblioteca-digital.json: { "Título": licencias libres, ... }
 */
public class ServicioBibliotecaDigital implements ProveedorServicio {

    private final Path archivo;

    public ServicioBibliotecaDigital() {
        this(Datos.CARPETA.resolve("biblioteca-digital.json"));
    }

    public ServicioBibliotecaDigital(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public Set<String> operaciones() {
        return Set.of("disponibilidad");
    }

    @Override
    public Respuesta ejecutar(Solicitud s) {
        String titulo = s.parametro("titulo");
        if (titulo == null || titulo.isBlank()) {
            return Respuesta.error(s.id(), Estado.PARAMETRO_INVALIDO, "Falta el parámetro 'titulo'");
        }
        Object licencias = Datos.buscarSinMayusculas(Datos.leer(archivo), titulo);
        int n = licencias == null ? 0 : Integer.parseInt(licencias.toString());
        return Respuesta.ok(s.id(), "[Biblioteca digital] '" + titulo + "': " + n + " licencia(s) digital(es) libres");
    }
}
