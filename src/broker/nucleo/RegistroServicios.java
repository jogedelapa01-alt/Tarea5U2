package broker.nucleo;

import broker.comun.Log;
import broker.comun.ProveedorServicio;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro de servicios: asocia un nombre lógico de servicio con el proveedor que lo atiende.
 * Es thread-safe (ConcurrentHashMap) porque en el nivel 2 los proveedores se registran
 * mientras el Broker atiende solicitudes en otros hilos.
 */
public class RegistroServicios {

    private final Map<String, ProveedorServicio> proveedores = new ConcurrentHashMap<>();

    /**
     * Registra un proveedor. Si ya existía uno con ese nombre, lo SUSTITUYE:
     * los clientes siguen usando el mismo nombre y no se enteran del cambio.
     */
    public void registrar(String servicio, ProveedorServicio proveedor) {
        if (servicio == null || servicio.isBlank()) throw new IllegalArgumentException("Nombre de servicio vacío");
        if (proveedor == null) throw new IllegalArgumentException("Proveedor nulo");
        ProveedorServicio anterior = proveedores.put(servicio, proveedor);
        if (anterior == null) {
            Log.info("REGISTRO", "Registrado '" + servicio + "' -> " + proveedor.descripcion() + " " + proveedor.operaciones());
        } else {
            Log.info("REGISTRO", "Sustituido '" + servicio + "': " + anterior.descripcion() + " -> " + proveedor.descripcion());
        }
    }

    /** Busca el proveedor de un servicio; vacío si el servicio es desconocido. */
    public Optional<ProveedorServicio> buscar(String servicio) {
        return servicio == null ? Optional.empty() : Optional.ofNullable(proveedores.get(servicio));
    }

    /**
     * Da de baja un servicio solo si sigue atendido por ese mismo proveedor.
     * Así, si un proveedor viejo se apaga después de haber sido sustituido, no borra al nuevo.
     */
    public boolean eliminar(String servicio, ProveedorServicio proveedor) {
        boolean eliminado = proveedores.remove(servicio, proveedor);
        if (eliminado) Log.info("REGISTRO", "Baja de '" + servicio + "' (" + proveedor.descripcion() + ")");
        return eliminado;
    }

    public Set<String> servicios() {
        return new TreeSet<>(proveedores.keySet());
    }
}
