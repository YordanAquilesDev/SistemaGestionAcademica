package org.mackas.model;


public class Seccion {
    int id_seccion;
    int id_curso;
    int id_docente;
    int id_periodo;
    String codigo_curso;
    String nombre_curso;
    int creditos;
    int ciclo_nivel;
    String nombre_carrera;
    String nombre_docente;
    String codigo_seccion;
    String aula;
    String horario;
    int cupo_max;
    int matriculados;
    int vacantes;

    public String toString(){
        String curso = codigo_curso == null ? "" : codigo_curso + " · ";
        String nombre = nombre_curso == null ? codigo_seccion : nombre_curso;
        String hor = horario == null ? "Sin horario" : horario;
        return curso + nombre + " · " + codigo_seccion + " · " + hor;
    }
    public int getId_seccion() {
        return id_seccion;
    }

    public void setId_seccion(int id_seccion) {
        this.id_seccion = id_seccion;
    }

    public int getId_curso() {
        return id_curso;
    }

    public void setId_curso(int id_curso) {
        this.id_curso = id_curso;
    }

    public int getId_docente() {
        return id_docente;
    }

    public void setId_docente(int id_docente) {
        this.id_docente = id_docente;
    }

    public int getId_periodo() {
        return id_periodo;
    }

    public void setId_periodo(int id_periodo) {
        this.id_periodo = id_periodo;
    }

    public String getCodigo_seccion() {
        return codigo_seccion;
    }

    public void setCodigo_seccion(String codigo_seccion) {
        this.codigo_seccion = codigo_seccion;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }

    public int getCupo_max() {
        return cupo_max;
    }

    public void setCupo_max(int cupo_max) {
        this.cupo_max = cupo_max;
    }

    public String getCodigo_curso() { return codigo_curso; }
    public void setCodigo_curso(String codigo_curso) { this.codigo_curso = codigo_curso; }
    public String getNombre_curso() { return nombre_curso; }
    public void setNombre_curso(String nombre_curso) { this.nombre_curso = nombre_curso; }
    public int getCreditos() { return creditos; }
    public void setCreditos(int creditos) { this.creditos = creditos; }
    public int getCiclo_nivel() { return ciclo_nivel; }
    public void setCiclo_nivel(int ciclo_nivel) { this.ciclo_nivel = ciclo_nivel; }
    public String getNombre_carrera() { return nombre_carrera; }
    public void setNombre_carrera(String nombre_carrera) { this.nombre_carrera = nombre_carrera; }
    public String getNombre_docente() { return nombre_docente; }
    public void setNombre_docente(String nombre_docente) { this.nombre_docente = nombre_docente; }
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
    public int getMatriculados() { return matriculados; }
    public void setMatriculados(int matriculados) { this.matriculados = matriculados; }
    public int getVacantes() { return vacantes; }
    public void setVacantes(int vacantes) { this.vacantes = vacantes; }

}
