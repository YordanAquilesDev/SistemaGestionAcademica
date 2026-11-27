SET NAMES utf8mb4;
-- Este script reinicia solo la base de demostración para una ejecución limpia.
DROP DATABASE IF EXISTS db_sistema_uni_final;

CREATE DATABASE db_sistema_uni_final
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE db_sistema_uni_final;

CREATE TABLE carrera (
  id_carrera INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL, codigo VARCHAR(20) NOT NULL UNIQUE, estado BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL,
  rol ENUM('ALUMNO','DOCENTE','ADMIN') NOT NULL, estado BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE docente (
  id_docente INT AUTO_INCREMENT PRIMARY KEY, id_usuario INT NOT NULL UNIQUE,
  codigo VARCHAR(20) NOT NULL UNIQUE, nombres VARCHAR(100) NOT NULL, apellidos VARCHAR(100) NOT NULL,
  dni VARCHAR(15) NOT NULL UNIQUE, email VARCHAR(100) NOT NULL UNIQUE, telefono VARCHAR(20), especialidad VARCHAR(100),
  CONSTRAINT fk_docente_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE alumno (
  id_alumno INT AUTO_INCREMENT PRIMARY KEY, id_usuario INT NOT NULL UNIQUE, id_carrera INT NOT NULL,
  codigo VARCHAR(20) NOT NULL UNIQUE, nombres VARCHAR(100) NOT NULL, apellidos VARCHAR(100) NOT NULL,
  dni VARCHAR(15) NOT NULL UNIQUE, email VARCHAR(100) NOT NULL UNIQUE, telefono VARCHAR(20), fecha_nacimiento DATE,
  CONSTRAINT fk_alumno_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
  CONSTRAINT fk_alumno_carrera FOREIGN KEY (id_carrera) REFERENCES carrera(id_carrera)
) ENGINE=InnoDB;

CREATE TABLE curso (
  id_curso INT AUTO_INCREMENT PRIMARY KEY, id_carrera INT NOT NULL, codigo VARCHAR(20) NOT NULL UNIQUE,
  nombre VARCHAR(100) NOT NULL, creditos TINYINT NOT NULL, ciclo_nivel TINYINT NOT NULL,
  CONSTRAINT fk_curso_carrera FOREIGN KEY (id_carrera) REFERENCES carrera(id_carrera),
  CONSTRAINT chk_creditos CHECK (creditos BETWEEN 1 AND 8)
) ENGINE=InnoDB;

CREATE TABLE periodo_academico (
  id_periodo INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(50) NOT NULL UNIQUE,
  fecha_inicio DATE NOT NULL, fecha_fin DATE NOT NULL, estado ENUM('PLANIFICADO','ACTIVO','CERRADO') NOT NULL DEFAULT 'PLANIFICADO',
  CONSTRAINT chk_periodo_fechas CHECK (fecha_fin > fecha_inicio)
) ENGINE=InnoDB;

CREATE TABLE seccion (
  id_seccion INT AUTO_INCREMENT PRIMARY KEY, id_curso INT NOT NULL, id_docente INT NOT NULL, id_periodo INT NOT NULL,
  codigo_seccion VARCHAR(20) NOT NULL, aula VARCHAR(30), cupo_maximo INT NOT NULL DEFAULT 30,
  UNIQUE KEY uk_seccion_periodo_codigo (id_periodo,codigo_seccion),
  CONSTRAINT fk_seccion_curso FOREIGN KEY (id_curso) REFERENCES curso(id_curso),
  CONSTRAINT fk_seccion_docente FOREIGN KEY (id_docente) REFERENCES docente(id_docente),
  CONSTRAINT fk_seccion_periodo FOREIGN KEY (id_periodo) REFERENCES periodo_academico(id_periodo),
  CONSTRAINT chk_cupo CHECK (cupo_maximo > 0)
) ENGINE=InnoDB;

CREATE TABLE horario (
  id_horario INT AUTO_INCREMENT PRIMARY KEY, id_seccion INT NOT NULL,
  dia_semana ENUM('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO') NOT NULL,
  hora_inicio TIME NOT NULL, hora_fin TIME NOT NULL, aula VARCHAR(30),
  CONSTRAINT fk_horario_seccion FOREIGN KEY (id_seccion) REFERENCES seccion(id_seccion) ON DELETE CASCADE,
  CONSTRAINT chk_horario CHECK (hora_fin > hora_inicio)
) ENGINE=InnoDB;

CREATE TABLE matricula (
  id_matricula INT AUTO_INCREMENT PRIMARY KEY, id_alumno INT NOT NULL, id_periodo INT NOT NULL,
  fecha_matricula DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, monto_total DECIMAL(10,2) NOT NULL DEFAULT 0,
  estado ENUM('REGISTRADA','ANULADA') NOT NULL DEFAULT 'REGISTRADA', UNIQUE KEY uk_alumno_periodo (id_alumno,id_periodo),
  CONSTRAINT fk_matricula_alumno FOREIGN KEY (id_alumno) REFERENCES alumno(id_alumno),
  CONSTRAINT fk_matricula_periodo FOREIGN KEY (id_periodo) REFERENCES periodo_academico(id_periodo)
) ENGINE=InnoDB;

CREATE TABLE detalle_matricula (
  id_detalle_matricula INT AUTO_INCREMENT PRIMARY KEY, id_matricula INT NOT NULL, id_seccion INT NOT NULL,
  promedio_final DECIMAL(4,2), estado_curso ENUM('CURSANDO','APROBADO','DESAPROBADO','RETIRADO') NOT NULL DEFAULT 'CURSANDO',
  UNIQUE KEY uk_matricula_seccion (id_matricula,id_seccion),
  CONSTRAINT fk_detalle_matricula FOREIGN KEY (id_matricula) REFERENCES matricula(id_matricula) ON DELETE CASCADE,
  CONSTRAINT fk_detalle_seccion FOREIGN KEY (id_seccion) REFERENCES seccion(id_seccion)
) ENGINE=InnoDB;

CREATE TABLE evaluacion (
  id_evaluacion INT AUTO_INCREMENT PRIMARY KEY, id_seccion INT NOT NULL, nombre VARCHAR(50) NOT NULL,
  ponderado DECIMAL(5,4) NOT NULL, fecha_evaluacion DATE, estado ENUM('ACTIVA','CERRADA') NOT NULL DEFAULT 'ACTIVA',
  UNIQUE KEY uk_evaluacion_seccion_nombre (id_seccion,nombre),
  CONSTRAINT fk_evaluacion_seccion FOREIGN KEY (id_seccion) REFERENCES seccion(id_seccion) ON DELETE CASCADE,
  CONSTRAINT chk_ponderado CHECK (ponderado > 0 AND ponderado <= 1)
) ENGINE=InnoDB;

CREATE TABLE calificacion (
  id_calificacion INT AUTO_INCREMENT PRIMARY KEY, id_detalle_matricula INT NOT NULL, id_evaluacion INT NOT NULL,
  nota DECIMAL(4,2) NOT NULL, fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_calificacion_detalle_evaluacion (id_detalle_matricula,id_evaluacion),
  CONSTRAINT fk_calificacion_detalle FOREIGN KEY (id_detalle_matricula) REFERENCES detalle_matricula(id_detalle_matricula) ON DELETE CASCADE,
  CONSTRAINT fk_calificacion_evaluacion FOREIGN KEY (id_evaluacion) REFERENCES evaluacion(id_evaluacion) ON DELETE CASCADE,
  CONSTRAINT chk_nota CHECK (nota BETWEEN 0 AND 20)
) ENGINE=InnoDB;

CREATE TABLE pension (
  id_pension INT AUTO_INCREMENT PRIMARY KEY, id_alumno INT NOT NULL, id_matricula INT NOT NULL, numero_cuota TINYINT NOT NULL,
  monto DECIMAL(10,2) NOT NULL, monto_mora DECIMAL(10,2) NOT NULL DEFAULT 0, fecha_vencimiento DATE NOT NULL,
  fecha_pago DATETIME, estado ENUM('PENDIENTE','PAGADO','VENCIDO') NOT NULL DEFAULT 'PENDIENTE',
  UNIQUE KEY uk_pension_cuota (id_matricula,numero_cuota),
  CONSTRAINT fk_pension_alumno FOREIGN KEY (id_alumno) REFERENCES alumno(id_alumno),
  CONSTRAINT fk_pension_matricula FOREIGN KEY (id_matricula) REFERENCES matricula(id_matricula),
  CONSTRAINT chk_pension_monto CHECK (monto > 0)
) ENGINE=InnoDB;
