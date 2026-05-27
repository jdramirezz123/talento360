BEGIN;

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
UPDATE personas p
SET primer_apellido = b.primer_apellido,
    segundo_apellido = b.segundo_apellido,
    nombres = b.nombres,
    nombre_completo = b.primer_apellido || ' ' || b.segundo_apellido || ' ' || b.nombres,
    expedida = (ARRAY['TUNJA','DUITAMA','SOGAMOSO','CHIQUINQUIRÁ','PAIPA','MONIQUIRÁ','GARAGOA','VILLA DE LEYVA'])[(b.gs % 8) + 1]
FROM base b
WHERE p.id_persona = 'PER' || LPAD(b.gs::text, 4, '0')
   OR p.id_persona = 'PER' || LPAD(b.gs::text, 3, '0');

UPDATE vacaciones v
SET apellidos_nombres = p.nombre_completo
FROM personas p
WHERE v.documento = p.cedula;

COMMIT;