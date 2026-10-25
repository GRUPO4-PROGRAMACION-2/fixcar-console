package taller.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ClienteTest {
    @Test
    void creaClienteConDatosValidosYLimpiaEspacios() {
        Cliente cliente = new Cliente("  Ana Pérez ", " DUI-1 ", " 7000-1111 ", " ana@correo.com ", "Centro");

        assertEquals("Ana Pérez", cliente.getNombre());
        assertEquals("DUI-1", cliente.getDocumento());
        assertEquals("ana@correo.com", cliente.getCorreo());
        assertEquals("Cliente", cliente.getTipo());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "sin-arroba", "@correo.com", "ana@", "a@@b.com", "a@b@c.com"})
    void rechazaCorreosInvalidos(String correo) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new Cliente("Ana", "DUI-1", "7000", correo, "Centro"));
        assertEquals("El correo no es válido", error.getMessage());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void rechazaNombreVacio(String nombre) {
        assertThrows(IllegalArgumentException.class,
                () -> new Cliente(nombre, "DUI-1", "7000", "ana@correo.com", "Centro"));
    }

    @Test
    void rechazaDocumentoYTelefonoVacios() {
        assertThrows(IllegalArgumentException.class,
                () -> new Cliente("Ana", " ", "7000", "ana@correo.com", "Centro"));
        assertThrows(IllegalArgumentException.class,
                () -> new Cliente("Ana", "DUI-1", "", "ana@correo.com", "Centro"));
    }
}
