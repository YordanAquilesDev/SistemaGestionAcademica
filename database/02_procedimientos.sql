SET NAMES utf8mb4;
USE db_sistema_uni_final;
DELIMITER //

DROP PROCEDURE IF EXISTS sp_login //
DROP PROCEDURE IF EXISTS sp_crear_evaluacion //
DROP PROCEDURE IF EXISTS sp_listar_evaluaciones_seccion //
DROP PROCEDURE IF EXISTS sp_registrar_calificacion //
DROP PROCEDURE IF EXISTS sp_listar_notas_alumno_seccion //
DROP PROCEDURE IF EXISTS sp_buscar_detalle_matricula //
DROP PROCEDURE IF EXISTS sp_listar_secciones_docente //
DROP PROCEDURE IF EXISTS sp_docente_usuario //
DROP PROCEDURE IF EXISTS sp_alumno_usuario //
DROP PROCEDURE IF EXISTS sp_listar_alumnos_seccion //
DROP PROCEDURE IF EXISTS sp_listar_calificacion_curso //
DROP PROCEDURE IF EXISTS sp_listar_alumnos_notas_seccion //
DROP PROCEDURE IF EXISTS sp_resumen_docente //
DROP PROCEDURE IF EXISTS sp_resumen_secciones_docente //
DROP PROCEDURE IF EXISTS sp_matricula_alumno //
DROP PROCEDURE IF EXISTS sp_listar_cursos_alumno //
DROP PROCEDURE IF EXISTS sp_listar_horarios_alumno //
DROP PROCEDURE IF EXISTS sp_pensiones_pendientes_alumno //
DROP PROCEDURE IF EXISTS sp_resumen_alumno //
DROP PROCEDURE IF EXISTS sp_listar_periodo_academico //
DROP PROCEDURE IF EXISTS sp_listar_secciones_periodo //
DROP PROCEDURE IF EXISTS sp_matricular_alumno //
DROP PROCEDURE IF EXISTS sp_detalle_matricula_alumno //

CREATE PROCEDURE sp_login(IN p_username VARCHAR(50), IN p_password VARCHAR(255))
BEGIN SELECT id_usuario, username, rol, estado FROM usuario WHERE username=p_username AND password_hash=p_password AND estado=TRUE; END //

CREATE PROCEDURE sp_crear_evaluacion(IN p_id_seccion INT, IN p_nombre VARCHAR(50), IN p_ponderado DECIMAL(5,4), IN p_fecha DATE)
BEGIN
  IF (SELECT COALESCE(SUM(ponderado),0) FROM evaluacion WHERE id_seccion=p_id_seccion) + p_ponderado > 1.0000 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La suma de ponderados no puede superar 1.00';
  END IF;
  INSERT INTO evaluacion(id_seccion,nombre,ponderado,fecha_evaluacion) VALUES(p_id_seccion,p_nombre,p_ponderado,p_fecha);
END //

CREATE PROCEDURE sp_listar_evaluaciones_seccion(IN p_id_seccion INT)
BEGIN SELECT id_evaluacion,nombre,ponderado,fecha_evaluacion,estado FROM evaluacion WHERE id_seccion=p_id_seccion ORDER BY fecha_evaluacion,nombre; END //

CREATE PROCEDURE sp_registrar_calificacion(IN p_id_detalle INT, IN p_id_evaluacion INT, IN p_nota DECIMAL(4,2))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM detalle_matricula dm JOIN evaluacion e ON e.id_seccion=dm.id_seccion WHERE dm.id_detalle_matricula=p_id_detalle AND e.id_evaluacion=p_id_evaluacion) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La evaluación no pertenece a la sección del alumno';
  END IF;
  INSERT INTO calificacion(id_detalle_matricula,id_evaluacion,nota) VALUES(p_id_detalle,p_id_evaluacion,p_nota)
  ON DUPLICATE KEY UPDATE nota=VALUES(nota),fecha_registro=CURRENT_TIMESTAMP;
  UPDATE detalle_matricula dm SET promedio_final=(SELECT ROUND(SUM(c.nota*e.ponderado),2) FROM calificacion c JOIN evaluacion e ON e.id_evaluacion=c.id_evaluacion WHERE c.id_detalle_matricula=dm.id_detalle_matricula) WHERE dm.id_detalle_matricula=p_id_detalle;
