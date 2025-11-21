-- ------------------------------------------------------------------
-- SCRIPT SQL ARREGLADO PARA SIMULACIÓN REALISTA
-- ------------------------------------------------------------------

-- 1. INSERTAR COCHES
-- ------------------------------------------------------------------
INSERT INTO coche (id, modelo, imagen, piloto, potencia) VALUES
    (1, 'Red Bull RB20', 'https://placehold.co/800x400/0000FF/FFFFFF?text=RB20_VERSTAPPEN', 'Max Verstappen', 750),
    (2, 'Aston Martin AMR24', 'https://placehold.co/800x400/005f50/FFFFFF?text=AMR24_ALONSO', 'Fernando Alonso', 745);

-- 2. INSERTAR COMPONENTES
-- CAMBIOS REALIZADOS:
-- A) 'estado' ahora es 100.0 (antes 1.0 era el 1% de vida).
-- B) 'limite_usos' aumentado drásticamente (Motor: 400 vueltas, Neumáticos: 90 vueltas).
-- ------------------------------------------------------------------

INSERT INTO componente (nombre, tipo, limite_usos, veces_usado, estado, caballos, peso, downforce, drag, grip_seco, grip_lluvia, coche_id) VALUES

    -- == COCHE 1: Red Bull RB20 (Verstappen) ==
    ('Motor Honda RBPT Spec 2',      'MOTOR',           400, 0, 100.0, 150.0, 150.0, 0.0, 0.0, 0.0, 0.0, 1),
    ('Turbo Compresor RB',           'TURBO',           200, 0, 100.0,  25.5,  10.0, 0.0, 0.0, 0.0, 0.0, 1),
    ('Batería ESS Red Bull',         'BATERIA',         200, 0, 100.0,  10.0,  80.0, 0.0, 0.0, 0.0, 0.0, 1),
    ('Caja de Cambios Xtrac RB',     'CAJA_DE_CAMBIOS', 300, 0, 100.0,   0.0,  45.0, 0.0, 0.0, 0.0, 0.0, 1),
    -- Neumáticos aguantan 90 vueltas (sobra para carrera de 66)
    ('Neumáticos Pirelli Blando',    'NEUMATICOS',       90, 0, 100.0,   2.0,   8.0, 0.0, 0.0, 1.0, 0.6, 1),
    ('Alerón Frontal High Downforce','ALERON',          500, 0, 100.0,   4.5,   5.0, 45.0, 18.0, 0.0, 0.0, 1),
    ('Difusor Trasero RB',           'PARAGOLPES',      500, 0, 100.0,   0.0,  12.0, 65.0, 22.0, 0.0, 0.0, 1),
    ('Suspensión Push-Rod RB',       'SUSPENSION',      500, 0, 100.0,   0.0,  25.0, 0.0, 0.0, 0.0, 0.0, 1),
    ('Dirección Alta Precisión',     'DIRECCION',       500, 0, 100.0,   0.0,   5.0, 0.0, 0.0, 0.0, 0.0, 1),

    -- == COCHE 2: Aston Martin AMR24 (Alonso) ==
    ('Motor Mercedes PU24 Spec B',   'MOTOR',           400, 0, 100.0, 148.0, 148.0, 0.0, 0.0, 0.0, 0.0, 2),
    ('Turbo Compresor AMR24',        'TURBO',           200, 0, 100.0,  26.0,  10.5, 0.0, 0.0, 0.0, 0.0, 2),
    ('Batería ESS Aston Martin',     'BATERIA',         200, 0, 100.0,  10.5,  82.0, 0.0, 0.0, 0.0, 0.0, 2),
    ('Caja de Cambios Xtrac AM',     'CAJA_DE_CAMBIOS', 300, 0, 100.0,   0.0,  46.0, 0.0, 0.0, 0.0, 0.0, 2),
    -- Neumáticos aguantan 90 vueltas
    ('Neumáticos Pirelli Medio',     'NEUMATICOS',       90, 0, 100.0,   2.5,   7.5, 0.0, 0.0, 0.9, 0.7, 2),
    ('Alerón Frontal Low Drag',      'ALERON',          500, 0, 100.0,   3.5,   4.5, 30.0, 10.0, 0.0, 0.0, 2),
    ('Difusor Trasero Venturi',      'PARAGOLPES',      500, 0, 100.0,   0.0,  11.5, 62.0, 25.0, 0.0, 0.0, 2),
    ('Suspensión Doble Horquilla AM','SUSPENSION',      500, 0, 100.0,   0.0,  26.0, 0.0, 0.0, 0.0, 0.0, 2),
    ('Dirección Hidráulica AM',      'DIRECCION',       500, 0, 100.0,   0.0,   5.5, 0.0, 0.0, 0.0, 0.0, 2),

    -- == ALMACÉN (Piezas sueltas) ==
    ('Motor Ferrari PU24 (Spare)',      'MOTOR',           400, 0, 100.0, 155.0, 152.0, 0.0, 0.0, 0.0, 0.0, NULL),
    ('Turbo Compresor Estándar',        'TURBO',           200, 0, 100.0,  25.0,   9.8, 0.0, 0.0, 0.0, 0.0, NULL),
    ('Batería ESS de Emergencia',       'BATERIA',         200, 0, 100.0,   8.0,  75.0, 0.0, 0.0, 0.0, 0.0, NULL),
    ('Neumáticos Pirelli Intermedio',   'NEUMATICOS',       90, 0, 100.0,   2.2,   8.2, 0.0, 0.0, 0.7, 0.9, NULL),
    ('Neumáticos Pirelli Lluvia Ext.',  'NEUMATICOS',       90, 0, 100.0,   1.5,   8.5, 0.0, 0.0, 0.5, 1.0, NULL),
    ('Alerón Trasero DRS Activo',       'ALERON',          500, 0, 100.0,   4.0,   4.5, 55.0, 12.0, 0.0, 0.0, NULL),
    ('Alerón Delantero Low Drag',       'ALERON',          500, 0, 100.0,   3.0,   4.0, 28.0, 8.0, 0.0, 0.0, NULL),
    ('Difusor de Alta Eficiencia',      'PARAGOLPES',      500, 0, 100.0,   0.0,  13.0, 75.0, 20.0, 0.0, 0.0, NULL),
    ('Suspensión Push-Rod Estándar',    'SUSPENSION',      500, 0, 100.0,   0.0,  28.0, 0.0, 0.0, 0.0, 0.0, NULL),
    ('Dirección Asistida Hidráulica',   'DIRECCION',       500, 0, 100.0,   0.0,   6.0, 0.0, 0.0, 0.0, 0.0, NULL);


