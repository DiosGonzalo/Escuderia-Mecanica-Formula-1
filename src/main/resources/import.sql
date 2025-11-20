-- ------------------------------------------------------------------
-- SCRIPT SQL DE INICIALIZACIÓN: 2 COCHES, COMPONENTES Y 3 CARRERAS
-- ------------------------------------------------------------------

-- NOTA: Este script asume que las tablas 'coche', 'componente', 'carrera' y 'carrera_coche' ya existen
-- y se usará para poblar los datos iniciales.

-- 1. INSERTS DE COCHES
-- ------------------------------------------------------------------

INSERT INTO coche (id, modelo, imagen, piloto, potencia) VALUES
    (1, 'Red Bull RB20', 'https://placehold.co/800x400/0000FF/FFFFFF?text=RB20_VERSTAPPEN', 'Max Verstappen', 750),
    (2, 'Aston Martin AMR24', 'https://placehold.co/800x400/005f50/FFFFFF?text=AMR24_ALONSO', 'Fernando Alonso', 745);

-- 2. INSERTS DE COMPONENTES
-- ------------------------------------------------------------------
-- Componente (nombre, tipo, limite_usos, veces_usado, estado, caballos, peso, downforce, drag, grip_seco, grip_lluvia, coche_id)

INSERT INTO componente (nombre, tipo, limite_usos, veces_usado, estado, caballos, peso, downforce, drag, grip_seco, grip_lluvia, coche_id) VALUES

    -- Componentes instalados en Coche 1 (Red Bull RB20 / Verstappen)
    ('Motor Honda RBPT Spec 2', 'MOTOR', 8, 1, 0.95, 150.0, 150.0, 0.0, 0.0, 0.0, 0.0, 1),
    ('Turbo Compresor de Alto Flujo RB', 'TURBO', 4, 0, 1.0, 25.5, 10.0, 0.0, 0.0, 0.0, 0.0, 1),
    ('Batería ESS Red Bull (80%)', 'BATERIA', 4, 2, 0.80, 10.0, 80.0, 0.0, 0.0, 0.0, 0.0, 1),
    ('Caja de Cambios Xtrac Gp RB', 'CAJA_DE_CAMBIOS', 5, 0, 1.0, 0.0, 45.0, 0.0, 0.0, 0.0, 0.0, 1),
    ('Neumáticos Pirelli Blando', 'NEUMATICOS', 1, 0, 1.0, 2.0, 8.0, 0.0, 0.0, 1.0, 0.6, 1),
    ('Alerón Frontal High Downforce', 'ALERON', 20, 5, 0.75, 4.5, 5.0, 45.0, 18.0, 0.0, 0.0, 1),
    ('Difusor Trasero RB Específico', 'PARAGOLPES', 50, 0, 1.0, 0.0, 12.0, 65.0, 22.0, 0.0, 0.0, 1),
    ('Suspensión Push-Rod RB', 'SUSPENSION', 10, 3, 0.70, 0.0, 25.0, 0.0, 0.0, 0.0, 0.0, 1),
    ('Dirección de Alta Precisión', 'DIRECCION', 50, 0, 1.0, 0.0, 5.0, 0.0, 0.0, 0.0, 0.0, 1),

    -- Componentes instalados en Coche 2 (Aston Martin AMR24 / Alonso)
    ('Motor Mercedes PU24 Spec B', 'MOTOR', 8, 1, 0.92, 148.0, 148.0, 0.0, 0.0, 0.0, 0.0, 2),
    ('Turbo Compresor AMR24', 'TURBO', 4, 0, 1.0, 26.0, 10.5, 0.0, 0.0, 0.0, 0.0, 2),
    ('Batería ESS Aston Martin (70%)', 'BATERIA', 4, 2, 0.70, 10.5, 82.0, 0.0, 0.0, 0.0, 0.0, 2),
    ('Caja de Cambios Xtrac Gp AM', 'CAJA_DE_CAMBIOS', 5, 0, 1.0, 0.0, 46.0, 0.0, 0.0, 0.0, 0.0, 2),
    ('Neumáticos Pirelli Medio', 'NEUMATICOS', 1, 0, 1.0, 2.5, 7.5, 0.0, 0.0, 0.9, 0.7, 2),
    ('Alerón Frontal Low Drag', 'ALERON', 20, 5, 0.80, 3.5, 4.5, 30.0, 10.0, 0.0, 0.0, 2),
    ('Difusor Trasero Venturi', 'PARAGOLPES', 50, 0, 1.0, 0.0, 11.5, 62.0, 25.0, 0.0, 0.0, 2),
    ('Suspensión Doble Horquilla AM', 'SUSPENSION', 10, 3, 0.75, 0.0, 26.0, 0.0, 0.0, 0.0, 0.0, 2),
    ('Dirección Hidráulica Espec. AM', 'DIRECCION', 50, 0, 1.0, 0.0, 5.5, 0.0, 0.0, 0.0, 0.0, 2),

    -- Componentes de ALMACÉN (coche_id = NULL)
    ('Motor Ferrari PU24 (Spare)', 'MOTOR', 8, 0, 1.0, 155.0, 152.0, 0.0, 0.0, 0.0, 0.0, NULL),
    ('Turbo Compresor Estándar', 'TURBO', 4, 0, 1.0, 25.0, 9.8, 0.0, 0.0, 0.0, 0.0, NULL),
    ('Batería ESS de Emergencia', 'BATERIA', 4, 0, 1.0, 8.0, 75.0, 0.0, 0.0, 0.0, 0.0, NULL),
    ('Neumáticos Pirelli Intermedio', 'NEUMATICOS', 1, 0, 1.0, 2.2, 8.2, 0.0, 0.0, 0.7, 0.9, NULL),
    ('Neumáticos Pirelli Lluvia Extrema', 'NEUMATICOS', 1, 0, 1.0, 1.5, 8.5, 0.0, 0.0, 0.5, 1.0, NULL),
    ('Alerón Trasero DRS Activo', 'ALERON', 20, 0, 1.0, 4.0, 4.5, 55.0, 12.0, 0.0, 0.0, NULL),
    ('Alerón Delantero Low Drag', 'ALERON', 20, 0, 1.0, 3.0, 4.0, 28.0, 8.0, 0.0, 0.0, NULL),
    ('Difusor de Alta Eficiencia', 'PARAGOLPES', 50, 0, 1.0, 0.0, 13.0, 75.0, 20.0, 0.0, 0.0, NULL),
    ('Suspensión Push-Rod Estándar', 'SUSPENSION', 10, 0, 1.0, 0.0, 28.0, 0.0, 0.0, 0.0, 0.0, NULL),
    ('Dirección Asistida Hidráulica', 'DIRECCION', 50, 0, 1.0, 0.0, 6.0, 0.0, 0.0, 0.0, 0.0, NULL);