END //

CREATE PROCEDURE sp_listar_notas_alumno_seccion(IN p_id_alumno INT, IN p_id_seccion INT)
BEGIN
  SELECT e.id_evaluacion,e.nombre,e.ponderado,c.nota,c.fecha_registro FROM detalle_matricula dm JOIN matricula m ON m.id_matricula=dm.id_matricula JOIN evaluacion e ON e.id_seccion=dm.id_seccion LEFT JOIN calificacion c ON c.id_detalle_matricula=dm.id_detalle_matricula AND c.id_evaluacion=e.id_evaluacion WHERE m.id_alumno=p_id_alumno AND dm.id_seccion=p_id_seccion;
END //

CREATE PROCEDURE sp_buscar_detalle_matricula(IN p_id_alumno INT, IN p_id_seccion INT)
BEGIN
  SELECT dm.id_detalle_matricula,dm.promedio_final,dm.estado_curso
  FROM detalle_matricula dm JOIN matricula m ON m.id_matricula=dm.id_matricula
  WHERE m.id_alumno=p_id_alumno AND dm.id_seccion=p_id_seccion;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_listar_secciones_docente(IN p_id_docente INT)
BEGIN
  SELECT s.id_seccion,s.id_curso,s.codigo_seccion,s.aula,s.id_periodo,s.cupo_maximo
  FROM seccion s WHERE s.id_docente=p_id_docente ORDER BY s.codigo_seccion;
END //

CREATE PROCEDURE sp_docente_usuario(IN p_id_usuario INT)
BEGIN SELECT d.* FROM docente d WHERE d.id_usuario=p_id_usuario; END //

CREATE PROCEDURE sp_alumno_usuario(IN p_id_usuario INT)
BEGIN SELECT a.* FROM alumno a WHERE a.id_usuario=p_id_usuario; END //

CREATE PROCEDURE sp_listar_alumnos_seccion(IN p_id_seccion INT)
BEGIN
  SELECT a.id_alumno,a.id_carrera,a.codigo,a.nombres,a.apellidos,a.dni,a.email,a.telefono,a.fecha_nacimiento
  FROM alumno a JOIN matricula m ON m.id_alumno=a.id_alumno JOIN detalle_matricula dm ON dm.id_matricula=m.id_matricula
  WHERE dm.id_seccion=p_id_seccion ORDER BY a.apellidos,a.nombres;
END //

CREATE PROCEDURE sp_listar_calificacion_curso(IN p_id_curso INT, IN p_id_alumno INT)
BEGIN
  SELECT c.id_calificacion,c.id_detalle_matricula,c.id_evaluacion,e.nombre AS nombre_evaluacion,e.ponderado,c.nota,c.fecha_registro
  FROM calificacion c JOIN detalle_matricula dm ON dm.id_detalle_matricula=c.id_detalle_matricula
  JOIN matricula m ON m.id_matricula=dm.id_matricula JOIN seccion s ON s.id_seccion=dm.id_seccion
  JOIN evaluacion e ON e.id_evaluacion=c.id_evaluacion
  WHERE s.id_curso=p_id_curso AND m.id_alumno=p_id_alumno ORDER BY e.fecha_evaluacion,e.nombre;
END //

CREATE PROCEDURE sp_listar_alumnos_notas_seccion(IN p_id_seccion INT, IN p_id_evaluacion INT)
BEGIN
  SELECT a.id_alumno,a.codigo,a.nombres,a.apellidos,a.dni,a.email,c.nota,
    COALESCE((SELECT ROUND(SUM(c2.nota*e2.ponderado),2) FROM calificacion c2 JOIN evaluacion e2 ON e2.id_evaluacion=c2.id_evaluacion WHERE c2.id_detalle_matricula=dm.id_detalle_matricula),0) AS promedio
  FROM detalle_matricula dm JOIN matricula m ON m.id_matricula=dm.id_matricula JOIN alumno a ON a.id_alumno=m.id_alumno
  LEFT JOIN calificacion c ON c.id_detalle_matricula=dm.id_detalle_matricula AND c.id_evaluacion=p_id_evaluacion
  WHERE dm.id_seccion=p_id_seccion ORDER BY a.apellidos,a.nombres;
