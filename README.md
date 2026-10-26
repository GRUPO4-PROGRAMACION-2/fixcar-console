# FixCar — Sistema del taller (consola)

Sistema de consola en Java 21 para administrar un taller mecánico: registra clientes y vehículos, crea órdenes de trabajo con servicios (mantenimiento o reparación), controla su estado y diagnostica órdenes en segundo plano, incluso todas las pendientes a la vez. Los datos se guardan en una base de datos H2 con JDBC y un pool de conexiones.

Proyecto de Programación II, Universidad Evangélica de El Salvador. Versión actual: v2.0.0 (entrega final).

Repositorio: https://github.com/GRUPO4-PROGRAMACION-2/fixcar-console

## Integrantes

Edwin Castro, Elisa Guzmán, Kenia Hernández, Wilmer González, Josué Juárez, Edgar Gómez, Brayan Méndez, Juan Molina y David Nerio.

## Tecnologías

- Java 21 y Maven
- H2 (base de datos embebida) con JDBC
- Pool de conexiones Tomcat JDBC
- JUnit 5 y Mockito

## Requisitos

- JDK 21
- Maven 3.9 o posterior

No hace falta instalar ninguna base de datos.

## Base de datos

La base H2 se crea sola la primera vez que se ejecuta, en `data/taller.mv.db`. Las tablas salen de `database/schema.sql` y los dos mecánicos iniciales de `database/data.sql`. La conexión y los parámetros del pool están en `src/main/resources/db.properties`.

Para empezar de cero se borra la carpeta `data/`. Solo un programa puede abrir la base a la vez.

## Cómo ejecutar

```powershell
mvn clean package
java -jar target/taller-consola-1.0-SNAPSHOT.jar
```

También se puede descargar el JAR ya armado desde la sección Releases (`FixCar-v2.0.0.jar`) y ejecutarlo con `java -jar`.

## Cómo ejecutar las pruebas

```powershell
mvn test
```

Son 90 pruebas con JUnit y Mockito. Las de los DAO y la concurrencia usan una base H2 en memoria.

## Usuario de prueba

La aplicación no tiene inicio de sesión. Los mecánicos que se pueden asignar a una orden son Elisa y Dilan, que se cargan desde `database/data.sql`.

## Concurrencia

El diagnóstico de órdenes se ejecuta en un `ExecutorService` de 4 hilos (`ProcesadorOrdenes`). Cada diagnóstico toma una conexión del pool, cambia el estado de la orden de PENDIENTE a EN_PROCESO y la devuelve. Para que dos hilos no cambien la misma orden a la vez, el `UPDATE` solo se aplica si la orden sigue en el estado que se leyó (`WHERE id = ? AND estado = ?`); al otro hilo se le informa que la orden ya cambió.

En el menú, la opción 6 diagnostica una orden sin bloquear la consola y la opción 8 diagnostica todas las pendientes a la vez.

## Estructura

- `src/main/java/taller/model/` — Persona, Cliente, Trabajador, Vehículo, servicios, órdenes y estados.
- `src/main/java/taller/dao/` — interfaces `Repository` y DAO por entidad.
- `src/main/java/taller/dao/jdbc/` — implementaciones JDBC.
- `src/main/java/taller/service/` — `TallerService` (reglas) y `ProcesadorOrdenes` (hilos).
- `src/main/java/taller/controller/` — `TallerController`.
- `src/main/java/taller/ui/` — `ConsoleUI` (menú).
- `src/main/java/taller/config/` — `DataSourceConfig` (pool).
- `src/main/java/taller/exception/` y `taller/dto/` — excepciones propias y objetos de resultado.
- `src/test/java/` — pruebas.
- `database/` — `schema.sql` y `data.sql`.
- `docs/` — diagramas UML y de arquitectura.
