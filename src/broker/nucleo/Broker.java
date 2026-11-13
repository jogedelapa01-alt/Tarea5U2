package broker.nucleo;

import broker.comun.Log;
import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.comun.Solicitud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Broker: intermediario entre clientes y proveedores.
 * Recibe la solicitud, identifica servicio y operación, localiza al proveedor en el registro,
 * le reenvía la solicitud y devuelve la respuesta. Ningún error del proveedor llega al
 * cliente como excepción: siempre se convierte en una Respuesta con estado de error.
 *
 * La misma clase se usa en el nivel 1 (proveedores locales) y en el nivel 2
 * (proveedores remotos detrás de ProveedorRemoto): el Broker no nota la diferencia.
 */
public class Broker {

    private static final Path BITACORA = Path.of("bitacora.log");

    private final RegistroServicios registro;

    public Broker(RegistroServicios registro) {
        this.registro = registro;
    }

    public Respuesta procesar(Solicitud solicitud) {
        bitacora("RECIBIDA  " + solicitud.id() + " " + solicitud.servicio() + "." + solicitud.operacion() + " " + solicitud.parametros());
        Respuesta respuesta = despachar(solicitud);
        bitacora("RESPUESTA " + respuesta);
        return respuesta;
    }

    private Respuesta despachar(Solicitud s) {
        // 1. Validar la solicitud
        if (s.id() == null || s.servicio() == null || s.operacion() == null) {
            return Respuesta.error(s.id(), Estado.SOLICITUD_INVALIDA, "La solicitud debe indicar id, servicio y operación");
        }
        // 2. Localizar al proveedor en el registro
        Optional<ProveedorServicio> encontrado = registro.buscar(s.servicio());
        if (encontrado.isEmpty()) {
            return Respuesta.error(s.id(), Estado.SERVICIO_DESCONOCIDO,
                    "Servicio desconocido: '" + s.servicio() + "'. Disponibles: " + registro.servicios());
        }
        ProveedorServicio proveedor = encontrado.get();
        // 3. Identificar la operación
        if (!proveedor.operaciones().contains(s.operacion())) {
            return Respuesta.error(s.id(), Estado.OPERACION_DESCONOCIDA,
                    "El servicio '" + s.servicio() + "' no ofrece '" + s.operacion() + "'. Operaciones: " + proveedor.operaciones());
        }
        // 4. Reenviar y recibir el resultado
        bitacora("ENRUTANDO " + s.id() + " -> " + proveedor.descripcion());
        try {
            Respuesta r = proveedor.ejecutar(s);
            return r != null ? r : Respuesta.error(s.id(), Estado.ERROR_PROVEEDOR, "El proveedor devolvió una respuesta nula");
        } catch (RuntimeException e) {
            return Respuesta.error(s.id(), Estado.ERROR_PROVEEDOR,
                    "Excepción en " + proveedor.descripcion() + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /** Bitácora de solicitudes: consola + archivo bitacora.log. */
    private void bitacora(String mensaje) {
        Log.info("BROKER", mensaje);
        synchronized (BITACORA) {
            try {
                Files.writeString(BITACORA, LocalDateTime.now() + " [" + Thread.currentThread().getName() + "] " + mensaje + System.lineSeparator(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                Log.info("BROKER", "No se pudo escribir la bitácora: " + e.getMessage());
            }
        }
    }
}
