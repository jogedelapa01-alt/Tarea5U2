package broker.comun;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mensaje que el cliente envía al Broker.
 *
 * @param id         identificador único de la solicitud
 * @param servicio   nombre lógico del servicio (ej. "calificaciones"), NO la clase concreta
 * @param operacion  operación a ejecutar dentro del servicio (ej. "consultar")
 * @param parametros parámetros de la operación (clave -> valor)
 * @param fecha      fecha y hora de creación
 */
public record Solicitud(String id, String servicio, String operacion,
                        Map<String, String> parametros, LocalDateTime fecha) {

    public Solicitud {
        parametros = parametros == null ? Map.of() : Map.copyOf(parametros);
        if (fecha == null) fecha = LocalDateTime.now();
    }

    /** Crea una solicitud nueva con id aleatorio y la hora actual. */
    public static Solicitud nueva(String servicio, String operacion, Map<String, String> parametros) {
        return new Solicitud(UUID.randomUUID().toString().substring(0, 8), servicio, operacion, parametros, LocalDateTime.now());
    }

    public String parametro(String nombre) {
        return parametros.get(nombre);
    }

    // ---------------------------------------------------------------- marshalling

    public Map<String, Object> aMapa() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("servicio", servicio);
        m.put("operacion", operacion);
        m.put("parametros", parametros);
        m.put("fecha", fecha.toString());
        return m;
    }

    public String aJson() {
        return Json.escribir(aMapa());
    }

    // -------------------------------------------------------------- unmarshalling

    public static Solicitud desdeJson(String json) {
        return desdeMapa(Json.leer(json));
    }

    public static Solicitud desdeMapa(Map<?, ?> m) {
        Map<String, String> params = new LinkedHashMap<>();
        if (m.get("parametros") instanceof Map<?, ?> p) {
            p.forEach((k, v) -> params.put(String.valueOf(k), v == null ? "" : v.toString()));
        }
        String fecha = texto(m, "fecha");
        return new Solicitud(texto(m, "id"), texto(m, "servicio"), texto(m, "operacion"), params,
                fecha == null ? null : LocalDateTime.parse(fecha));
    }

    static String texto(Map<?, ?> m, String clave) {
        Object v = m.get(clave);
        return v == null ? null : v.toString();
    }
}
