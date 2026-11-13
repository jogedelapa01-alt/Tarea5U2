# Actividad 5: Broker de Servicios Universitarios (Java)

Este prototipo implementa el patrón arquitectónico **Broker** en sus dos niveles:

- **Nivel 1 (local):** el cliente, el Broker y los proveedores corren dentro de la misma aplicación.
- **Nivel 2 (distribuido):** cada componente corre en un proceso distinto. Se comunican por **sockets TCP** con mensajes **JSON**, cada conexión se atiende en un **hilo**, y hay **timeouts**, **reintentos limitados**, **registro dinámico** y **bitácora**.

Los servicios son: consulta de calificaciones, consulta de materias y disponibilidad de libros.

El programa no tiene dependencias externas: compila con `javac` en Java 17 o superior (se probó con JDK 24). El JSON se procesa con una clase propia (`Json.java`). Maven (`pom.xml`) solo se usa para correr los tests JUnit.

## Estructura del código

```
src/broker/
├── comun/                      Contrato compartido por todos los componentes
│   ├── Solicitud.java          id, servicio, operación, parámetros, fecha y hora (+ JSON)
│   ├── Respuesta.java          idSolicitud, estado, resultado, error, fecha y hora (+ JSON)
│   ├── ProveedorServicio.java  Interfaz común de los proveedores
│   ├── Json.java               Marshalling y unmarshalling JSON
│   ├── Red.java                Sockets TCP: petición con timeout y servidor con pool de hilos
│   └── Log.java                Mensajes en consola con hora, componente e hilo
├── nucleo/
│   ├── Broker.java             Recibe, valida, localiza, reenvía, gestiona errores y escribe la bitácora
│   └── RegistroServicios.java  Registrar, buscar, sustituir y dar de baja proveedores
├── proveedores/
│   ├── Datos.java                      Lee los archivos JSON de datos/
│   ├── ServicioCalificaciones.java     consultar(matricula)
│   ├── ServicioMaterias.java           listar(semestre)
│   ├── ServicioBiblioteca.java         disponibilidad(titulo)  [simularFalla=true lanza una excepción]
│   └── ServicioBibliotecaDigital.java  Proveedor alternativo para demostrar la sustitución
├── cliente/
│   └── Cliente.java            Construye solicitudes y muestra respuestas. No conoce a los proveedores.
├── local/
│   └── DemoLocal.java          NIVEL 1: ejecuta las 8 pruebas requeridas
└── distribuido/
    ├── BrokerServidor.java     NIVEL 2: proceso Broker (puerto 5000)
    ├── ProveedorServidor.java  NIVEL 2: proceso proveedor (se registra solo en el Broker)
    ├── ProveedorRemoto.java    Proxy del Broker hacia un proveedor remoto (timeout + reintentos)
    └── ClienteRemoto.java      NIVEL 2: proceso cliente
```

```
datos/                          Datos de prueba que usan los proveedores (edítalos para probar otros casos)
├── calificaciones.json         { "A001": { "Materia": calificación } }
├── materias.json               { "1": ["Materia", ...] }
├── biblioteca.json             { "Título": ejemplares }
└── biblioteca-digital.json     { "Título": licencias }
test/                           Tests JUnit, uno por clase
pom.xml                         Solo para correr los tests con Maven
```

Los proveedores releen su archivo en cada solicitud: si cambias un dato con el programa corriendo, la siguiente consulta ya lo muestra. Ejecuta siempre desde la raíz del proyecto, porque la carpeta `datos/` se busca ahí.

### Correspondencia con el patrón

| Elemento del patrón | Clase |
|---|---|
| Cliente | `Cliente` (usa `ClienteRemoto.enviarAlBroker` como proxy del lado cliente en el nivel 2) |
| Broker | `Broker`, expuesto en red por `BrokerServidor` |
| Registro de servicios | `RegistroServicios` |
| Proveedores | `ServicioCalificaciones`, `ServicioMaterias`, `ServicioBiblioteca`, `ServicioBibliotecaDigital` |
| Proxy del lado servidor | `ProveedorRemoto` (el Broker lo usa como si fuera un proveedor local) |
| Solicitud / Respuesta | `Solicitud`, `Respuesta` |
| Comunicación | `Red` + `Json` |

