package broker.distribuido;

import broker.comun.Json;
import broker.comun.Red;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;
import broker.nucleo.Broker;
import broker.nucleo.RegistroServicios;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Set;

/**
 * NIVEL 2 — Proceso Broker. Escucha en el puerto 5000 y atiende cada conexión en un hilo.
 *
 * Mensajes que acepta (una línea JSON):
 *   {"tipo":"registro","servicio":"materias","host":"localhost","puerto":"6002","operaciones":"listar"}
 *   {"tipo":"baja",    ...mismos campos que registro...}
 *   {"tipo":"solicitud","solicitud":{"id":..,"servicio":..,"operacion":..,"parametros":{..},"fecha":..}}
 * Siempre responde con una Respuesta en JSON.
 *
 * Arranca con el registro VACÍO: los proveedores se dan de alta solos (registro dinámico).
 */
public class BrokerServidor {

    public static void main(String[] args) throws IOException {
        RegistroServicios registro = new RegistroServicios();
        Broker broker = new Broker(registro); // el mismo Broker del nivel 1
        Red.servir(new ServerSocket(Red.BROKER_PUERTO), "BROKER", linea -> manejar(linea, registro, broker));
    }

    static String manejar(String linea, RegistroServicios registro, Broker broker) {
        Map<String, Object> m;
        try {
            m = Json.leer(linea); // unmarshalling
        } catch (IllegalArgumentException e) {
            return Respuesta.error(null, Estado.SOLICITUD_INVALIDA, "JSON inválido: " + e.getMessage()).aJson();
        }
        String tipo = String.valueOf(m.get("tipo"));
        Respuesta respuesta;
        try {
            respuesta = switch (tipo) {
                case "registro" -> {
                    registro.registrar(texto(m, "servicio"), proveedorRemoto(m));
                    yield Respuesta.ok("registro", "Servicio '" + m.get("servicio") + "' registrado");
                }
                case "baja" -> {
                    boolean ok = registro.eliminar(texto(m, "servicio"), proveedorRemoto(m));
                    yield Respuesta.ok("baja", ok ? "Baja realizada" : "Ese proveedor ya no estaba registrado");
                }
                case "solicitud" -> {
                    if (!(m.get("solicitud") instanceof Map<?, ?> s)) throw new IllegalArgumentException("falta el objeto 'solicitud'");
                    yield broker.procesar(Solicitud.desdeMapa(s));
                }
                default -> Respuesta.error(null, Estado.SOLICITUD_INVALIDA, "Tipo de mensaje desconocido: " + tipo);
            };
        } catch (RuntimeException e) { // campos faltantes, puerto no numérico, fecha mal formada...
            respuesta = Respuesta.error(null, Estado.SOLICITUD_INVALIDA, "Mensaje '" + tipo + "' mal formado: " + e.getMessage());
        }
        return respuesta.aJson(); // marshalling
    }

    private static ProveedorRemoto proveedorRemoto(Map<String, Object> m) {
        return new ProveedorRemoto(texto(m, "host"), Integer.parseInt(texto(m, "puerto")),
                Set.of(texto(m, "operaciones").split(",")));
    }

    private static String texto(Map<String, Object> m, String clave) {
        Object v = m.get(clave);
        if (v == null) throw new IllegalArgumentException("falta el campo '" + clave + "'");
        return v.toString();
    }
}