END //

CREATE PROCEDURE sp_resumen_docente(IN p_id_docente INT)
BEGIN
 SELECT (SELECT COUNT(*) FROM seccion WHERE id_docente=p_id_docente) secciones,
 (SELECT COUNT(*) FROM detalle_matricula dm JOIN seccion s ON s.id_seccion=dm.id_seccion WHERE s.id_docente=p_id_docente) alumnos,
 (SELECT COALESCE(SUM(cupo_maximo),0) FROM seccion WHERE id_docente=p_id_docente) capacidad,
 (SELECT COALESCE(SUM((SELECT COUNT(*) FROM evaluacion e WHERE e.id_seccion=s.id_seccion)*(SELECT COUNT(*) FROM detalle_matricula d WHERE d.id_seccion=s.id_seccion)),0) FROM seccion s WHERE s.id_docente=p_id_docente)-(SELECT COUNT(*) FROM calificacion c JOIN detalle_matricula dm ON dm.id_detalle_matricula=c.id_detalle_matricula JOIN seccion s ON s.id_seccion=dm.id_seccion WHERE s.id_docente=p_id_docente) notas_pendientes,
 (SELECT COALESCE(ROUND(AVG(dm.promedio_final),2),0) FROM detalle_matricula dm JOIN seccion s ON s.id_seccion=dm.id_seccion WHERE s.id_docente=p_id_docente) promedio_general;
END //

CREATE PROCEDURE sp_resumen_secciones_docente(IN p_id_docente INT)
BEGIN
 SELECT s.id_seccion,s.codigo_seccion,s.aula,s.cupo_maximo,COUNT(DISTINCT dm.id_detalle_matricula) matriculados,
 COUNT(DISTINCT e.id_evaluacion) evaluaciones,COUNT(c.id_calificacion) notas_registradas,
 COUNT(DISTINCT dm.id_detalle_matricula)*COUNT(DISTINCT e.id_evaluacion)-COUNT(c.id_calificacion) notas_pendientes,
 COALESCE(ROUND(AVG(dm.promedio_final),2),0) promedio
 FROM seccion s LEFT JOIN detalle_matricula dm ON dm.id_seccion=s.id_seccion LEFT JOIN evaluacion e ON e.id_seccion=s.id_seccion LEFT JOIN calificacion c ON c.id_detalle_matricula=dm.id_detalle_matricula AND c.id_evaluacion=e.id_evaluacion
 WHERE s.id_docente=p_id_docente GROUP BY s.id_seccion,s.codigo_seccion,s.aula,s.cupo_maximo ORDER BY s.codigo_seccion;
END //

CREATE PROCEDURE sp_matricula_alumno(IN p_id_alumno INT)
BEGIN SELECT * FROM matricula WHERE id_alumno=p_id_alumno AND estado='REGISTRADA'; END //

CREATE PROCEDURE sp_listar_cursos_alumno(IN p_id_alumno INT)
BEGIN
 SELECT c.id_curso,c.id_carrera,s.id_seccion,c.codigo codigo_curso,c.nombre nombre_curso,c.creditos,c.ciclo_nivel,ca.nombre nombre_carrera
 FROM detalle_matricula dm JOIN matricula m ON m.id_matricula=dm.id_matricula JOIN seccion s ON s.id_seccion=dm.id_seccion JOIN curso c ON c.id_curso=s.id_curso JOIN carrera ca ON ca.id_carrera=c.id_carrera
 WHERE m.id_alumno=p_id_alumno AND dm.estado_curso<>'RETIRADO';
END //

CREATE PROCEDURE sp_listar_horarios_alumno(IN p_id_alumno INT)
BEGIN
 SELECT h.id_horario,h.id_seccion,h.dia_semana,h.hora_inicio,h.hora_fin,h.aula FROM horario h
 JOIN detalle_matricula dm ON dm.id_seccion=h.id_seccion JOIN matricula m ON m.id_matricula=dm.id_matricula
 WHERE m.id_alumno=p_id_alumno AND dm.estado_curso<>'RETIRADO';
END //

CREATE PROCEDURE sp_pensiones_pendientes_alumno(IN p_id_alumno INT)
BEGIN SELECT * FROM pension WHERE id_alumno=p_id_alumno ORDER BY fecha_vencimiento; END //

