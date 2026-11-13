package broker.proveedores;

import broker.comun.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Lee los archivos JSON de la carpeta datos/ que usan los proveedores. */
final class Datos {

    /** Carpeta de datos, relativa a donde se ejecuta el programa (la raíz del proyecto). */
    static final Path CARPETA = Path.of("datos");

    /**
     * Lee y convierte un archivo de datos.
     * Si falta o está mal escrito lanza IllegalStateException; el Broker la convierte en ERROR_PROVEEDOR.
     */
    static Map<String, Object> leer(Path archivo) {
        try {
            return Json.leer(Files.readString(archivo));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + archivo + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JSON mal escrito en " + archivo + ": " + e.getMessage());
        }
    }

    /** Busca una clave sin distinguir mayúsculas ("clean code" encuentra "Clean Code"). */
    static Object buscarSinMayusculas(Map<String, Object> datos, String clave) {
        for (Map.Entry<String, Object> e : datos.entrySet()) {
            if (e.getKey().equalsIgnoreCase(clave)) return e.getValue();
        }
        return null;
    }
}
