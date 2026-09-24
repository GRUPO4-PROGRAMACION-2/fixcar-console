# T02 — Personas
- **Prioridad:** 2
- **Estado:** Disponible
- **Responsable:** [nombre / usuario de GitHub]
- **Rama:** `feature/personas`
- **Depende de:** T01

## Alcance
- Crear `Persona` abstracta con los datos comunes necesarios.
- Crear `Cliente` y `Trabajador` como tipos derivados.
- Mantener atributos privados, validar datos básicos e implementar `Serializable`
  para guardar los objetos en el archivo `.dat`.

## Archivos propios
- `src/main/java/taller/model/Persona.java`
- `src/main/java/taller/model/Cliente.java`
- `src/main/java/taller/model/Trabajador.java`

## Lista de listo
- Cliente y trabajador se pueden crear con datos válidos.
- La herencia es útil para representar las personas del taller.
- Las clases pueden guardarse en `.dat`.
