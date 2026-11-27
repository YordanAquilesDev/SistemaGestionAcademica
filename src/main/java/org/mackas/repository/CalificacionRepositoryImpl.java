/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.mackas.repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mackas.model.Calificacion;
import org.mackas.service.interf.ICalificacionDao;
import org.mackas.util.singlenton.ConexionMySQL;

/**
 *
 * @author Administrador
 */
public class CalificacionRepositoryImpl implements ICalificacionDao {

    private Connection mysql;
    public CalificacionRepositoryImpl(Connection conx){
        mysql=conx;
    }

    @Override
    public List<Calificacion> findByIdCursoAlumno(int idCurso, int idAlumno) {
        List<Calificacion> lista = new ArrayList<>();
        String sql = "{CALL sp_listar_calificacion_curso(?, ?)}"; 

        try (CallableStatement stmt = mysql.prepareCall(sql)) {
            // Asignamos ambos parámetros de entrada del procedimiento
            stmt.setInt(1, idCurso);
            stmt.setInt(2, idAlumno); 

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Calificacion cal = new Calificacion();
                    cal.setId_calificacion(rs.getInt("id_calificacion"));
                    cal.setId_detalle_matricula(rs.getInt("id_detalle_matricula"));
                    cal.setId_evaluacion(rs.getInt("id_evaluacion"));
                    cal.setMombre_evaluacion(rs.getString("nombre_evaluacion"));
                    cal.setPonderado(rs.getDouble("ponderado"));
                    cal.setNota(rs.getDouble("nota"));
                    cal.setFecha_registro(rs.getDate("fecha_registro"));

                    lista.add(cal);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    @Override
    public List<Calificacion> findByIdSeccionAlumno(int idSeccion, int idAlumno) {
        List<Calificacion> lista = new ArrayList<>();
        try (CallableStatement stmt = mysql.prepareCall("{CALL sp_listar_notas_alumno_seccion(?, ?)}")) {
            stmt.setInt(1, idAlumno);
            stmt.setInt(2, idSeccion);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Calificacion cal = new Calificacion();
                    cal.setId_evaluacion(rs.getInt("id_evaluacion"));
                    cal.setMombre_evaluacion(rs.getString("nombre"));
                    cal.setPonderado(rs.getDouble("ponderado"));
                    cal.setNota(rs.getDouble("nota"));
                    lista.add(cal);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron obtener las notas de la sección", e);
        }
        return lista;
    }

    @Override
    public Calificacion save(Calificacion beans) {
        String sql = "{CALL sp_registrar_calificacion(?, ?, ?)}";

        // The connection belongs to the factory and must remain available for later operations.
        try (CallableStatement stmt = mysql.prepareCall(sql)) {

            // Mapeamos los parámetros en el orden del procedimiento almacenado
            stmt.setInt(1, beans.getId_detalle_matricula());
            stmt.setInt(2, beans.getId_evaluacion());
            stmt.setDouble(3, beans.getNota());

            // Ejecutamos el procedimiento de inserción
            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                // Si deseas puedes retornar el mismo objeto indicando éxito
                return beans;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Retorna null si falló
    }

}