-- 3. INSERTS DE CARRERAS (Añadiendo 'longitud_circuito' que es esencial para la simulación)
-- ------------------------------------------------------------------
-- Carrera (id, nombre, fecha, imagen, dificultad, numero_vueltas, longitud_circuito, clima)

INSERT INTO carrera (id, nombre, fecha, imagen, dificultad, numero_vueltas, longitud_circuito, clima) VALUES
    (1, 'Gran Premio de Bahréin', '2025-03-02', 'https://placehold.co/800x400/FF2A2A/0F0F0F?text=BAHREIN', 'MEDIA', 57, 5.412, 'SECO'),
    (2, 'Gran Premio de España', '2025-05-11', 'https://placehold.co/800x400/00BFFF/0F0F0F?text=ESPAÑA', 'FACIL', 66, 4.655, 'SECO'),
    (3, 'Gran Premio de Mónaco', '2025-05-25', 'https://placehold.co/800x400/AAAAAA/0F0F0F?text=MONACO', 'DIFICIL', 78, 3.337, 'LLUVIA');

-- 4. INSERTS DE RELACIÓN MANY-TO-MANY (carrera_coche)
-- ------------------------------------------------------------------

INSERT INTO carrera_coche (carreras_id, coche_id) VALUES
    (1, 1), (1, 2),
    (2, 1), (2, 2),
    (3, 1), (3, 2);

-- 5. REINICIAMOS EL CONTADOR DE ID (Para futuros INSERTs)
-- ------------------------------------------------------------------
ALTER TABLE coche ALTER COLUMN id RESTART WITH 3;