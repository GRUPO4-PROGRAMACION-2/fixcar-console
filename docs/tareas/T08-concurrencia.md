# T08 — Procesamiento concurrente
- **Prioridad:** 8
- **Estado:** Disponible
- **Responsable:** [nombre / usuario de GitHub]
- **Rama:** `feature/concurrencia`
- **Depende de:** T05 y T07

## Trabajo de desarrollo
- Crear `ProcesadorOrdenes` para procesar diagnósticos de órdenes distintas en
  segundo plano con un grupo pequeño de hilos.
- Encolar órdenes y delegar el diagnóstico a `TallerService`.
- Permitir que la consola siga respondiendo mientras se procesan órdenes.
- Cerrar los hilos correctamente al salir.

## Archivo propio
- `src/main/java/taller/service/ProcesadorOrdenes.java`

## Terminado cuando
- Se pueden encolar varias órdenes reales y procesarlas sin bloquear el menú.
- El guardado concurrente no daña `taller.dat`.
- El procesamiento usa órdenes reales del taller; no crea hilos solo para demostrar `Thread`.
