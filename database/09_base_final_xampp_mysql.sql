SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS historial_solicitudes;
DROP TABLE IF EXISTS vacaciones;
DROP TABLE IF EXISTS rel_principal;
DROP TABLE IF EXISTS estados;
DROP TABLE IF EXISTS contactos;
DROP TABLE IF EXISTS educacion;
DROP TABLE IF EXISTS cargos;
DROP TABLE IF EXISTS dependencias;
DROP TABLE IF EXISTS personas;
DROP TABLE IF EXISTS usuarios;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
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
    id_persona VARCHAR(30) NOT NULL,
    id_cargo_base VARCHAR(30) NOT NULL,
    id_cargo_actual VARCHAR(30) NOT NULL,
    id_dependencia VARCHAR(30) NOT NULL,
    id_educacion VARCHAR(30) NOT NULL,
    id_contacto VARCHAR(30) NOT NULL,
    id_estado VARCHAR(30) NOT NULL,
    otro_tiempo_gobernacion VARCHAR(120) NOT NULL,
    fecha_ingreso VARCHAR(80) NOT NULL,
    tiempo_servicio VARCHAR(120) NOT NULL,
    fecha_encargo VARCHAR(80) NOT NULL,
    FOREIGN KEY (id_persona) REFERENCES personas(id_persona),
    FOREIGN KEY (id_cargo_base) REFERENCES cargos(id_cargo),
    FOREIGN KEY (id_cargo_actual) REFERENCES cargos(id_cargo),
    FOREIGN KEY (id_dependencia) REFERENCES dependencias(id_dependencia),
    FOREIGN KEY (id_educacion) REFERENCES educacion(id_educacion),
    FOREIGN KEY (id_contacto) REFERENCES contactos(id_contacto),
    FOREIGN KEY (id_estado) REFERENCES estados(id_estado)
);

CREATE TABLE vacaciones (
    id_vacacion INT AUTO_INCREMENT PRIMARY KEY,
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
    id_historial INT AUTO_INCREMENT PRIMARY KEY,
    id_vacacion INT,
    estado_nuevo VARCHAR(80) NOT NULL,
    nota VARCHAR(400) NOT NULL DEFAULT 'Cambio realizado desde interfaz',
    actualizado_por VARCHAR(120) NOT NULL DEFAULT 'interfaz_javafx',
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_vacacion) REFERENCES vacaciones(id_vacacion) ON DELETE CASCADE
);

INSERT INTO usuarios (username, password, nombre, rol, estado) VALUES
('admin@boyaca.gov.co', 'admin123', 'Administrador Talento Humano', 'Administrador', 'ACTIVO'),
('carlos.aguilar@boyaca.gov.co', 'carlos123', 'Carlos Aguilar', 'Coordinador Talento Humano', 'ACTIVO'),
('maria.gonzales@boyaca.gov.co', 'maria123', 'Maria Gonzales', 'Consulta', 'ACTIVO');

INSERT INTO dependencias(id_dependencia, dependencia) VALUES
('DEP001','DESPACHO DEL GOBERNADOR'),
('DEP002','SECRETARIA DE PLANEACION'),
('DEP003','SECRETARIA DE HACIENDA'),
('DEP004','SECRETARIA GENERAL'),
('DEP005','DIRECCION DE TALENTO HUMANO');

INSERT INTO cargos(id_cargo, tipo_cargo, cargo, codigo, grado, asignacion_sueldo, nivel) VALUES
('CAR001','Planta','PROFESIONAL UNIVERSITARIO','219','12','$4.900.000','Profesional'),
('CAR002','Planta','PROFESIONAL ESPECIALIZADO','222','18','$6.300.000','Profesional'),
('CAR003','Planta','TECNICO ADMINISTRATIVO','367','08','$3.200.000','Tecnico'),
('CAR004','Planta','AUXILIAR ADMINISTRATIVO','407','06','$2.600.000','Asistencial');

INSERT INTO estados(id_estado, clasificacion_empleo, situacion, funciones_pagadas, novedades, opec) VALUES
('EST001','Carrera administrativa','ACTIVO','Si','Sin novedades','OPEC-101'),
('EST002','Carrera administrativa','ENCARGO','Si','Encargo vigente','OPEC-102'),
('EST003','Carrera administrativa','RETIRADO','No','Retiro registrado','OPEC-103'),
('EST004','Provisional','PROVISIONAL','Si','Nombramiento provisional','OPEC-104');

