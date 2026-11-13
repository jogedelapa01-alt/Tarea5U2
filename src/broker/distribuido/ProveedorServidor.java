package broker.distribuido;

import broker.comun.Json;
import broker.comun.Log;
import broker.comun.ProveedorServicio;
import broker.comun.Red;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;
import broker.proveedores.ServicioBiblioteca;
import broker.proveedores.ServicioBibliotecaDigital;
import broker.proveedores.ServicioCalificaciones;
import broker.proveedores.ServicioMaterias;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * NIVEL 2 — Proceso proveedor. Envuelve a un proveedor del paquete "proveedores" y lo expone por TCP.
 *
 * Uso: ProveedorServidor <tipo> <puerto> [retardoMs]
 *   tipo: calificaciones | materias | biblioteca | biblioteca-digital
 *   retardoMs: demora artificial por solicitud, para provocar el timeout del Broker.
 *
 * Al arrancar se registra en el Broker (registro dinámico) y al cerrarse (Ctrl+C) se da de baja.
 * "biblioteca-digital" se registra con el nombre "biblioteca": sustituye al proveedor actual.
 */
public class ProveedorServidor {

    /** Intentos de registro, uno por segundo: permite arrancar Broker y proveedores al mismo tiempo. */
    private static final int INTENTOS_REGISTRO = 10;

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Uso: ProveedorServidor <calificaciones|materias|biblioteca|biblioteca-digital> <puerto> [retardoMs]");
            return;
        }
        String tipo = args[0];
        int puerto = Integer.parseInt(args[1]);
        long retardo = args.length > 2 ? Long.parseLong(args[2]) : 0;

        ProveedorServicio proveedor = switch (tipo) {
            case "calificaciones" -> new ServicioCalificaciones();
            case "materias" -> new ServicioMaterias();
            case "biblioteca" -> new ServicioBiblioteca();
            case "biblioteca-digital" -> new ServicioBibliotecaDigital();
            default -> throw new IllegalArgumentException("Tipo desconocido: " + tipo);
        };
        String servicio = tipo.equals("biblioteca-digital") ? "biblioteca" : tipo;
        String componente = "PROV-" + tipo;

        // Se abre el puerto ANTES de registrarse, para que el Broker nunca enrute a un puerto cerrado.
        ServerSocket servidor = new ServerSocket(puerto);

        Map<String, Object> alta = new LinkedHashMap<>();
        alta.put("tipo", "registro");
        alta.put("servicio", servicio);
        alta.put("host", "localhost");
        alta.put("puerto", String.valueOf(puerto));
        alta.put("operaciones", String.join(",", proveedor.operaciones()));
        avisarAlBroker(componente, alta, INTENTOS_REGISTRO); // reintenta por si el Broker aún está arrancando

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            alta.put("tipo", "baja");
            avisarAlBroker(componente, alta, 1);
        }));

        Red.servir(servidor, componente, linea -> atender(componente, proveedor, retardo, linea));
    }

    static String atender(String componente, ProveedorServicio proveedor, long retardo, String linea) {
        Solicitud s;
        try {
            s = Solicitud.desdeJson(linea); // unmarshalling
        } catch (RuntimeException e) {
            return Respuesta.error(null, Estado.SOLICITUD_INVALIDA, "Solicitud ilegible: " + e.getMessage()).aJson();
        }
        Log.info(componente, "Procesando " + s.id() + " " + s.operacion() + " " + s.parametros());
        if (retardo > 0) {
            Log.info(componente, "Retardo simulado de " + retardo + " ms");
            try {
                Thread.sleep(retardo);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Respuesta r;
        try {
            r = proveedor.ejecutar(s);
        } catch (RuntimeException e) { // la excepción no puede viajar por la red: se convierte en respuesta de error
            Log.info(componente, "Excepción: " + e);
            r = Respuesta.error(s.id(), Estado.ERROR_PROVEEDOR, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return r.aJson(); // marshalling
    }

    private static void avisarAlBroker(String componente, Map<String, Object> mensaje, int intentos) {
        for (int intento = 1; intento <= intentos; intento++) {
            try {
                Respuesta r = Respuesta.desdeJson(Red.peticion(Red.BROKER_HOST, Red.BROKER_PUERTO, Json.escribir(mensaje), 3_000));
                Log.info(componente, mensaje.get("tipo") + ": " + r);
                return;
            } catch (IOException e) {
                Log.info(componente, "Broker no disponible para '" + mensaje.get("tipo") + "' (intento " + intento + "/" + intentos + "): " + e.getMessage());
                if (intento < intentos) {
                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }
}
