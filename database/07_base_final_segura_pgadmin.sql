BEGIN;

DROP TABLE IF EXISTS historial_solicitudes CASCADE;
DROP TABLE IF EXISTS vacaciones CASCADE;
DROP TABLE IF EXISTS rel_principal CASCADE;
DROP TABLE IF EXISTS estados CASCADE;
DROP TABLE IF EXISTS contactos CASCADE;
DROP TABLE IF EXISTS educacion CASCADE;
DROP TABLE IF EXISTS cargos CASCADE;
DROP TABLE IF EXISTS dependencias CASCADE;
DROP TABLE IF EXISTS personas CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

CREATE TABLE usuarios (
    id_usuario SERIAL PRIMARY KEY,
    username VARCHAR(80) UNIQUE NOT NULL,
    password VARCHAR(120) NOT NULL,
    nombre VARCHAR(160) NOT NULL,
    rol VARCHAR(80) NOT NULL DEFAULT 'Administrador',
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE personas (
    id_persona VARCHAR(30) PRIMARY KEY,
    cedula VARCHAR(40) NOT NULL,
    primer_apellido VARCHAR(120) NOT NULL,
    segundo_apellido VARCHAR(120) NOT NULL,
    nombres VARCHAR(180) NOT NULL,
    nombre_completo VARCHAR(280) NOT NULL,
    expedida VARCHAR(120) NOT NULL,
    tipo_sangre VARCHAR(30) NOT NULL,
    fecha_nacimiento VARCHAR(60) NOT NULL,
    edad VARCHAR(30) NOT NULL,
    sexo VARCHAR(30) NOT NULL
);

CREATE TABLE dependencias (
    id_dependencia VARCHAR(30) PRIMARY KEY,
    dependencia VARCHAR(280) NOT NULL
);

CREATE TABLE cargos (
    id_cargo VARCHAR(30) PRIMARY KEY,
    tipo_cargo VARCHAR(120) NOT NULL,
    cargo VARCHAR(280) NOT NULL,
    codigo VARCHAR(80) NOT NULL,
    grado VARCHAR(80) NOT NULL,
    asignacion_sueldo VARCHAR(120) NOT NULL,
    nivel VARCHAR(120) NOT NULL
);

CREATE TABLE educacion (
    id_educacion VARCHAR(30) PRIMARY KEY,
    estudios VARCHAR(280) NOT NULL,
    matricula_profesional VARCHAR(180) NOT NULL,
    institucion_estudios VARCHAR(280) NOT NULL,
    postgrado VARCHAR(280) NOT NULL,
    institucion_postgrado VARCHAR(280) NOT NULL,
    diplomado_cap_sena VARCHAR(280) NOT NULL,
    correo_institucional VARCHAR(180) NOT NULL
);

CREATE TABLE contactos (
    id_contacto VARCHAR(30) PRIMARY KEY,
    direccion VARCHAR(280) NOT NULL,
    ciudad VARCHAR(120) NOT NULL,
    telefono_fijo VARCHAR(80) NOT NULL,
    celular VARCHAR(80) NOT NULL,
    correo_personal VARCHAR(180) NOT NULL,
    correo_institucional VARCHAR(180) NOT NULL
);

CREATE TABLE estados (
    id_estado VARCHAR(30) PRIMARY KEY,
    clasificacion_empleo VARCHAR(180) NOT NULL,
    situacion VARCHAR(180) NOT NULL,
    funciones_pagadas VARCHAR(180) NOT NULL,
    novedades VARCHAR(280) NOT NULL,
    opec VARCHAR(120) NOT NULL
);

CREATE TABLE rel_principal (
    id_registro VARCHAR(30) PRIMARY KEY,
    id_persona VARCHAR(30) NOT NULL REFERENCES personas(id_persona),
    id_cargo_base VARCHAR(30) NOT NULL REFERENCES cargos(id_cargo),
    id_cargo_actual VARCHAR(30) NOT NULL REFERENCES cargos(id_cargo),
    id_dependencia VARCHAR(30) NOT NULL REFERENCES dependencias(id_dependencia),
    id_educacion VARCHAR(30) NOT NULL REFERENCES educacion(id_educacion),
    id_contacto VARCHAR(30) NOT NULL REFERENCES contactos(id_contacto),
    id_estado VARCHAR(30) NOT NULL REFERENCES estados(id_estado),
    otro_tiempo_gobernacion VARCHAR(120) NOT NULL,
    fecha_ingreso VARCHAR(80) NOT NULL,
    tiempo_servicio VARCHAR(120) NOT NULL,
    fecha_encargo VARCHAR(80) NOT NULL
);

CREATE TABLE vacaciones (
    id_vacacion SERIAL PRIMARY KEY,
    dependencia VARCHAR(280) NOT NULL,
    numero VARCHAR(60) NOT NULL,
    apellidos_nombres VARCHAR(280) NOT NULL,
    titular_cargo VARCHAR(280) NOT NULL,
    genero VARCHAR(40) NOT NULL,
    documento VARCHAR(60) NOT NULL,
    fecha_ingreso VARCHAR(80) NOT NULL,
    cargo VARCHAR(280) NOT NULL,
    codigo VARCHAR(80) NOT NULL,
    grado VARCHAR(80) NOT NULL,
    sueldo VARCHAR(120) NOT NULL,
    gastos_rep VARCHAR(120) NOT NULL,
    fecha_corte VARCHAR(80) NOT NULL,
    hoy VARCHAR(80) NOT NULL,
    dias_totales VARCHAR(80) NOT NULL,
    anos VARCHAR(80) NOT NULL,
    meses VARCHAR(80) NOT NULL,
    periodos VARCHAR(120) NOT NULL,
    observaciones VARCHAR(400) NOT NULL,
    tipo_vinculacion VARCHAR(180) NOT NULL,
    estado VARCHAR(80) NOT NULL DEFAULT 'Pendiente',
    revision_planta VARCHAR(180) NOT NULL
);

CREATE TABLE historial_solicitudes (
    id_historial SERIAL PRIMARY KEY,
    id_vacacion INTEGER REFERENCES vacaciones(id_vacacion) ON DELETE CASCADE,
    estado_nuevo VARCHAR(80) NOT NULL,
    nota VARCHAR(400) NOT NULL DEFAULT 'Cambio realizado desde interfaz',
    actualizado_por VARCHAR(120) NOT NULL DEFAULT CURRENT_USER,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO usuarios (username, password, nombre, rol, estado) VALUES
('admin@boyaca.gov.co', 'admin123', 'Administrador Talento Humano', 'Administrador', 'ACTIVO'),
('carlos.aguilar@boyaca.gov.co', 'carlos123', 'Carlos Aguilar', 'Coordinador Talento Humano', 'ACTIVO'),
('maria.gonzales@boyaca.gov.co', 'maria123', 'Maria Gonzales', 'Consulta', 'ACTIVO');

INSERT INTO dependencias(id_dependencia, dependencia) VALUES
('DEP01','DESPACHO DEL GOBERNADOR'),
('DEP02','SECRETARÍA DE PLANEACIÓN'),
('DEP03','SECRETARÍA DE HACIENDA'),
('DEP04','SECRETARÍA DE CONTRATACIÓN'),
('DEP05','SECRETARÍA GENERAL'),
('DEP06','SECRETARÍA DE GOBIERNO Y ACCIÓN COMUNAL'),
('DEP07','SECRETARÍA DE INTEGRACIÓN SOCIAL'),
('DEP08','SECRETARÍA DE EDUCACIÓN'),
('DEP09','SECRETARÍA DE CULTURA Y PATRIMONIO'),
('DEP10','SECRETARÍA DE SALUD'),
('DEP11','SECRETARÍA DE INFRAESTRUCTURA PÚBLICA'),
('DEP12','SECRETARÍA DE MINAS Y ENERGÍA'),
('DEP13','SECRETARÍA DE AMBIENTE Y DESARROLLO SOSTENIBLE'),
('DEP14','SECRETARÍA DE AGRICULTURA'),
('DEP15','SECRETARÍA DE DESARROLLO EMPRESARIAL'),
('DEP16','SECRETARÍA DE TURISMO'),
('DEP17','SECRETARÍA DE TIC Y GOBIERNO ABIERTO'),
('DEP18','DIRECCIÓN DE TALENTO HUMANO'),
('DEP19','OFICINA JURÍDICA'),
('DEP20','CONTROL INTERNO');

INSERT INTO cargos(id_cargo, tipo_cargo, cargo, codigo, grado, asignacion_sueldo, nivel) VALUES
('CAR01','Planta','PROFESIONAL UNIVERSITARIO','219','12','$4.900.000','Profesional'),
('CAR02','Planta','PROFESIONAL ESPECIALIZADO','222','18','$6.300.000','Profesional'),
('CAR03','Planta','TÉCNICO ADMINISTRATIVO','367','08','$3.200.000','Técnico'),
('CAR04','Planta','AUXILIAR ADMINISTRATIVO','407','06','$2.600.000','Asistencial'),
('CAR05','Libre nombramiento','ASESOR JURÍDICO','105','03','$7.200.000','Asesor'),
('CAR06','Planta','COORDINADOR DE ÁREA','227','20','$6.800.000','Profesional'),
('CAR07','Contrato','CONTRATISTA DE APOYO','OPS','01','$3.500.000','Apoyo'),
('CAR08','Directivo','SECRETARIO DE DESPACHO','020','02','$10.500.000','Directivo'),
('CAR09','Planta','AUXILIAR DE SERVICIOS GENERALES','470','04','$2.100.000','Asistencial'),
('CAR10','Planta','TÉCNICO OPERATIVO','314','09','$3.350.000','Técnico'),
('CAR11','Planta','PROFESIONAL DE APOYO','219','10','$4.400.000','Profesional'),
('CAR12','Directivo','DIRECTOR ADMINISTRATIVO','009','01','$9.800.000','Directivo');

INSERT INTO estados(id_estado, clasificacion_empleo, situacion, funciones_pagadas, novedades, opec) VALUES
('EST01','Carrera administrativa','ACTIVO','Sí','Sin novedades','OPEC-101'),
('EST02','Carrera administrativa','ENCARGO','Sí','Encargo vigente','OPEC-102'),
('EST03','Carrera administrativa','RETIRADO','No','Retiro registrado','OPEC-103'),
('EST04','Provisional','PROVISIONAL','Sí','Nombramiento provisional','OPEC-104');

WITH catalogos AS (
    SELECT ARRAY[
        'MARTÍNEZ','PÉREZ','RODRÍGUEZ','GÓMEZ','RUIZ','SILVA','PARRA','NIÑO','CORTÉS','RINCÓN',
        'DUARTE','TORRES','CÁRDENAS','BERNAL','REYES','AGUILAR','VEGA','MÉNDEZ','ROJAS','RIVERA',
        'SÁNCHEZ','LÓPEZ','CÁCERES','MORA','PINZÓN','SUÁREZ','DÍAZ','HERRERA','VARGAS','CASTELLANOS',
        'BARRERA','FUENTES','PÁEZ','MORALES','CASTRO','CIFUENTES','ORTIZ','TÉLLEZ','ROMERO','PATIÑO',
        'RAMÍREZ','MORENO','MEJÍA','SOTO','HERNÁNDEZ','MENDOZA','ACOSTA','GUTIÉRREZ','ARIAS','OSPINA',
        'FONSECA','BELTRÁN','QUINTERO','LEÓN','VILLAMIL','ÁLVAREZ','MEDINA','BUITRAGO','FORERO','CHACÓN',
        'GUALDRÓN','AVENDAÑO','CELY','MONTAÑA','BONILLA','BECERRA','GALVIS','CAMARGO','ZAMBRANO','LUNA',
        'SALAZAR','GARZÓN','VELANDIA','BUSTOS','CAICEDO','BÁEZ','MALDONADO','ARÉVALO','CARVAJAL','PEÑA'
    ] AS apellidos,
    ARRAY[
        'LAURA CAMILA','DIANA CAROLINA','PAULA ANDREA','NATALIA FERNANDA','MARÍA CAMILA','VALENTINA',
        'MÓNICA ALEJANDRA','CLAUDIA PATRICIA','CAROLINA','ISABELLA','LUCÍA FERNANDA','VERÓNICA',
        'DAHIANA','LAURA SOFÍA','DIANA MARCELA','ANA SOFÍA','JULIANA','VALERIA','MANUELA','CATALINA',
        'SOFÍA','PAULA MARCELA','MARCELA','ISABEL','DANIELA','ALEJANDRA','SANDRA MILENA','NATALIA',
        'CAMILA','CAROLINA MARÍA','ELIANA PATRICIA','MELISSA','ANGIE TATIANA','KAREN JULIETH','LINA MARÍA',
        'YULIANA','MAYRA ALEJANDRA','GLORIA ESPERANZA','ADRIANA LUCÍA','JENNYFER PAOLA','VIVIANA ANDREA',
        'KATHERINE','JESSICA PAOLA','TATIANA','ERIKA JOHANA','YENNY CAROLINA','LUISA FERNANDA','MARIANA',
        'SARA VALENTINA','LORENA','PILAR','NADIA','JULIANA MARCELA','MARTHA CECILIA','DIANA LORENA',
        'ANGÉLICA MARÍA','PAOLA ANDREA','LILIANA PATRICIA','MARGARITA ROSA','CAMILA ANDREA','ROCÍO DEL PILAR'
    ] AS nombres_f,
    ARRAY[
        'CARLOS ANDRÉS','JUAN SEBASTIÁN','MIGUEL ÁNGEL','ANDRÉS FELIPE','SANTIAGO','JORGE IVÁN',
        'DANIEL ESTEBAN','FELIPE ANDRÉS','CAMILO ANDRÉS','CARLOS EDUARDO','JUAN PABLO','MIGUEL ALEJANDRO',
        'SANTIAGO ANDRÉS','FELIPE','ANDRÉS','DANIEL','JORGE','SEBASTIÁN','CAMILO','JULIÁN',
        'PABLO ANDRÉS','NICOLÁS','ESTEBAN','MAURICIO','DAVID','ALEJANDRO','CRISTIAN','RICARDO',
        'OSCAR MAURICIO','RAFAEL','JOSÉ DAVID','LUIS FERNANDO','EDWIN ALEXANDER','HENRY ALBERTO','WILSON JAVIER',
        'BRAYAN STIVEN','FERNANDO JOSÉ','ALBERTO JOSÉ','GABRIEL EDUARDO','HUGO ARMANDO','IVÁN DARÍO',
        'JAIRO ANTONIO','LEONARDO','MANUEL ALEJANDRO','ORLANDO','RODRIGO','SAMUEL DAVID','TOMÁS',
        'WILLIAM ANDRÉS','ÁLVARO JOSÉ','EMILIO','MATEO','DIEGO FERNANDO','JAVIER EDUARDO','MARIO ALBERTO',
        'FRANCISCO JAVIER','ARMANDO','EDUARDO','ROBERTO CARLOS','ENRIQUE','OMAR ANDRÉS'
    ] AS nombres_m
), base AS (
    SELECT gs,
           CASE WHEN gs % 2 = 0 THEN 'F' ELSE 'M' END AS sexo,
           apellidos[((gs * 7) % array_length(apellidos, 1)) + 1] AS primer_apellido,
           CASE
               WHEN apellidos[((gs * 7) % array_length(apellidos, 1)) + 1] = apellidos[((gs * 13 + 5) % array_length(apellidos, 1)) + 1]
               THEN apellidos[((gs * 13 + 17) % array_length(apellidos, 1)) + 1]
               ELSE apellidos[((gs * 13 + 5) % array_length(apellidos, 1)) + 1]
           END AS segundo_apellido,
           CASE WHEN gs % 2 = 0
               THEN nombres_f[((((gs / 2) * 7) % array_length(nombres_f, 1)) + 1)]
               ELSE nombres_m[(((((gs + 1) / 2) * 7) % array_length(nombres_m, 1)) + 1)]
           END AS nombres
    FROM generate_series(1,180) gs
    CROSS JOIN catalogos
)
INSERT INTO personas(id_persona, cedula, primer_apellido, segundo_apellido, nombres, nombre_completo, expedida, tipo_sangre, fecha_nacimiento, edad, sexo)
SELECT 'PER' || LPAD(gs::text, 4, '0'),
       (1000000000 + gs * 137)::text,
       primer_apellido,
       segundo_apellido,
       nombres,
       primer_apellido || ' ' || segundo_apellido || ' ' || nombres,
       (ARRAY['TUNJA','DUITAMA','SOGAMOSO','CHIQUINQUIRÁ','PAIPA','MONIQUIRÁ','GARAGOA','VILLA DE LEYVA'])[(gs % 8) + 1],
       (ARRAY['O+','A+','B+','AB+','O-'])[(gs % 5) + 1],
       TO_CHAR(DATE '1975-01-01' + (gs * 97), 'DD/MM/YYYY'),
       (24 + (gs % 34))::text,
       sexo
FROM base;

INSERT INTO contactos(id_contacto, direccion, ciudad, telefono_fijo, celular, correo_personal, correo_institucional)
SELECT 'CON' || LPAD(gs::text, 4, '0'),
       'Calle ' || (10 + gs % 80) || ' # ' || (gs % 30) || '-' || (gs % 90),
       'Tunja, Boyacá',
       '608742' || LPAD((gs % 100)::text, 2, '0'),
       '+57 300 ' || LPAD((1000000 + gs * 431)::text, 7, '0'),
       'personal' || LPAD(gs::text, 3, '0') || '@correo.com',
       'servidor' || LPAD(gs::text, 3, '0') || '@boyaca.gov.co'
FROM generate_series(1,180) gs;

INSERT INTO educacion(id_educacion, estudios, matricula_profesional, institucion_estudios, postgrado, institucion_postgrado, diplomado_cap_sena, correo_institucional)
SELECT 'EDU' || LPAD(gs::text, 4, '0'),
       (ARRAY['Administración Pública','Derecho','Ingeniería de Sistemas','Contaduría Pública','Trabajo Social','Economía','Gestión Documental','Salud Pública'])[(gs % 8) + 1],
       'MP-' || LPAD((20000 + gs)::text, 5, '0'),
       (ARRAY['Universidad Pedagógica y Tecnológica de Colombia','Universidad de Boyacá','SENA','Universidad Nacional Abierta y a Distancia','Escuela Superior de Administración Pública'])[(gs % 5) + 1],
       (ARRAY['Especialización en Gestión Pública','Especialización en Contratación Estatal','Especialización en Gerencia de Proyectos','Especialización en Finanzas Públicas','Especialización en Derecho Administrativo'])[(gs % 5) + 1],
       (ARRAY['UPTC','Universidad de Boyacá','ESAP','Universidad Nacional Abierta y a Distancia','Universidad Santo Tomás'])[(gs % 5) + 1],
       (ARRAY['Curso de servicio al ciudadano y gestión administrativa','Diplomado en gestión documental','Curso SENA de atención al usuario','Diplomado en contratación estatal','Curso de ética pública'])[(gs % 5) + 1],
       'servidor' || LPAD(gs::text, 3, '0') || '@boyaca.gov.co'
FROM generate_series(1,180) gs;

INSERT INTO rel_principal(id_registro, id_persona, id_cargo_base, id_cargo_actual, id_dependencia, id_educacion, id_contacto, id_estado, otro_tiempo_gobernacion, fecha_ingreso, tiempo_servicio, fecha_encargo)
SELECT 'REL' || LPAD(gs::text, 4, '0'),
       'PER' || LPAD(gs::text, 4, '0'),
       'CAR' || LPAD(((gs % 12) + 1)::text, 2, '0'),
       'CAR' || LPAD((((gs + 2) % 12) + 1)::text, 2, '0'),
       'DEP' || LPAD(((gs % 20) + 1)::text, 2, '0'),
       'EDU' || LPAD(gs::text, 4, '0'),
       'CON' || LPAD(gs::text, 4, '0'),
       CASE WHEN gs % 18 = 0 THEN 'EST03' WHEN gs % 11 = 0 THEN 'EST02' WHEN gs % 7 = 0 THEN 'EST04' ELSE 'EST01' END,
       (gs % 5) || ' años adicionales',
       TO_CHAR(DATE '2010-01-01' + (gs * 23), 'DD/MM/YYYY'),
       (1 + (gs % 15)) || ' años ' || (gs % 11) || ' meses',
       TO_CHAR(DATE '2023-01-01' + (gs * 9), 'DD/MM/YYYY')
FROM generate_series(1,180) gs;

WITH req AS (
    SELECT gs,
           CASE WHEN gs <= 90 THEN 'Licencia maternidad'
                WHEN gs % 4 = 0 THEN 'Incapacidad'
                WHEN gs % 4 = 1 THEN 'Permiso'
                ELSE 'Vacaciones' END AS tipo,
           CASE WHEN gs <= 90 THEN ((gs * 2 - 2) % 180) + 2 ELSE ((gs - 1) % 180) + 1 END AS persona_num
    FROM generate_series(1,320) gs
), joined AS (
    SELECT req.gs, req.tipo, p.*, rp.id_dependencia, d.dependencia, c.cargo, c.codigo, c.grado, c.asignacion_sueldo,
           CASE (req.gs % 5)
               WHEN 0 THEN 'Aprobada'
               WHEN 1 THEN 'Finalizada'
               WHEN 2 THEN 'Rechazada'
               WHEN 3 THEN 'En revisión'
               ELSE 'Pendiente'
           END AS estado_solicitud
    FROM req
    JOIN personas p ON p.id_persona = 'PER' || LPAD(req.persona_num::text, 4, '0')
    JOIN rel_principal rp ON rp.id_persona = p.id_persona
    JOIN dependencias d ON d.id_dependencia = rp.id_dependencia
    JOIN cargos c ON c.id_cargo = rp.id_cargo_actual
)
INSERT INTO vacaciones(dependencia, numero, apellidos_nombres, titular_cargo, genero, documento, fecha_ingreso, cargo, codigo, grado, sueldo, gastos_rep, fecha_corte, hoy, dias_totales, anos, meses, periodos, observaciones, tipo_vinculacion, estado, revision_planta)
SELECT dependencia,
       'SOL-' || LPAD(gs::text, 5, '0'),
       nombre_completo,
       cargo,
       sexo,
       cedula,
       TO_CHAR(DATE '2025-01-01' + (gs % 360), 'DD/MM/YYYY'),
       cargo,
       codigo,
       grado,
       asignacion_sueldo,
       '$0',
       TO_CHAR(DATE '2026-01-01' + (gs % 150), 'DD/MM/YYYY'),
       TO_CHAR(CURRENT_DATE, 'DD/MM/YYYY'),
       CASE WHEN tipo = 'Licencia maternidad' THEN '126'
            WHEN tipo = 'Incapacidad' THEN (3 + (gs % 12))::text
            WHEN tipo = 'Permiso' THEN (1 + (gs % 3))::text
            ELSE (5 + (gs % 15))::text END,
       (gs % 8)::text,
       (gs % 12)::text,
       'Periodo ' || (2025 + (gs % 2)),
       'Tipo: ' || tipo || ' | Género: ' || sexo || ' | Registro final de demostración conectado a PostgreSQL.',
       tipo,
       estado_solicitud,
       'Revisión generada en base final'
FROM joined;

INSERT INTO historial_solicitudes(id_vacacion, estado_nuevo, nota, actualizado_por)
SELECT id_vacacion, estado, 'Estado inicial cargado por script final', 'script_final'
FROM vacaciones;

COMMIT;