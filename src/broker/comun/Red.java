package broker.comun;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Comunicación TCP del nivel 2.
 * Protocolo: por cada conexión se envía UNA línea con un JSON y se recibe UNA línea con el JSON de respuesta.
 */
public final class Red {

    public static final String BROKER_HOST = "localhost";
    public static final int BROKER_PUERTO = 5000;

    /** Tiempo máximo que un servidor espera a que el otro extremo envíe su línea. */
    private static final int TIMEOUT_LECTURA_MS = 10_000;
    private static final int HILOS_POR_SERVIDOR = 16;

    /**
     * Lado cliente: abre conexión, envía una línea y espera la respuesta.
     *
     * @throws SocketTimeoutException si no hay respuesta en {@code timeoutMs}
     * @throws IOException            si no se puede conectar o se corta la conexión
     */
    public static String peticion(String host, int puerto, String linea, int timeoutMs) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, puerto), timeoutMs);
            socket.setSoTimeout(timeoutMs);
            PrintWriter salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
            salida.println(linea);
            String respuesta = entrada.readLine();
            if (respuesta == null) throw new EOFException("el otro extremo cerró la conexión");
            return respuesta;
        }
    }

    /**
     * Lado servidor: acepta conexiones indefinidamente y atiende cada una en un hilo del pool,
     * así varias solicitudes se procesan en paralelo.
     */
    public static void servir(ServerSocket servidor, String componente, Function<String, String> manejador) throws IOException {
        ExecutorService hilos = Executors.newFixedThreadPool(HILOS_POR_SERVIDOR);
        Log.info(componente, "Escuchando en puerto " + servidor.getLocalPort());
        while (true) {
            Socket conexion = servidor.accept();
            hilos.submit(() -> atender(conexion, componente, manejador));
        }
    }

    private static void atender(Socket socket, String componente, Function<String, String> manejador) {
        try (socket) {
            socket.setSoTimeout(TIMEOUT_LECTURA_MS);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));
            PrintWriter salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), UTF_8), true);
            String linea = entrada.readLine();
            if (linea == null) return; // el cliente se desconectó sin enviar nada
            String respuesta;
            try {
                respuesta = manejador.apply(linea);
            } catch (RuntimeException e) { // red de seguridad: el servidor nunca se cae por un mensaje
                respuesta = Respuesta.error(null, Respuesta.Estado.SOLICITUD_INVALIDA, "Mensaje no procesable: " + e).aJson();
            }
            salida.println(respuesta);
        } catch (SocketTimeoutException e) {
            Log.info(componente, "Timeout: el cliente no envió datos, conexión cerrada");
        } catch (IOException e) {
            Log.info(componente, "Desconexión: " + e.getMessage());
        }
    }
}
