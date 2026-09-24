# T05 — Orden de trabajo
- **Prioridad:** 5
- **Estado:** Disponible
- **Responsable:** [nombre / usuario de GitHub]
- **Rama:** `feature/orden-trabajo`
- **Depende de:** T02, T03 y T04

## Alcance
- Crear `OrdenTrabajo` serializable, asociada a un vehículo, trabajador y servicio.
- Crear `EstadoOrden` con los estados mínimos acordados.
- Permitir cambios de estado controlados.

## Archivos propios
- `src/main/java/taller/model/OrdenTrabajo.java`
- `src/main/java/taller/model/EstadoOrden.java`

## Lista de listo
- La orden tiene identificador, descripción y asociaciones necesarias.
- Sus atributos están encapsulados y sus clases pueden guardarse en `.dat`.
