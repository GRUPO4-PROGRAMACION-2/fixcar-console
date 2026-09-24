# T04 — Servicios y polimorfismo
- **Prioridad:** 4
- **Estado:** Disponible
- **Responsable:** [nombre / usuario de GitHub]
- **Rama:** `feature/servicios`
- **Depende de:** T01 (puede avanzar en paralelo con T02/T03)

## Alcance
- Crear la clase abstracta `Servicio`.
- Crear dos tipos concretos, por ejemplo `Mantenimiento` y `Reparacion`.
- Cada tipo sobrescribe un método sencillo, como `calcularCosto()`.
- Hacer serializables las clases para permitir su persistencia junto a la orden.

## Archivos propios
- `src/main/java/taller/model/Servicio.java`
- `src/main/java/taller/model/Mantenimiento.java`
- `src/main/java/taller/model/Reparacion.java`

## Lista de listo
- Hay una clase abstracta y dos implementaciones.
- La orden puede llamar el método sobrescrito usando el tipo `Servicio`.
- Las clases pueden guardarse en `.dat`.
