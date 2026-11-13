package broker.distribuido;

import broker.cliente.Cliente;
import broker.comun.Json;
import broker.comun.Red;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * NIVEL 2 — Proceso cliente. Usa el MISMO Cliente del nivel 1; solo cambia el canal:
 * en vez de llamar al Broker en memoria, envía la solicitud por socket.
 *
 * Uso:
 *   ClienteRemoto                                  -> ejecuta la demostración completa
 *   ClienteRemoto <servicio> <operacion> [k=v ...] -> envía una sola solicitud
 */
public class ClienteRemoto {

    /** Debe ser mayor que el peor caso del Broker (3 intentos x 3 s de timeout). */
    private static final int TIMEOUT_MS = 15_000;

    /** Proxy del lado cliente: marshalling, envío por socket y unmarshalling. */
    static Respuesta enviarAlBroker(Solicitud s) {
        String sobre = Json.escribir(Map.of("tipo", "solicitud", "solicitud", s.aMapa()));
        try {
            return Respuesta.desdeJson(Red.peticion(Red.BROKER_HOST, Red.BROKER_PUERTO, sobre, TIMEOUT_MS));
        } catch (IOException e) {
            return Respuesta.error(s.id(), Estado.BROKER_NO_DISPONIBLE, "No se pudo contactar al Broker: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        Cliente cliente = new Cliente(ClienteRemoto::enviarAlBroker);

        if (args.length >= 2) {
            Map<String, String> params = new LinkedHashMap<>();
            for (int i = 2; i < args.length; i++) {
                String[] kv = args[i].split("=", 2);
                params.put(kv[0], kv.length > 1 ? kv[1] : "");
            }
            cliente.solicitar(args[0], args[1], params);
            return;
        }

        System.out.println("\n=== Una operación por servicio ===");
        cliente.solicitar("calificaciones", "consultar", Map.of("matricula", "A002"));
        cliente.solicitar("materias", "listar", Map.of("semestre", "1"));
        cliente.solicitar("biblioteca", "disponibilidad", Map.of("titulo", "Patrones de diseño"));

        System.out.println("\n=== Errores ===");
        cliente.solicitar("cafeteria", "menu", Map.of());
        cliente.solicitar("calificaciones", "consultar", Map.of("matricula", "123"));
        cliente.solicitar("biblioteca", "disponibilidad", Map.of("titulo", "Clean Code", "simularFalla", "true"));

        System.out.println("\n=== 8 solicitudes concurrentes (observa los hilos en la consola del Broker) ===");
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<Respuesta>> pendientes = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            String semestre = String.valueOf(i % 3 + 1);
            pendientes.add(pool.submit(() -> cliente.solicitar("materias", "listar", Map.of("semestre", semestre))));
        }
        long exitosas = 0;
        for (Future<Respuesta> f : pendientes) if (f.get().exitosa()) exitosas++;
        pool.shutdown();
        System.out.println("Concurrentes exitosas: " + exitosas + "/8");
    }
}
