package broker.comun;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.*;

class RedTest {

    /** Levanta un servidor eco en un puerto libre, en un hilo aparte. */
    private static ServerSocket servidorEco() throws IOException {
        ServerSocket servidor = new ServerSocket(0); // 0 = el sistema elige un puerto libre
        Thread hilo = new Thread(() -> {
            try {
                Red.servir(servidor, "TEST", linea -> "eco:" + linea);
            } catch (IOException cerrado) {
                // el test cerró el servidor: fin del hilo
            }
        });
        hilo.setDaemon(true);
        hilo.start();
        return servidor;
    }

    @Test
    void peticionRecibeLaRespuestaDeServir() throws IOException {
        try (ServerSocket servidor = servidorEco()) {
            assertEquals("eco:hola", Red.peticion("localhost", servidor.getLocalPort(), "hola", 2_000));
        }
    }

    @Test
    void servirAtiendeVariasPeticiones() throws IOException {
        try (ServerSocket servidor = servidorEco()) {
            for (int i = 0; i < 5; i++) {
                assertEquals("eco:" + i, Red.peticion("localhost", servidor.getLocalPort(), String.valueOf(i), 2_000));
            }
        }
    }

    @Test
    void peticionAPuertoCerradoLanzaIOException() throws IOException {
        int puertoLibre;
        try (ServerSocket s = new ServerSocket(0)) {
            puertoLibre = s.getLocalPort(); // se cierra al salir: nadie escucha ahí
        }
        assertThrows(IOException.class, () -> Red.peticion("localhost", puertoLibre, "hola", 1_000));
    }
}