CREATE PROCEDURE sp_resumen_alumno(IN p_id_alumno INT)
BEGIN
  SELECT (SELECT COUNT(*) FROM detalle_matricula dm JOIN matricula m ON m.id_matricula=dm.id_matricula WHERE m.id_alumno=p_id_alumno AND dm.estado_curso='CURSANDO') cursos_activos,
  (SELECT COALESCE(ROUND(AVG(promedio_final),2),0) FROM detalle_matricula dm JOIN matricula m ON m.id_matricula=dm.id_matricula WHERE m.id_alumno=p_id_alumno AND dm.promedio_final IS NOT NULL) promedio_general,
  (SELECT COUNT(*) FROM pension WHERE id_alumno=p_id_alumno AND estado<>'PAGADO') pensiones_pendientes;
END //

CREATE PROCEDURE sp_listar_periodo_academico()
BEGIN
  SELECT id_periodo,nombre,fecha_inicio,fecha_fin,(estado='ACTIVO') AS activo
  FROM periodo_academico WHERE estado='ACTIVO' ORDER BY fecha_inicio;
END //

CREATE PROCEDURE sp_listar_secciones_periodo(IN p_id_periodo INT)
BEGIN
  SELECT s.id_seccion,s.id_curso,c.codigo codigo_curso,c.nombre nombre_curso,c.creditos,c.ciclo_nivel,
  ca.nombre nombre_carrera,s.codigo_seccion,s.aula,s.id_periodo,s.cupo_maximo,d.id_docente,
  CONCAT(d.nombres,' ',d.apellidos) nombre_docente,
  COALESCE((SELECT GROUP_CONCAT(CONCAT(h.dia_semana,' ',TIME_FORMAT(h.hora_inicio,'%H:%i'),'-',TIME_FORMAT(h.hora_fin,'%H:%i')) ORDER BY FIELD(h.dia_semana,'LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO'),h.hora_inicio SEPARATOR ' | ') FROM horario h WHERE h.id_seccion=s.id_seccion),'Sin horario') horario,
  (SELECT COUNT(*) FROM detalle_matricula dm WHERE dm.id_seccion=s.id_seccion) matriculados,
  s.cupo_maximo-(SELECT COUNT(*) FROM detalle_matricula dm WHERE dm.id_seccion=s.id_seccion) vacantes
  FROM seccion s
  JOIN curso c ON c.id_curso=s.id_curso
  JOIN carrera ca ON ca.id_carrera=c.id_carrera
  JOIN docente d ON d.id_docente=s.id_docente
  WHERE s.id_periodo=p_id_periodo ORDER BY c.codigo,s.codigo_seccion;
END //

CREATE PROCEDURE sp_matricular_alumno(IN p_id_alumno INT, IN p_id_periodo INT, IN p_monto_total DECIMAL(10,2))
BEGIN
  INSERT INTO matricula(id_alumno,id_periodo,monto_total) VALUES(p_id_alumno,p_id_periodo,p_monto_total);
  SELECT LAST_INSERT_ID() AS id_matricula;
END //

CREATE PROCEDURE sp_detalle_matricula_alumno(IN p_id_matricula INT, IN p_id_seccion INT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM matricula m JOIN seccion s ON s.id_periodo=m.id_periodo
    WHERE m.id_matricula=p_id_matricula AND s.id_seccion=p_id_seccion
  ) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sección no pertenece al período de matrícula';
  END IF;
  IF EXISTS (
    SELECT 1 FROM detalle_matricula dm
    JOIN seccion actual ON actual.id_seccion=dm.id_seccion
    JOIN seccion nueva ON nueva.id_seccion=p_id_seccion
    WHERE dm.id_matricula=p_id_matricula AND actual.id_curso=nueva.id_curso
  ) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El curso ya está registrado con otra sección';
  END IF;
  IF (SELECT COUNT(*) FROM detalle_matricula WHERE id_seccion=p_id_seccion) >=
     (SELECT cupo_maximo FROM seccion WHERE id_seccion=p_id_seccion) THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La sección no tiene vacantes disponibles';
  END IF;
  INSERT INTO detalle_matricula(id_matricula,id_seccion) VALUES(p_id_matricula,p_id_seccion);
END //
DELIMITER ;
