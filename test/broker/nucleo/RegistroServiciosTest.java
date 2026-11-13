package broker.nucleo;

import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegistroServiciosTest {

    private final RegistroServicios registro = new RegistroServicios();
    private final ProveedorServicio uno = new ProveedorFalso(s -> Respuesta.ok(s.id(), "uno"));
    private final ProveedorServicio dos = new ProveedorFalso(s -> Respuesta.ok(s.id(), "dos"));

    @Test
    void registrarYBuscar() {
        registro.registrar("a", uno);
        assertSame(uno, registro.buscar("a").orElseThrow());
    }

    @Test
    void registrarDosVecesSustituyeAlProveedor() {
        registro.registrar("a", uno);
        registro.registrar("a", dos);
        assertSame(dos, registro.buscar("a").orElseThrow());
        assertEquals(1, registro.servicios().size());
    }

    @Test
    void registrarRechazaNombreVacioOProveedorNulo() {
        assertThrows(IllegalArgumentException.class, () -> registro.registrar("", uno));
        assertThrows(IllegalArgumentException.class, () -> registro.registrar(null, uno));
        assertThrows(IllegalArgumentException.class, () -> registro.registrar("a", null));
    }

    @Test
    void buscarServicioDesconocidoONuloDevuelveVacio() {
        assertTrue(registro.buscar("nada").isEmpty());
        assertTrue(registro.buscar(null).isEmpty());
    }

    @Test
    void eliminarSoloBorraSiEsElMismoProveedor() {
        registro.registrar("a", dos);
        assertFalse(registro.eliminar("a", uno), "un proveedor viejo no debe borrar al nuevo");
        assertTrue(registro.buscar("a").isPresent());
        assertTrue(registro.eliminar("a", dos));
        assertTrue(registro.buscar("a").isEmpty());
    }

    @Test
    void serviciosDevuelveLosNombresOrdenados() {
        registro.registrar("materias", uno);
        registro.registrar("biblioteca", dos);
        assertEquals(List.of("biblioteca", "materias"), List.copyOf(registro.servicios()));
    }
}
