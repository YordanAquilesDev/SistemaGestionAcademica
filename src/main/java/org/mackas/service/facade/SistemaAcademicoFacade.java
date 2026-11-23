package org.mackas.service.facade;

import java.util.List;
import org.mackas.model.Calificacion;
import org.mackas.model.Matricula;
import org.mackas.model.Usuario;
import org.mackas.service.interf.ICalificacionDao;
import org.mackas.service.interf.IMatriculaDao;
import org.mackas.service.interf.IUsuarioDao;

/** Facade for the application operations required by the user interface. */
public class SistemaAcademicoFacade {

    private final IUsuarioDao usuarioService;
    private final IMatriculaDao matriculaService;
    private final ICalificacionDao calificacionService;

    public SistemaAcademicoFacade(IUsuarioDao usuarioService, IMatriculaDao matriculaService,
            ICalificacionDao calificacionService) {
        this.usuarioService = usuarioService;
        this.matriculaService = matriculaService;
        this.calificacionService = calificacionService;
    }

    public Usuario iniciarSesion(String codigo, String password) {
        return usuarioService.login(codigo, password);
    }

    public boolean tieneMatricula(int idAlumno) {
        return matriculaService.isMatricula(idAlumno);
    }

    public Matricula registrarMatricula(Matricula matricula) {
        return matriculaService.setMatricula(matricula);
    }

    public Calificacion registrarCalificacion(Calificacion calificacion) {
        return calificacionService.save(calificacion);
    }

    public List<Calificacion> listarCalificaciones(int idCurso, int idAlumno) {
        return calificacionService.findByIdCursoAlumno(idCurso, idAlumno);
    }
}
