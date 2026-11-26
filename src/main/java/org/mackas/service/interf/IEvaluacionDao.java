package org.mackas.service.interf;
import java.sql.Date;
import java.util.List;
import org.mackas.model.Evaluacion;
public interface IEvaluacionDao {
    List<Evaluacion> listarPorSeccion(int idSeccion);
    boolean crear(int idSeccion, String nombre, double ponderado, Date fecha);
}
