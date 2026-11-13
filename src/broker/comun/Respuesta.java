package broker.comun;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mensaje que el Broker devuelve al cliente.
 *
 * @param idSolicitud id de la solicitud que se responde
 * @param estado      resultado de la operación (EXITO o el tipo de error)
 * @param resultado   datos devueltos por el proveedor (null si hubo error)
 * @param error       mensaje de error (null si fue exitosa)
 * @param fecha       fecha y hora de la respuesta
 */
public record Respuesta(String idSolicitud, Estado estado, String resultado, String error, LocalDateTime fecha) {

    public enum Estado {
        EXITO,
        SOLICITUD_INVALIDA,      // faltan campos o el mensaje está mal formado
        SERVICIO_DESCONOCIDO,    // no hay proveedor registrado con ese nombre
        OPERACION_DESCONOCIDA,   // el proveedor no ofrece esa operación
        PARAMETRO_INVALIDO,      // el proveedor rechazó los parámetros
        ERROR_PROVEEDOR,         // el proveedor lanzó una excepción
        PROVEEDOR_NO_DISPONIBLE, // timeout / desconexión con el proveedor (nivel 2)
        BROKER_NO_DISPONIBLE     // el cliente no pudo contactar al Broker (nivel 2)
    }

    public static Respuesta ok(String idSolicitud, String resultado) {
        return new Respuesta(idSolicitud, Estado.EXITO, resultado, null, LocalDateTime.now());
    }

    public static Respuesta error(String idSolicitud, Estado estado, String mensaje) {
        return new Respuesta(idSolicitud, estado, null, mensaje, LocalDateTime.now());
    }

    public boolean exitosa() {
        return estado == Estado.EXITO;
    }

    @Override
    public String toString() {
        return "[" + estado + "] " + (exitosa() ? resultado : error) + "  (solicitud " + idSolicitud + ")";
    }

    // ---------------------------------------------------------------- marshalling

    public String aJson() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("idSolicitud", idSolicitud);
        m.put("estado", estado.name());
        m.put("resultado", resultado);
        m.put("error", error);
        m.put("fecha", fecha.toString());
        return Json.escribir(m);
    }

    // -------------------------------------------------------------- unmarshalling

    public static Respuesta desdeJson(String json) {
        Map<String, Object> m = Json.leer(json);
        return new Respuesta(Solicitud.texto(m, "idSolicitud"), Estado.valueOf(Solicitud.texto(m, "estado")),
                Solicitud.texto(m, "resultado"), Solicitud.texto(m, "error"),
                LocalDateTime.parse(Solicitud.texto(m, "fecha")));
    }
}
