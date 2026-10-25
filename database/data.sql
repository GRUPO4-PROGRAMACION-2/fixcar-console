MERGE INTO trabajador (documento, nombre, telefono, cargo, especialidad) KEY (documento)
VALUES ('T-001', 'Elisa', '7000-0001', 'Mecánica', 'Motor');

MERGE INTO trabajador (documento, nombre, telefono, cargo, especialidad) KEY (documento)
VALUES ('T-002', 'Dilan', '7000-0002', 'Mecánico', 'Electricidad automotriz');
