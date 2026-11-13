package broker.distribuido;

import broker.comun.Red;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.function.Function;

/** Arranca un servidor TCP en un hilo aparte para los tests. Se apaga cerrando el ServerSocket. */
final class ServidorDePrueba {

    static ServerSocket iniciar(int puerto, Function<String, String> manejador) throws IOException {
        ServerSocket servidor = new ServerSocket(puerto); // 0 = puerto libre elegido por el sistema
        Thread hilo = new Thread(() -> {
            try {
                Red.servir(servidor, "TEST", manejador);
            } catch (IOException cerrado) {
                // el test cerró el servidor
            }
        });
        hilo.setDaemon(true);
        hilo.start();
        return servidor;
    }

    static int puertoSinNadie() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }
}
