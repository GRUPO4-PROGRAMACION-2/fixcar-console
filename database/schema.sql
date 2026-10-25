CREATE TABLE IF NOT EXISTS cliente (
    documento   VARCHAR(30)  PRIMARY KEY,
    nombre      VARCHAR(120) NOT NULL,
    telefono    VARCHAR(30)  NOT NULL,
    correo      VARCHAR(120) NOT NULL,
    direccion   VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS trabajador (
    documento    VARCHAR(30)  PRIMARY KEY,
    nombre       VARCHAR(120) NOT NULL,
    telefono     VARCHAR(30)  NOT NULL,
    cargo        VARCHAR(60)  NOT NULL,
    especialidad VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS vehiculo (
    placa             VARCHAR(20) PRIMARY KEY,
    marca             VARCHAR(60),
    modelo            VARCHAR(60),
    cliente_documento VARCHAR(30) NOT NULL,
    CONSTRAINT fk_vehiculo_cliente FOREIGN KEY (cliente_documento) REFERENCES cliente (documento)
);

CREATE TABLE IF NOT EXISTS orden_trabajo (
    id                   VARCHAR(20)  PRIMARY KEY,
    descripcion          VARCHAR(500) NOT NULL,
    cliente_documento    VARCHAR(30)  NOT NULL,
    vehiculo_placa       VARCHAR(20)  NOT NULL,
    trabajador_documento VARCHAR(30)  NOT NULL,
    estado               VARCHAR(20)  NOT NULL,
    fecha_creacion       TIMESTAMP    NOT NULL,
    CONSTRAINT fk_orden_cliente    FOREIGN KEY (cliente_documento)    REFERENCES cliente (documento),
    CONSTRAINT fk_orden_vehiculo   FOREIGN KEY (vehiculo_placa)       REFERENCES vehiculo (placa),
    CONSTRAINT fk_orden_trabajador FOREIGN KEY (trabajador_documento) REFERENCES trabajador (documento),
    CONSTRAINT ck_orden_estado CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'FINALIZADA', 'CANCELADA'))
);

CREATE TABLE IF NOT EXISTS servicio_orden (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    orden_id        VARCHAR(20)   NOT NULL,
    tipo            VARCHAR(20)   NOT NULL,
    nombre          VARCHAR(120)  NOT NULL,
    precio_base     DECIMAL(12,2) NOT NULL,
    costo_mano_obra DECIMAL(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_servicio_orden FOREIGN KEY (orden_id) REFERENCES orden_trabajo (id) ON DELETE CASCADE,
    CONSTRAINT ck_servicio_tipo CHECK (tipo IN ('MANTENIMIENTO', 'REPARACION'))
);

CREATE INDEX IF NOT EXISTS idx_orden_estado ON orden_trabajo (estado);
CREATE INDEX IF NOT EXISTS idx_orden_vehiculo ON orden_trabajo (vehiculo_placa);
CREATE INDEX IF NOT EXISTS idx_servicio_orden ON servicio_orden (orden_id);
