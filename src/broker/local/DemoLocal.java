package broker.local;

import broker.cliente.Cliente;
import broker.comun.Respuesta;
import broker.comun.Respuesta.Estado;
import broker.nucleo.Broker;
import broker.nucleo.RegistroServicios;
import broker.proveedores.ServicioBiblioteca;
import broker.proveedores.ServicioBibliotecaDigital;
import broker.proveedores.ServicioCalificaciones;
import broker.proveedores.ServicioMaterias;

import java.util.Map;

/**
 * NIVEL 1 — Broker local: todos los componentes en la misma aplicación.
 * Ejecuta las 8 pruebas requeridas y termina con código 1 si alguna falla.
 *
 * Esta clase es el punto de arranque (composition root): es el ÚNICO lugar que conoce
 * las clases concretas de los proveedores. El Cliente nunca las ve.
 */
public class DemoLocal {

    private static int fallidas = 0;

    public static void main(String[] args) {
        // 1. Los proveedores se registran
        titulo("Prueba 1: Registro correcto de proveedores");
        RegistroServicios registro = new RegistroServicios();
        registro.registrar("calificaciones", new ServicioCalificaciones());
        registro.registrar("materias", new ServicioMaterias());
        registro.registrar("biblioteca", new ServicioBiblioteca());
        verificar("3 servicios registrados " + registro.servicios(), registro.servicios().size() == 3);

        titulo("Prueba 2: Localización de un servicio");
        verificar("'materias' se localiza", registro.buscar("materias").isPresent());
        verificar("'cafeteria' no se localiza", registro.buscar("cafeteria").isEmpty());

        Broker broker = new Broker(registro);
        Cliente cliente = new Cliente(broker::procesar); // el cliente solo recibe el canal al Broker

        titulo("Pruebas 3 y 4: Ejecución de una operación por servicio y respuesta exitosa");
        esperar(cliente.solicitar("calificaciones", "consultar", Map.of("matricula", "A001")), Estado.EXITO);
        esperar(cliente.solicitar("materias", "listar", Map.of("semestre", "3")), Estado.EXITO);
        esperar(cliente.solicitar("biblioteca", "disponibilidad", Map.of("titulo", "Clean Code")), Estado.EXITO);

        titulo("Prueba 5: Servicio desconocido");
        esperar(cliente.solicitar("cafeteria", "menu", Map.of()), Estado.SERVICIO_DESCONOCIDO);
        esperar(cliente.solicitar("materias", "borrar", Map.of()), Estado.OPERACION_DESCONOCIDA);

        titulo("Prueba 6: Parámetro inválido");
        esperar(cliente.solicitar("calificaciones", "consultar", Map.of("matricula", "xyz")), Estado.PARAMETRO_INVALIDO);
        esperar(cliente.solicitar("materias", "listar", Map.of("semestre", "tres")), Estado.PARAMETRO_INVALIDO);

        titulo("Prueba 7: Excepción producida por un proveedor");
        esperar(cliente.solicitar("biblioteca", "disponibilidad", Map.of("titulo", "Clean Code", "simularFalla", "true")), Estado.ERROR_PROVEEDOR);

        titulo("Prueba 8: Cambio de proveedor sin modificar el cliente");
        registro.registrar("biblioteca", new ServicioBibliotecaDigital()); // sustitución en caliente
        Respuesta r = cliente.solicitar("biblioteca", "disponibilidad", Map.of("titulo", "Clean Code")); // misma llamada que antes
        verificar("respuesta exitosa del nuevo proveedor", r.exitosa() && r.resultado().contains("digital"));

        System.out.println();
        System.out.println(fallidas == 0 ? "TODAS LAS PRUEBAS PASARON" : fallidas + " PRUEBA(S) FALLARON");
        System.exit(fallidas == 0 ? 0 : 1);
    }

    private static void esperar(Respuesta r, Estado esperado) {
        verificar("estado " + r.estado() + " (esperado " + esperado + ")", r.estado() == esperado);
    }

    private static void verificar(String descripcion, boolean ok) {
        if (!ok) fallidas++;
        System.out.println("   " + (ok ? "[PASÓ] " : "[FALLÓ] ") + descripcion);
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }
}
