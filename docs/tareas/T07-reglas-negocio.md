# T07 — Reglas de negocio
- **Prioridad:** 7
- **Estado:** Disponible
- **Responsable:** [nombre / usuario de GitHub]
- **Rama:** `feature/reglas-negocio`
- **Depende de:** T05 y T06

## Alcance
- Crear `TallerService` para registrar clientes/vehículos, crear/listar órdenes y
  cambiar su estado.
- Validar reglas básicas y usar `TallerDAO` para guardar los datos.
- Acordar con T10 el método que procesa el diagnóstico de una orden.

## Archivo propio
- `src/main/java/taller/service/TallerService.java`

## Lista de listo
- El servicio no depende de `Scanner` ni muestra menús.
- Las operaciones básicas funcionan usando el DAO.
- Las firmas públicas se acordaron con T08, T09 y T10.