El cliente no conoce a los proveedores: `Cliente.java` solo importa `Solicitud` y `Respuesta`, y pide los servicios por su nombre lógico. El único lugar que instancia proveedores concretos es el punto de arranque (`DemoLocal`, o `ProveedorServidor` en el nivel 2). El mismo `Broker` y el mismo `Cliente` funcionan en ambos niveles; lo único que cambia es el canal de comunicación.

## Ejecutar desde IntelliJ IDEA

Las configuraciones están en la carpeta `.run/` y aparecen solas en el menú de configuraciones de arriba (junto al botón ▶).

1. **Nivel 1:** elige `0. Nivel 1 - Demo local` y presiona ▶.
2. **Nivel 2:** elige `Nivel 2 - Broker y 3 proveedores` y presiona ▶ (arranca las 4 piezas, cada una en su pestaña del panel Run). Después elige `5. Cliente (demostración)` y presiona ▶.
3. **Sustitución:** `6. Sustituir por biblioteca digital`, y vuelve a ejecutar `5. Cliente`.
4. **Timeout:** `7. Proveedor materias lento (timeout)`, y vuelve a ejecutar `5. Cliente`.
5. **Tests:** clic derecho en la carpeta `test` → **Run 'All Tests'**, con el Broker apagado.

Para detener una pieza, usa el cuadro rojo ■ de su pestaña. Si no ves las configuraciones, usa **File → Reload All from Disk**.

## Ejecutar desde NetBeans

1. **File → Open Project** y elige la carpeta `ProyectoArqui` (NetBeans la reconoce por el `pom.xml`).
2. **Nivel 1:** F6 (Run Project) ejecuta `DemoLocal`.
3. **Nivel 2:** clic derecho en el proyecto → **Run Maven**, y lanza en orden: `1. Broker`, `2.`, `3.` y `4.` (los proveedores), y luego `5. Cliente`. Cada uno abre su propia pestaña en Output.
4. **Sustitución:** `6. Sustituir biblioteca por la digital`, y vuelve a lanzar `5. Cliente`.
5. **Timeout:** `7. Proveedor materias lento`, y vuelve a lanzar `5. Cliente`.
6. **Tests:** Alt+F6 (Test Project), con el Broker apagado.

Para detener una pieza, usa el cuadro rojo de su pestaña en Output. Las acciones están definidas en `nbactions.xml`.

## Compilar

