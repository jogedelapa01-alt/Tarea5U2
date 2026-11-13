package broker.proveedores;

import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;

import java.nio.file.Path;
import java.util.Set;

/**
 * Proveedor: disponibilidad de libros en el catálogo físico.
 * Operación "disponibilidad", parámetro "titulo" (sin distinguir mayúsculas).
 * Datos en datos/biblioteca.json: { "Título": ejemplares, ... }
 * Con el parámetro simularFalla=true lanza una excepción, para probar que el Broker la contiene.
 */
public class ServicioBiblioteca implements ProveedorServicio {

    private final Path archivo;

    public ServicioBiblioteca() {
        this(Datos.CARPETA.resolve("biblioteca.json"));
    }

    public ServicioBiblioteca(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public Set<String> operaciones() {
        return Set.of("disponibilidad");
    }

    @Override
    public Respuesta ejecutar(Solicitud s) {
        if ("true".equals(s.parametro("simularFalla"))) {
            throw new IllegalStateException("Se perdió la conexión con el catálogo físico (falla simulada)");
        }
        String titulo = s.parametro("titulo");
        if (titulo == null || titulo.isBlank()) {
            return Respuesta.error(s.id(), Estado.PARAMETRO_INVALIDO, "Falta el parámetro 'titulo'");
        }
        Object ejemplares = Datos.buscarSinMayusculas(Datos.leer(archivo), titulo);
        if (ejemplares == null) return Respuesta.ok(s.id(), "'" + titulo + "' no está en el catálogo físico");
        int n = Integer.parseInt(ejemplares.toString());
        return Respuesta.ok(s.id(), "'" + titulo + "': " + (n > 0 ? n + " ejemplar(es) disponible(s) en estantería" : "sin ejemplares disponibles"));
    }
}
