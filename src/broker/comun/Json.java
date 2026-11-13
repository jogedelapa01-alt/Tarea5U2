package broker.comun;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Marshalling / unmarshalling JSON mínimo, sin dependencias externas.
 * Se usa para dos cosas: los mensajes que viajan por la red (Solicitud, Respuesta)
 * y los archivos de datos de la carpeta datos/.
 *
 * leer:     objetos, listas, cadenas, números y null (números y booleanos se devuelven como texto).
 * escribir: objetos, cadenas y null, que es lo que necesitan los mensajes.
 *
 * Un JSON escrito por esta clase nunca contiene saltos de línea (se escapan),
 * por eso se puede enviar "una línea = un mensaje" por el socket.
 */
// ponytail: escribir no admite listas y los números se leen como texto. Cambiar a Gson/Jackson si el protocolo crece.
public final class Json {

    private final String s;
    private int i;

    private Json(String s) {
        this.s = s;
    }

    // ---------------------------------------------------------------- marshalling

    @SuppressWarnings("unchecked")
    public static String escribir(Map<String, ?> objeto) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, ?> e : objeto.entrySet()) {
            if (sb.length() > 1) sb.append(',');
            cadena(sb, e.getKey()).append(':');
            Object v = e.getValue();
            if (v == null) sb.append("null");
            else if (v instanceof Map<?, ?> m) sb.append(escribir((Map<String, ?>) m));
            else cadena(sb, v.toString());
        }
        return sb.append('}').toString();
    }

    private static StringBuilder cadena(StringBuilder sb, String texto) {
        sb.append('"');
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append('"');
    }

    // -------------------------------------------------------------- unmarshalling

    /** @throws IllegalArgumentException si el texto no es un objeto JSON válido. */
    public static Map<String, Object> leer(String json) {
        if (json == null) throw new IllegalArgumentException("JSON nulo");
        Json p = new Json(json);
        try {
            p.espacios();
            Map<String, Object> m = p.objeto();
            p.espacios();
            if (p.i != json.length()) throw new IllegalArgumentException("caracteres sobrantes en posición " + p.i);
            return m;
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("JSON incompleto");
        }
    }

    private Map<String, Object> objeto() {
        esperar('{');
        Map<String, Object> m = new LinkedHashMap<>();
        espacios();
        if (s.charAt(i) == '}') {
            i++;
            return m;
        }
        while (true) {
            espacios();
            String clave = cadena();
            espacios();
            esperar(':');
            espacios();
            m.put(clave, valor());
            espacios();
            if (s.charAt(i) == ',') {
                i++;
                continue;
            }
            esperar('}');
            return m;
        }
    }

    private List<Object> lista() {
        esperar('[');
        List<Object> l = new ArrayList<>();
        espacios();
        if (s.charAt(i) == ']') {
            i++;
            return l;
        }
        while (true) {
            espacios();
            l.add(valor());
            espacios();
            if (s.charAt(i) == ',') {
                i++;
                continue;
            }
            esperar(']');
            return l;
        }
    }

    private Object valor() {
        char c = s.charAt(i);
        if (c == '{') return objeto();
        if (c == '[') return lista();
        if (c == '"') return cadena();
        if (s.startsWith("null", i)) {
            i += 4;
            return null;
        }
        int inicio = i; // números y booleanos se conservan como texto
        while (i < s.length() && ",}] \t\r\n".indexOf(s.charAt(i)) < 0) i++;
        if (inicio == i) throw new IllegalArgumentException("se esperaba un valor en posición " + i);
        return s.substring(inicio, i);
    }

    private String cadena() {
        esperar('"');
        StringBuilder sb = new StringBuilder();
        for (char c = s.charAt(i++); c != '"'; c = s.charAt(i++)) {
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            char e = s.charAt(i++);
            switch (e) {
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> {
                    sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                    i += 4;
                }
                default -> sb.append(e); // \" \\ \/
            }
        }
        return sb.toString();
    }

    private void espacios() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
    }

    private void esperar(char c) {
        if (s.charAt(i) != c) throw new IllegalArgumentException("se esperaba '" + c + "' en posición " + i);
        i++;
    }
}