INSERT INTO personas(id_persona, cedula, primer_apellido, segundo_apellido, nombres, nombre_completo, expedida, tipo_sangre, fecha_nacimiento, edad, sexo) VALUES
('PER0001','1000000137','MARTINEZ','PEREZ','LAURA CAMILA','MARTINEZ PEREZ LAURA CAMILA','TUNJA','O+','01/01/1985','41','F'),
('PER0002','1000000274','TORRES','RIVERA','CARLOS ANDRES','TORRES RIVERA CARLOS ANDRES','DUITAMA','A+','02/02/1986','40','M'),
('PER0003','1000000411','ROJAS','GOMEZ','DIANA CAROLINA','ROJAS GOMEZ DIANA CAROLINA','SOGAMOSO','B+','03/03/1987','39','F');

INSERT INTO contactos(id_contacto, direccion, ciudad, telefono_fijo, celular, correo_personal, correo_institucional) VALUES
('CON0001','Calle 10 # 10-10','Tunja, Boyaca','6087421001','3001001001','personal001@correo.com','servidor001@boyaca.gov.co'),
('CON0002','Calle 20 # 20-20','Tunja, Boyaca','6087421002','3001001002','personal002@correo.com','servidor002@boyaca.gov.co'),
('CON0003','Calle 30 # 30-30','Tunja, Boyaca','6087421003','3001001003','personal003@correo.com','servidor003@boyaca.gov.co');

INSERT INTO educacion(id_educacion, estudios, matricula_profesional, institucion_estudios, postgrado, institucion_postgrado, diplomado_cap_sena, correo_institucional) VALUES
('EDU0001','Administracion Publica','MP-00001','UPTC','Especializacion en Gestion Publica','UPTC','Servicio al ciudadano','servidor001@boyaca.gov.co'),
('EDU0002','Derecho','MP-00002','Universidad de Boyaca','Especializacion en Contratacion Estatal','ESAP','Gestion documental','servidor002@boyaca.gov.co'),
('EDU0003','Contaduria Publica','MP-00003','UPTC','Especializacion en Finanzas Publicas','UPTC','Etica publica','servidor003@boyaca.gov.co');

INSERT INTO rel_principal(id_registro, id_persona, id_cargo_base, id_cargo_actual, id_dependencia, id_educacion, id_contacto, id_estado, otro_tiempo_gobernacion, fecha_ingreso, tiempo_servicio, fecha_encargo) VALUES
('REL0001','PER0001','CAR001','CAR002','DEP005','EDU0001','CON0001','EST001','0 anos','01/02/2015','11 anos','NO REGISTRADO'),
('REL0002','PER0002','CAR003','CAR003','DEP002','EDU0002','CON0002','EST002','1 ano','15/03/2018','8 anos','01/01/2024'),
('REL0003','PER0003','CAR004','CAR001','DEP003','EDU0003','CON0003','EST001','0 anos','20/04/2020','6 anos','NO REGISTRADO');

INSERT INTO vacaciones(dependencia, numero, apellidos_nombres, titular_cargo, genero, documento, fecha_ingreso, cargo, codigo, grado, sueldo, gastos_rep, fecha_corte, hoy, dias_totales, anos, meses, periodos, observaciones, tipo_vinculacion, estado, revision_planta) VALUES
('DIRECCION DE TALENTO HUMANO','SOL-00001','MARTINEZ PEREZ LAURA CAMILA','PROFESIONAL ESPECIALIZADO','F','1000000137','01/02/2015','PROFESIONAL ESPECIALIZADO','222','18','$6.300.000','$0','01/01/2026','27/05/2026','15','1','0','Periodo 2026','Tipo: Vacaciones | Registro XAMPP','Vacaciones','Pendiente','Carga inicial XAMPP'),
('SECRETARIA DE PLANEACION','SOL-00002','TORRES RIVERA CARLOS ANDRES','TECNICO ADMINISTRATIVO','M','1000000274','15/03/2018','TECNICO ADMINISTRATIVO','367','08','$3.200.000','$0','01/01/2026','27/05/2026','3','0','0','Periodo 2026','Tipo: Incapacidad | Registro XAMPP','Incapacidad','En revision','Carga inicial XAMPP');

INSERT INTO historial_solicitudes(id_vacacion, estado_nuevo, nota, actualizado_por) VALUES
(1, 'Pendiente', 'Estado inicial cargado por script XAMPP', 'script_xampp'),
(2, 'En revision', 'Estado inicial cargado por script XAMPP', 'script_xampp');
