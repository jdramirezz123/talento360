\copy personas(id_persona, cedula, primer_apellido, segundo_apellido, nombres, nombre_completo, expedida, tipo_sangre, fecha_nacimiento, edad, sexo) FROM 'database/data/personas.csv' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8');
\copy dependencias(id_dependencia, dependencia) FROM 'database/data/dependencias.csv' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8');
\copy cargos(id_cargo, tipo_cargo, cargo, codigo, grado, asignacion_sueldo, nivel) FROM 'database/data/cargos.csv' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8');
\copy educacion(id_educacion, estudios, matricula_profesional, institucion_estudios, postgrado, institucion_postgrado, diplomado_cap_sena, correo_institucional) FROM 'database/data/educacion.csv' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8');
\copy contactos(id_contacto, direccion, ciudad, telefono_fijo, celular, correo_personal, correo_institucional) FROM 'database/data/contactos.csv' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8');
\copy estados(id_estado, clasificacion_empleo, situacion, funciones_pagadas, novedades, opec) FROM 'database/data/estados.csv' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8');
\copy rel_principal(id_registro, id_persona, id_cargo_base, id_cargo_actual, id_dependencia, id_educacion, id_contacto, id_estado, otro_tiempo_gobernacion, fecha_ingreso, tiempo_servicio, fecha_encargo) FROM 'database/data/rel_principal.csv' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8');
\copy vacaciones(dependencia, numero, apellidos_nombres, titular_cargo, genero, documento, fecha_ingreso, cargo, codigo, grado, sueldo, gastos_rep, fecha_corte, hoy, dias_totales, anos, meses, periodos, observaciones, tipo_vinculacion, revision_planta) FROM 'database/data/vacaciones.csv' WITH (FORMAT csv, HEADER true, ENCODING 'UTF8');


UPDATE vacaciones
SET estado = CASE (id_vacacion % 5)
    WHEN 0 THEN 'Aprobada'
    WHEN 1 THEN 'Finalizada'
    WHEN 2 THEN 'Rechazada'
    WHEN 3 THEN 'En revisión'
    ELSE 'Pendiente'
END;
