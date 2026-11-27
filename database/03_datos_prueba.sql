SET NAMES utf8mb4;
USE db_sistema_uni_final;
INSERT INTO carrera(nombre,codigo) VALUES
('Ingeniería de Sistemas e Informática','SIS'),('Ingeniería de Software','SWE'),('Ingeniería Industrial','IND'),('Administración','ADM'),('Contabilidad','CON'),('Derecho','DER'),('Psicología','PSI'),('Medicina','MED');
INSERT INTO periodo_academico(nombre,fecha_inicio,fecha_fin,estado) VALUES
('2026-I','2026-03-16','2026-07-17','CERRADO'),('2026-II','2026-08-17','2026-12-18','ACTIVO'),('2027-I','2027-03-15','2027-07-16','PLANIFICADO');

DELIMITER //
CREATE PROCEDURE seed_datos_final()
BEGIN
  DECLARE i INT DEFAULT 1;
  WHILE i <= 24 DO
    INSERT INTO usuario(username,password_hash,rol) VALUES(CONCAT('DOC',LPAD(i,3,'0')),'123','DOCENTE');
    INSERT INTO docente(id_usuario,codigo,nombres,apellidos,dni,email,telefono,especialidad)
    VALUES(LAST_INSERT_ID(),CONCAT('D',LPAD(i,4,'0')),CONCAT('Docente ',i),'Universidad',CONCAT('70',LPAD(i,6,'0')),CONCAT('docente',i,'@universidad.edu'),'999000000',CONCAT('Especialidad ',i));
    SET i=i+1;
  END WHILE;
  SET i=1;
  WHILE i <= 240 DO
    INSERT INTO usuario(username,password_hash,rol) VALUES(CONCAT('ALU',LPAD(i,4,'0')),'123','ALUMNO');
    INSERT INTO alumno(id_usuario,id_carrera,codigo,nombres,apellidos,dni,email,telefono,fecha_nacimiento)
    VALUES(LAST_INSERT_ID(),MOD(i-1,8)+1,CONCAT('A',LPAD(i,5,'0')),CONCAT('Alumno ',i),'Prueba',CONCAT('80',LPAD(i,6,'0')),CONCAT('alumno',i,'@universidad.edu'),'988000000','2002-01-01');
    SET i=i+1;
  END WHILE;
  SET i=1;
  WHILE i <= 48 DO
    INSERT INTO curso(id_carrera,codigo,nombre,creditos,ciclo_nivel) VALUES(MOD(i-1,8)+1,CONCAT('C',LPAD(i,3,'0')),CONCAT('Curso académico ',i),MOD(i,4)+2,MOD(i-1,10)+1);
    SET i=i+1;
  END WHILE;
END //
DELIMITER ;
CALL seed_datos_final();
DROP PROCEDURE seed_datos_final;

INSERT INTO seccion(id_curso,id_docente,id_periodo,codigo_seccion,aula,cupo_maximo)
SELECT id_curso,MOD(id_curso-1,24)+1,2,CONCAT('S-',LPAD(id_curso,3,'0')),'A-101',35 FROM curso;
INSERT INTO horario(id_seccion,dia_semana,hora_inicio,hora_fin,aula)
SELECT id_seccion,'LUNES','08:00:00','10:00:00','A-101' FROM seccion;

INSERT INTO matricula(id_alumno,id_periodo,monto_total)
SELECT id_alumno,2,750.00 FROM alumno;
INSERT INTO detalle_matricula(id_matricula,id_seccion)
SELECT m.id_matricula,MOD(m.id_alumno-1,48)+1 FROM matricula m
UNION ALL SELECT m.id_matricula,MOD(m.id_alumno+15,48)+1 FROM matricula m
UNION ALL SELECT m.id_matricula,MOD(m.id_alumno+30,48)+1 FROM matricula m;

INSERT INTO evaluacion(id_seccion,nombre,ponderado,fecha_evaluacion)
SELECT id_seccion,'Practica 1',0.2000,'2026-09-15' FROM seccion
UNION ALL SELECT id_seccion,'Examen Parcial',0.3000,'2026-10-15' FROM seccion
UNION ALL SELECT id_seccion,'Practica 2',0.2000,'2026-11-10' FROM seccion
UNION ALL SELECT id_seccion,'Examen Final',0.3000,'2026-12-10' FROM seccion;

INSERT INTO calificacion(id_detalle_matricula,id_evaluacion,nota)
SELECT dm.id_detalle_matricula,e.id_evaluacion,ROUND(10+RAND(dm.id_detalle_matricula*e.id_evaluacion)*10,2)
FROM detalle_matricula dm JOIN evaluacion e ON e.id_seccion=dm.id_seccion;
UPDATE detalle_matricula dm SET promedio_final=(SELECT ROUND(SUM(c.nota*e.ponderado),2) FROM calificacion c JOIN evaluacion e ON e.id_evaluacion=c.id_evaluacion WHERE c.id_detalle_matricula=dm.id_detalle_matricula), estado_curso='APROBADO' WHERE dm.id_detalle_matricula > 0;

INSERT INTO pension(id_alumno,id_matricula,numero_cuota,monto,fecha_vencimiento,estado)
SELECT id_alumno,id_matricula,1,250.00,'2026-09-30',IF(MOD(id_alumno,3)=0,'PAGADO','PENDIENTE') FROM matricula;
UPDATE pension SET fecha_pago='2026-09-20' WHERE id_pension > 0 AND estado='PAGADO';

-- Alumno habilitado para probar el portal sin una matrícula activa.
INSERT INTO usuario(username,password_hash,rol) VALUES('ALU0241','123','ALUMNO');
INSERT INTO alumno(id_usuario,id_carrera,codigo,nombres,apellidos,dni,email,telefono,fecha_nacimiento)
VALUES(LAST_INSERT_ID(),1,'A00241','Alumno sin matrícula','Prueba','80000241','alumno241@universidad.edu','988000000','2002-01-01');
