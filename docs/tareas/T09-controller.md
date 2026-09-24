# T09 — Controller
- **Prioridad:** 9
- **Estado:** Disponible
- **Responsable:** [nombre / usuario de GitHub]
- **Rama:** `feature/controller`
- **Depende de:** T07 y T08

## Trabajo de desarrollo
- Crear `TallerController` como puente entre consola y `TallerService`.
- Recibir opciones/datos y llamar a la operación adecuada.
- Enviar las órdenes de diagnóstico a `ProcesadorOrdenes`.

## Archivo propio
- `src/main/java/taller/controller/TallerController.java`

## Terminado cuando
- El controller conecta los servicios y el procesador sin duplicar reglas de negocio.
- Sus métodos públicos están acordados con T07, T08 y T10.
