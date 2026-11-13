package broker.comun;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LogTest {

    @Test
    void infoImprimeComponenteHiloYMensaje() {
        PrintStream original = System.out;
        ByteArrayOutputStream capturado = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturado));
        try {
            Log.info("BROKER", "hola");
        } finally {
            System.setOut(original);
        }
        String linea = capturado.toString();
        assertTrue(linea.contains("BROKER"));
        assertTrue(linea.contains("hola"));
        assertTrue(linea.contains(Thread.currentThread().getName()));
    }
}
