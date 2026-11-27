package org.mackas.ui.alumno;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.mackas.model.Calificacion;
import org.mackas.model.Evaluacion;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class NotasF extends JPanel {

    private JScrollPane jScrollPane1;
    private JTable jTable1;
    private JLabel txtCurso;
    private JLabel txtEstado;

    public NotasF() {
        initComponents();
        Dimension dimensionFija = new Dimension(1000, 140);
        setPreferredSize(dimensionFija);
        setMinimumSize(dimensionFija);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
    }

    public void setNombreCurso(String nombre) {
        txtCurso.setText(nombre);
    }

    public void mostrarNotas(List<Evaluacion> evaluaciones, List<Calificacion> notas) {
        String[] columnas = new String[evaluaciones.size() + 1];
        Map<Integer, Calificacion> porEvaluacion = new HashMap<>();
        for (Calificacion nota : notas) porEvaluacion.put(nota.getId_evaluacion(), nota);
        Object[] fila = new Object[columnas.length];
        double promedio = 0;
        for (int i = 0; i < evaluaciones.size(); i++) {
            Evaluacion evaluacion = evaluaciones.get(i);
            columnas[i] = "<html><center>" + evaluacion.getNombre() + "<br><span style='font-size:10px'>" + Math.round(evaluacion.getPonderado() * 100) + "%</span></center></html>";
            Calificacion nota = porEvaluacion.get(evaluacion.getIdEvaluacion());
            fila[i] = nota == null ? "Pendiente" : String.format("%.2f", nota.getNota());
            if (nota != null) promedio += nota.getNota() * evaluacion.getPonderado();
        }
        columnas[columnas.length - 1] = "Promedio";
        fila[fila.length - 1] = String.format("%.2f", promedio);
        boolean completo = notas.size() >= evaluaciones.size();
        String estado = !completo ? "EN PROGRESO" : promedio >= 11 ? "APROBADO" : "EN RIESGO";
        Color colorEstado = !completo ? new Color(202, 138, 4) : promedio >= 11 ? new Color(22, 163, 74) : new Color(220, 38, 38);
        txtEstado.setText(estado + "  " + String.format("%.2f", promedio));
        txtEstado.setForeground(colorEstado);
        jTable1.setModel(new DefaultTableModel(new Object[][] {fila}, columnas) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        for (int i = 0; i < jTable1.getColumnCount(); i++) {
            jTable1.getColumnModel().getColumn(i).setCellRenderer(crearRendererNotas());
            jTable1.getColumnModel().getColumn(i).setPreferredWidth(i == jTable1.getColumnCount() - 1 ? 105 : 130);
        }
    }

    private DefaultTableCellRenderer crearRendererNotas() {
        return new DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!selected) {
                    if ("Pendiente".equals(String.valueOf(value))) label.setForeground(new Color(100, 116, 139));
                    else { try { double nota = Double.parseDouble(String.valueOf(value)); label.setForeground(nota >= 14 ? new Color(22, 163, 74) : nota >= 11 ? new Color(202, 138, 4) : new Color(220, 38, 38)); } catch (NumberFormatException ex) { } }
                }
                return label;
            }
        };
    }

    private void initComponents() {
        txtCurso = new JLabel();
        txtEstado = new JLabel();
        jScrollPane1 = new JScrollPane();
        jTable1 = new JTable();

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        txtCurso.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtCurso.setForeground(new Color(15, 23, 42));
        txtCurso.setText("Curso: ");
        txtEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtEstado.setHorizontalAlignment(JLabel.RIGHT);

        jTable1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jTable1.setModel(new DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Sin evaluaciones"
            }
        ));
        jTable1.setRowHeight(28);
        jTable1.setShowGrid(false);
        jTable1.setIntercellSpacing(new Dimension(0, 0));
        jTable1.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        jTable1.getTableHeader().setBackground(new Color(224, 231, 255));
        jTable1.getTableHeader().setForeground(new Color(55, 48, 163));

        // Centrar columnas numéricas o de texto corto

        jScrollPane1.setViewportView(jTable1);
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.getViewport().setBackground(Color.WHITE);
        jScrollPane1.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup().addComponent(txtCurso).addGap(0, 0, Short.MAX_VALUE).addComponent(txtEstado))
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 950, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(txtCurso).addComponent(txtEstado))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }
}
