package org.mackas.model;

import java.sql.Date;

public class Evaluacion {
    private int idEvaluacion;
    private int idSeccion;
    private String nombre;
    private double ponderado;
    private Date fechaEvaluacion;

    public int getIdEvaluacion() { return idEvaluacion; }
    public void setIdEvaluacion(int idEvaluacion) { this.idEvaluacion = idEvaluacion; }
    public int getIdSeccion() { return idSeccion; }
    public void setIdSeccion(int idSeccion) { this.idSeccion = idSeccion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPonderado() { return ponderado; }
    public void setPonderado(double ponderado) { this.ponderado = ponderado; }
    public Date getFechaEvaluacion() { return fechaEvaluacion; }
    public void setFechaEvaluacion(Date fechaEvaluacion) { this.fechaEvaluacion = fechaEvaluacion; }
    @Override public String toString() { return nombre + " (" + Math.round(ponderado * 100) + "%)"; }
}
