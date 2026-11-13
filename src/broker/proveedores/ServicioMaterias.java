package broker.proveedores;

import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Proveedor: consulta de materias por semestre. Operación "listar", parámetro "semestre" (número).
 * Datos en datos/materias.json: { "1": ["Materia", ...], ... }
 */
public class ServicioMaterias implements ProveedorServicio {

    private final Path archivo;

    public ServicioMaterias() {
        this(Datos.CARPETA.resolve("materias.json"));
    }

    public ServicioMaterias(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public Set<String> operaciones() {
        return Set.of("listar");
    }

    @Override
    public Respuesta ejecutar(Solicitud s) {
        String valor = s.parametro("semestre");
        int semestre;
        try {
            semestre = Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return Respuesta.error(s.id(), Estado.PARAMETRO_INVALIDO, "El semestre debe ser un número, se recibió '" + valor + "'");
        }
        if (!(Datos.leer(archivo).get(String.valueOf(semestre)) instanceof List<?> materias)) {
            return Respuesta.error(s.id(), Estado.PARAMETRO_INVALIDO, "No hay materias registradas para el semestre " + semestre);
        }
        return Respuesta.ok(s.id(), "Semestre " + semestre + ": " + materias);
    }
}
