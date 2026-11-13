package broker.comun;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Mensajes de consola con hora, componente e hilo (el hilo evidencia la atención concurrente). */
public final class Log {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static synchronized void info(String componente, String mensaje) {
        System.out.printf("%s [%-14s] [%s] %s%n", LocalTime.now().format(HORA), componente,
                Thread.currentThread().getName(), mensaje);
    }
}
