# Tareas de desarrollo — Taller de consola

Cada integrante toma una tarea, escribe su nombre/usuario de GitHub y cambia el
estado a `En progreso`. Las tareas están ordenadas por dependencia; no se empieza
una hasta que las tareas indicadas como requisito estén integradas.

## Alcance común
- Java 21, aplicación de consola para trabajadores del taller.
- Sin frontend, login, API, Spring ni conexión a base de datos.
- Funciones mínimas: registrar clientes y vehículos, crear/listar órdenes y cambiar
  su estado.
- Guardar los datos en `taller.dat`.
- Capas sencillas: `ui`, `controller`, `service`, `dao` y `model`.

## Orden recomendado

| Prioridad | Tarea | Requiere |
| --- | --- | --- |
| 1 | [Estructura del proyecto](T01-estructura-proyecto.md) | — |
| 2 | [Personas](T02-personas.md) | T01 |
| 3 | [Vehículo](T03-vehiculo.md) | T02 |
| 4 | [Servicios](T04-servicios.md) | T01; puede hacerse en paralelo con T02/T03 |
| 5 | [Orden de trabajo](T05-orden-trabajo.md) | T02, T03 y T04 |
| 6 | [Persistencia `.dat`](T06-persistencia-dat.md) | T02–T05 |
| 7 | [Reglas de negocio](T07-reglas-negocio.md) | T05 y T06 |
| 8 | [Concurrencia](T08-concurrencia.md) | T05 y T07 |
| 9 | [Controller](T09-controller.md) | T07 y T08 |
| 10 | [Consola y arranque](T10-consola.md) | T01 y T09 |

T02 y T04 pueden avanzar en paralelo después de T01. T03 empieza al tener T02.

## Evitar conflictos
1. Cada persona modifica únicamente los archivos propios de su tarea.
2. Antes de conectar módulos, acuerden las firmas públicas necesarias; anótenlas en
   el issue o Pull Request, no creen capas adicionales.
3. Una sola persona es dueña de cada archivo compartido, especialmente `pom.xml`,
   `TallerDAO` y `Main.java`.
4. Cada integrante trabaja en su rama y registra commits propios.

El informe del avance es un entregable, no una tarea de programación: cada módulo
aporta su explicación breve para integrarla en `docs/avance2.md`.
