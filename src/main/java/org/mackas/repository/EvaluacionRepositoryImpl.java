package org.mackas.repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mackas.model.Evaluacion;
import org.mackas.service.interf.IEvaluacionDao;

public class EvaluacionRepositoryImpl implements IEvaluacionDao {

    private final Connection cn;

    public EvaluacionRepositoryImpl(Connection cn) {
        this.cn = cn;
    }

    @Override
    public List<Evaluacion> listarPorSeccion(int id) {
        List<Evaluacion> r = new ArrayList<>();
        imprimirInfoConexion();

        try (CallableStatement s = cn.prepareCall("{CALL sp_listar_evaluaciones_seccion(?)}")) {
            s.setInt(1, id);
            try (ResultSet x = s.executeQuery()) {
                while (x.next()) {
                    Evaluacion e = new Evaluacion();
                    e.setIdEvaluacion(x.getInt("id_evaluacion"));
                    e.setIdSeccion(id);
                    e.setNombre(x.getString("nombre"));
                    e.setPonderado(x.getDouble("ponderado"));
                    e.setFechaEvaluacion(x.getDate("fecha_evaluacion"));
                    r.add(e);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return r;
    }

    @Override
    public boolean crear(int id, String n, double p, Date f) {
        try (CallableStatement s = cn.prepareCall("{CALL sp_crear_evaluacion(?,?,?,?)}")) {
            s.setInt(1, id);
            s.setString(2, n);
            s.setDouble(3, p);
            s.setDate(4, f);
            s.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private void imprimirInfoConexion() {
        if (cn == null) {
            System.out.println("Conexión cn es NULL");
            return;
        }
        try (PreparedStatement ps = cn.prepareStatement("SELECT DATABASE(), @@hostname, @@port");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("Java cn -> BD: " + rs.getString(1)
                        + " | Host: " + rs.getString(2)
                        + " | Puerto: " + rs.getInt(3));
            }
        } catch (SQLException e) {
            System.out.println("No se pudo leer metadata de cn: " + e.getMessage());
        }
    }
}