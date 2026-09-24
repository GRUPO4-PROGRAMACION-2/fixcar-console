# T06 — Colecciones y persistencia `.dat`
- **Prioridad:** 6
- **Estado:** Disponible
- **Responsable:** [nombre / usuario de GitHub]
- **Rama:** `feature/persistencia-dat`
- **Depende de:** T02, T03, T04 y T05

## Alcance
- Crear `TallerData` con listas sencillas de clientes, vehículos y órdenes.
- Crear `TallerDAO` para cargar/guardar todo en `taller.dat`.
- Crear el archivo al primer inicio y evitar que guardados simultáneos lo dañen.

## Archivos propios
- `src/main/java/taller/dao/TallerData.java`
- `src/main/java/taller/dao/TallerDAO.java`
- `data/` (datos generados al ejecutar)

## Lista de listo
- Los datos siguen disponibles después de reiniciar el programa.
- Se justifica brevemente el uso de `ArrayList` por el tamaño pequeño del sistema.
- El archivo se crea si aún no existe.
