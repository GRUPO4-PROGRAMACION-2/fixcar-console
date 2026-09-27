# T10 — Consola y arranque
- **Prioridad:** 10
- **Estado:** En Progreso
- **Responsable:** [Edgar Gomez / edgargoguz-cmd]
- **Rama:** `feature/consola`
- **Depende de:** T01 y T09

## Trabajo de desarrollo
- Crear `ConsoleUI` con opciones para registrar clientes/vehículos, crear/listar
  órdenes y cambiar estados.
- Leer entradas con `Scanner` y llamar a `TallerController`.
- Crear `Main.java` para iniciar el controller, la consola y el procesador concurrente.
- Manejar entradas inválidas y permitir salir normalmente.

## Archivos propios
- `src/main/java/taller/ui/ConsoleUI.java`
- `src/main/java/taller/Main.java`

## Terminado cuando
- El trabajador realiza las funciones mínimas desde la consola.
- El programa inicia y termina limpiamente.
- La UI no escribe directamente en el `.dat` ni duplica las reglas del servicio.
