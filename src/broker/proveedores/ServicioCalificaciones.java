package broker.proveedores;

import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Proveedor: consulta de calificaciones por matrícula. Operación "consultar", parámetro "matricula" (formato A###).
 * Datos en datos/calificaciones.json: { "A001": { "Materia": calificación, ... }, ... }
 */
public class ServicioCalificaciones implements ProveedorServicio {

    private final Path archivo;

    public ServicioCalificaciones() {
        this(Datos.CARPETA.resolve("calificaciones.json"));
    }

    public ServicioCalificaciones(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public Set<String> operaciones() {
        return Set.of("consultar");
    }

    @Override
    public Respuesta ejecutar(Solicitud s) {
        String matricula = s.parametro("matricula");
        if (matricula == null || !matricula.matches("A\\d{3}")) {
            return Respuesta.error(s.id(), Estado.PARAMETRO_INVALIDO, "Matrícula inválida: '" + matricula + "' (formato esperado A###)");
        }
        // ponytail: se relee el archivo en cada solicitud para que tus cambios se vean sin reiniciar; cachear si importa el rendimiento.
        if (!(Datos.leer(archivo).get(matricula) instanceof Map<?, ?> materias)) {
            return Respuesta.error(s.id(), Estado.PARAMETRO_INVALIDO, "No existe el alumno " + matricula);
        }
        Map<String, Integer> calificaciones = new TreeMap<>();
        materias.forEach((materia, calif) -> calificaciones.put(materia.toString(), Integer.parseInt(calif.toString())));
        double promedio = calificaciones.values().stream().mapToInt(Integer::intValue).average().orElse(0);
        return Respuesta.ok(s.id(), "Alumno " + matricula + ": " + calificaciones + String.format(" | promedio %.1f", promedio));
    }
}
