package broker.cliente;

import broker.comun.Log;
import broker.comun.Respuesta;
import broker.comun.Solicitud;

import java.util.Map;
import java.util.function.Function;

/**
 * Cliente: construye solicitudes, las envía al Broker y muestra las respuestas.
 *
 * Fíjate en los imports: solo conoce Solicitud y Respuesta. No importa ningún proveedor
 * ni el registro; identifica los servicios por su NOMBRE lógico ("calificaciones", ...).
 */
public class Cliente {

    /** Canal hacia el Broker: el Broker local (broker::procesar) o el proxy de red del nivel 2. */
    private final Function<Solicitud, Respuesta> broker;

    public Cliente(Function<Solicitud, Respuesta> broker) {
        this.broker = broker;
    }

    public Respuesta solicitar(String servicio, String operacion, Map<String, String> parametros) {
        Solicitud solicitud = Solicitud.nueva(servicio, operacion, parametros);
        Log.info("CLIENTE", "Enviando " + solicitud.id() + " -> " + servicio + "." + operacion + " " + parametros);
        Respuesta respuesta = broker.apply(solicitud);
        Log.info("CLIENTE", "Recibido " + respuesta);
        return respuesta;
    }
}