-- 3. INSERTAR CARRERAS
-- ------------------------------------------------------------------
INSERT INTO carrera (id, nombre, fecha, imagen, dificultad, numero_vueltas, longitud_circuito, clima) VALUES
    (1, 'Gran Premio de Bahréin', '2025-03-02', 'https://placehold.co/800x400/FF2A2A/0F0F0F?text=BAHREIN', 'MEDIA', 57, 5.412, 'LLUVIA_INTENSA'),
    (2, 'Gran Premio de España', '2025-05-11', 'https://placehold.co/800x400/00BFFF/0F0F0F?text=ESPAÑA', 'FACIL', 66, 4.655, 'SECO'),
    (3, 'Gran Premio de Mónaco', '2025-05-25', 'https://placehold.co/800x400/AAAAAA/0F0F0F?text=MONACO', 'MUY_DIFICIL', 78, 3.337, 'LLUVIA');

-- 4. RELACIONAR CARRERAS Y COCHES
-- ------------------------------------------------------------------
INSERT INTO carrera_coche (carreras_id, coche_id) VALUES
    (1, 1), (1, 2),
    (2, 1), (2, 2),
    (3, 1), (3, 2);

-- 5. REINICIAR CONTADORES DE ID (Para H2 / PostgreSQL)
-- ------------------------------------------------------------------
ALTER TABLE coche ALTER COLUMN id RESTART WITH 3;