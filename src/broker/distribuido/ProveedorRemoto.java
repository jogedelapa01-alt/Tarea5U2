package broker.distribuido;

import broker.comun.Log;
import broker.comun.ProveedorServicio;
import broker.comun.Red;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;

import java.io.IOException;
import java.util.Set;

/**
 * Proxy del lado del Broker hacia un proveedor que corre en otro proceso.
 * Implementa la misma interfaz ProveedorServicio, así el Broker lo usa igual que a uno local.
 *
 * Hace marshalling de la Solicitud a JSON, la envía por socket con timeout y, si falla
 * (timeout, conexión rechazada o cortada), reintenta un número limitado de veces.
 * Es un record: dos ProveedorRemoto con mismo host/puerto/operaciones son iguales,
 * lo que permite dar de baja exactamente al proveedor que lo pide.
 */
public record ProveedorRemoto(String host, int puerto, Set<String> operaciones) implements ProveedorServicio {

    static final int TIMEOUT_MS = 3_000;
    static final int INTENTOS = 3;
    private static final int ESPERA_ENTRE_INTENTOS_MS = 300;

    @Override
    public Respuesta ejecutar(Solicitud s) {
        IOException ultimo = null;
        for (int intento = 1; intento <= INTENTOS; intento++) {
            try {
                String json = Red.peticion(host, puerto, s.aJson(), TIMEOUT_MS); // marshalling + envío
                return Respuesta.desdeJson(json);                                // unmarshalling
            } catch (IOException e) {
                ultimo = e;
                Log.info("BROKER", "Intento " + intento + "/" + INTENTOS + " con " + descripcion() + " falló: " + e.getMessage());
                dormir(ESPERA_ENTRE_INTENTOS_MS);
            }
        }
        return Respuesta.error(s.id(), Estado.PROVEEDOR_NO_DISPONIBLE,
                descripcion() + " no respondió tras " + INTENTOS + " intentos (" + ultimo.getMessage() + ")");
    }

    @Override
    public String descripcion() {
        return "remoto@" + host + ":" + puerto;
    }

    private static void dormir(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
