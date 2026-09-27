# FixCar — Sistema del taller (consola)

Sistema de consola en Java 21 para gestionar un taller: registra clientes y
vehículos, crea y lista órdenes de trabajo, cambia estados y envía órdenes a
diagnóstico en segundo plano. Los datos persisten en `data/taller.dat`.

Tareas T01–T10 integradas en `dev`.

## Funciones

- Registrar clientes y vehículos.
- Crear órdenes con uno o más servicios (mantenimiento o reparación).
- Listar órdenes, clientes y vehículos.
- Cambiar el estado de una orden (solo transiciones permitidas).
- Enviar órdenes a diagnóstico sin bloquear el menú (hilos).
- Trabajadores disponibles: Elisa y Dilan (plantilla fija en memoria).

## Requisitos

- JDK 21
- Maven 3.9 o posterior

## Compilar

```powershell
mvn clean package
```

## Ejecutar

```powershell
java -cp target/classes taller.Main
```

## Estructura

- `src/main/java/taller/model/` — Cliente, Vehículo, Trabajador, servicios, órdenes y estados.
- `src/main/java/taller/dao/` — `TallerData` y `TallerDAO` (persistencia `.dat`).
- `src/main/java/taller/service/` — `TallerService` (reglas) y `ProcesadorOrdenes` (hilos).
- `src/main/java/taller/controller/` — `TallerController` (puente consola-servicios).
- `src/main/java/taller/ui/` — `ConsoleUI` (menú y lectura de datos).
- `src/main/java/taller/Main.java` — arranque y cierre de hilos.

El detalle de cada tarea quedó registrado en los Issues #1–#10 (cerrados).