PowerShell:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
```

Bash o Git Bash:

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

## Tests JUnit

En `test/` hay una clase de test por cada clase del proyecto (78 tests con JUnit 5). Los tests de proveedores crean sus propios JSON temporales, así que editar `datos/` no los rompe. Se ejecutan con Maven:

```bash
mvn test
```

Si `mvn` no está en el PATH, usa el que trae la extensión de Java de VS Code o el botón de tests de tu IDE. El Broker real no debe estar corriendo mientras corren los tests, porque `ClienteRemotoTest` usa el puerto 5000.

Los `main` de demostración (`DemoLocal`, `BrokerServidor`, `ProveedorServidor`, `ClienteRemoto`) no tienen test unitario; se prueban ejecutándolos.

## Nivel 1: ejecutar la demostración

```bash
java -cp out broker.local.DemoLocal
```

El programa imprime `[PASÓ]` o `[FALLÓ]` en cada verificación y termina con código 1 si alguna falla.

| # | Prueba requerida | Qué se hace |
|---|---|---|
| 1 | Registro correcto de proveedores | Se registran 3 servicios |
| 2 | Localización de un servicio | `buscar("materias")` lo encuentra; `buscar("cafeteria")` no |
| 3 | Ejecución de una operación | Una operación por cada servicio |
| 4 | Respuesta exitosa | Las tres devuelven `EXITO` |
| 5 | Servicio desconocido | `cafeteria` da `SERVICIO_DESCONOCIDO`; `materias.borrar` da `OPERACION_DESCONOCIDA` |
| 6 | Parámetro inválido | `matricula=xyz` y `semestre=tres` dan `PARAMETRO_INVALIDO` |
| 7 | Excepción de un proveedor | `simularFalla=true` da `ERROR_PROVEEDOR` (el Broker atrapa la excepción) |
| 8 | Cambio de proveedor sin modificar el cliente | Se registra `ServicioBibliotecaDigital` como "biblioteca" y la misma llamada devuelve el resultado nuevo |

Cada solicitud también queda registrada en `bitacora.log`.

## Nivel 2: ejecución distribuida

Para lanzar todo de una vez, este script compila, abre el Broker y los 3 proveedores en ventanas separadas y ejecuta el cliente:

```powershell
.\ejecutar-distribuido.ps1
```

Para hacerlo a mano, usa una terminal por proceso y respeta este orden:

```bash
java -cp out broker.distribuido.BrokerServidor
```

```bash
java -cp out broker.distribuido.ProveedorServidor calificaciones 6001
```

```bash
java -cp out broker.distribuido.ProveedorServidor materias 6002
```

```bash
java -cp out broker.distribuido.ProveedorServidor biblioteca 6003
```

```bash
java -cp out broker.distribuido.ClienteRemoto
```

Sin argumentos, `ClienteRemoto` ejecuta una operación por servicio, los casos de error y 8 solicitudes concurrentes. En la consola del Broker se ve cómo las atienden hilos distintos (`pool-1-thread-N`). Con argumentos envía una sola solicitud:

```bash
java -cp out broker.distribuido.ClienteRemoto biblioteca disponibilidad "titulo=Clean Code"
```

### Escenarios para la demostración

| Escenario | Cómo provocarlo | Resultado esperado |
|---|---|---|
| Registro dinámico | Arrancar un proveedor con el Broker ya encendido | El Broker muestra `Registrado 'x' -> remoto@localhost:puerto` |
| Sustitución sin tocar el cliente | `ProveedorServidor biblioteca-digital 6004` | El Broker muestra `Sustituido 'biblioteca'`; la misma consulta devuelve `[Biblioteca digital] ...` |
| Timeout y reintentos | `ProveedorServidor materias 6005 5000` (tarda 5 s y reemplaza a materias) | 3 intentos de 3 s cada uno y luego `PROVEEDOR_NO_DISPONIBLE ... (Read timed out)` |
| Desconexión de un proveedor | Cerrar la ventana de un proveedor y consultarlo | 3 intentos y luego `PROVEEDOR_NO_DISPONIBLE ... (Connection refused)` |
| Baja ordenada | Ctrl+C en un proveedor | Envía `baja` y el servicio pasa a ser `SERVICIO_DESCONOCIDO` |
| Broker caído | Cerrar el Broker y ejecutar el cliente | El cliente responde `BROKER_NO_DISPONIBLE`, sin excepción |

### Protocolo

Por cada conexión se envía una línea JSON y se recibe una línea JSON.

```json
{"tipo":"registro","servicio":"materias","host":"localhost","puerto":"6002","operaciones":"listar"}
{"tipo":"solicitud","solicitud":{"id":"2cf2c615","servicio":"materias","operacion":"listar","parametros":{"semestre":"1"},"fecha":"2026-11-10T18:06:52.28"}}
{"idSolicitud":"2cf2c615","estado":"EXITO","resultado":"Semestre 1: [...]","error":null,"fecha":"2026-11-10T18:06:52.29"}
```

**Dónde ocurre el marshalling y el unmarshalling:**

- **Cliente:** `ClienteRemoto.enviarAlBroker`
- **Broker:** `BrokerServidor.manejar`, y `ProveedorRemoto.ejecutar` en el tramo hacia el proveedor
- **Proveedor:** `ProveedorServidor.atender`

**Tiempos:**

| Tramo | Valor |
|---|---|
| Timeout del Broker hacia el proveedor | 3 s, con 3 intentos |
| Timeout del cliente | 15 s |
| Lectura en los servidores | 10 s |
