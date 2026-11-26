package org.mackas.service.proxy;

import java.util.List;
import org.mackas.model.Calificacion;
import org.mackas.model.Docente;
import org.mackas.service.interf.ICalificacionDao;

/** Proxy that protects grade registration with authorization and input rules. */
public class CalificacionProxy implements ICalificacionDao {

    private final ICalificacionDao delegate;
    private final Docente docente;

    public CalificacionProxy(ICalificacionDao delegate, Docente docente) {
        this.delegate = delegate;
        this.docente = docente;
    }

    @Override
    public List<Calificacion> findByIdCursoAlumno(int idCurso, int idAlumno) {
        return delegate.findByIdCursoAlumno(idCurso, idAlumno);
    }

    @Override
    public List<Calificacion> findByIdSeccionAlumno(int idSeccion, int idAlumno) {
        return delegate.findByIdSeccionAlumno(idSeccion, idAlumno);
    }

    @Override
    public Calificacion save(Calificacion calificacion) {
        if (docente == null || docente.getIdDocente() <= 0) {
            throw new SecurityException("Solo un docente autenticado puede registrar calificaciones.");
        }
        if (calificacion.getNota() < 0 || calificacion.getNota() > 20) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 20.");
        }
        if (calificacion.getPonderado() <= 0 || calificacion.getPonderado() > 1) {
            throw new IllegalArgumentException("El ponderado debe ser mayor que 0 y menor o igual a 1.");
        }
        return delegate.save(calificacion);
    }
}
