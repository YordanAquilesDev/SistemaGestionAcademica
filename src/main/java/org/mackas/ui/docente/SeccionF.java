package org.mackas.ui.docente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import org.mackas.model.Alumno;
import org.mackas.model.Docente;
import org.mackas.model.Evaluacion;
import org.mackas.model.Seccion;
import org.mackas.service.factory.ServiceFactory;
import org.mackas.service.interf.IAlumnoDao;
import org.mackas.service.interf.ICalificacionDao;
import org.mackas.service.interf.IMatriculaDao;
import org.mackas.service.interf.ISeccionDao;
import org.mackas.service.proxy.CalificacionProxy;

public class SeccionF extends JPanel {

    private Docente docente;
    private ISeccionDao seccionService;
    private IAlumnoDao alumnoService;

    public SeccionF(Docente docente, ISeccionDao seccionService, IAlumnoDao alumnoService) {
        this.docente = docente;
        this.seccionService = seccionService;
        this.alumnoService = alumnoService;

        initComponentsPersonalizado();
        cargarSecciones(); // Vista inicial por defecto dentro de SeccionF
    }

    private void initComponentsPersonalizado() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    /**
     * Muestra la cuadrícula con las secciones asignadas al docente.
     */
    public void cargarSecciones() {
        removeAll();
        JPanel panelGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 18));
        panelGrid.setBackground(new Color(248, 250, 252));

        List<Seccion> seccionDocente = null;
        if (docente != null) {
            if (docente.getIdDocente() > 0) {
                seccionDocente = seccionService.findByIdDocente(docente.getIdDocente());
            }
        }

        if (seccionDocente != null && !seccionDocente.isEmpty()) {
            for (Seccion sec : seccionDocente) {
                List<Alumno> alumnos = alumnoService.findByIdSeccion(sec.getId_seccion());
                int matriculados = alumnos == null ? 0 : alumnos.size();
                SeccionCard seccionCard = new SeccionCard(sec, matriculados,
                        seleccionada -> cargarAlumnosDeSeccion(seleccionada));
                panelGrid.add(seccionCard);
            }
        } else {
            JLabel lblVacio = new JLabel("No hay secciones asignadas para este docente.");
            lblVacio.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            panelGrid.add(lblVacio);
        }
        JLabel encabezado = new JLabel("Mis secciones");
        encabezado.setFont(new Font("Segoe UI", Font.BOLD, 22));
        encabezado.setForeground(new Color(30, 41, 59));
        JLabel descripcion = new JLabel("Selecciona una sección para gestionar alumnos y calificaciones.");
        descripcion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descripcion.setForeground(new Color(100, 116, 139));
        JPanel cabecera = new JPanel(new GridLayout(2, 1, 0, 4));
        cabecera.setBackground(new Color(248, 250, 252));
        cabecera.add(encabezado);
        cabecera.add(descripcion);
        add(cabecera, BorderLayout.NORTH);
        add(panelGrid, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Muestra la cuadrícula con los alumnos de la sección seleccionada (Todo dentro de SeccionF).
     */
    public void cargarAlumnosDeSeccion(Seccion seccion) {
        removeAll();
        List<Alumno> alumnosSeccion = alumnoService.findByIdSeccion(seccion.getId_seccion());
        IMatriculaDao matriculaService= ServiceFactory.MatriculaServiceFactory();
        ICalificacionDao calificacionService = new CalificacionProxy(
                ServiceFactory.CalificacionServiceFactory(), docente);

        if (alumnosSeccion != null && !alumnosSeccion.isEmpty()) {
            List<Evaluacion> evaluaciones = ServiceFactory.EvaluacionServiceFactory()
                    .listarPorSeccion(seccion.getId_seccion());
            String[] columnas = new String[3 + evaluaciones.size() + 2];
            columnas[0] = "Código";
            columnas[1] = "Alumno";
            columnas[2] = "DNI";
            for (int i = 0; i < evaluaciones.size(); i++) {
                Evaluacion evaluacion = evaluaciones.get(i);
                columnas[3 + i] = evaluacion.getNombre() + " (" + Math.round(evaluacion.getPonderado() * 100) + "%)";
            }
            columnas[columnas.length - 2] = "Promedio";
            columnas[columnas.length - 1] = "Acción";
            DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
                @Override public boolean isCellEditable(int fila, int columna) { return columna == columnas.length - 1; }
            };
            for (Alumno alumno : alumnosSeccion) {
                List<org.mackas.model.Calificacion> notas = calificacionService.findByIdSeccionAlumno(seccion.getId_seccion(), alumno.getIdAlumno());
                Map<Integer, org.mackas.model.Calificacion> notasPorEvaluacion = new HashMap<>();
                if (notas != null) {
                    for (org.mackas.model.Calificacion nota : notas) {
                        notasPorEvaluacion.put(nota.getId_evaluacion(), nota);
                    }
                }
                Object[] fila = new Object[columnas.length];
                fila[0] = alumno.getCodigo();
                fila[1] = alumno.getNombres() + " " + alumno.getApellidos();
                fila[2] = alumno.getDni();
                double promedio = 0;
                for (int i = 0; i < evaluaciones.size(); i++) {
                    Evaluacion evaluacion = evaluaciones.get(i);
                    org.mackas.model.Calificacion nota = notasPorEvaluacion.get(evaluacion.getIdEvaluacion());
                    fila[3 + i] = nota == null ? "Pendiente" : String.format("%.2f", nota.getNota());
                    if (nota != null) {
                        promedio += nota.getNota() * evaluacion.getPonderado();
                    }
                }
                fila[columnas.length - 2] = String.format("%.2f", promedio);
                fila[columnas.length - 1] = "Registrar nota";
                modelo.addRow(fila);
            }
            final List<Alumno> alumnos = alumnosSeccion;
            JTable tabla = new JTable(modelo);
            tabla.setRowHeight(42);
            tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tabla.setShowGrid(false);
            tabla.setIntercellSpacing(new Dimension(0, 0));
            tabla.setSelectionBackground(new Color(224, 231, 255));
            tabla.setSelectionForeground(new Color(30, 41, 59));
            tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            tabla.getTableHeader().setBackground(new Color(15, 23, 42));
            tabla.getTableHeader().setForeground(Color.WHITE);
            tabla.getTableHeader().setPreferredSize(new Dimension(0, 42));
            tabla.getColumnModel().getColumn(columnas.length - 1).setPreferredWidth(130);
            DefaultTableCellRenderer notaRenderer = new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                    label.setHorizontalAlignment(CENTER);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if (!selected && column >= 3) {
                        String texto = String.valueOf(value);
                        if ("Pendiente".equals(texto)) { label.setForeground(new Color(100, 116, 139)); }
                        else { try { double nota = Double.parseDouble(texto); label.setForeground(nota >= 14 ? new Color(22, 163, 74) : nota >= 11 ? new Color(202, 138, 4) : new Color(220, 38, 38)); } catch (NumberFormatException ex) { } }
                    }
                    return label;
                }
            };
            for (int i = 3; i < columnas.length - 1; i++) tabla.getColumnModel().getColumn(i).setCellRenderer(notaRenderer);
            tabla.getColumnModel().getColumn(columnas.length - 1).setCellRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
                    JButton boton = new JButton("Registrar nota"); boton.setBackground(new Color(79, 70, 229)); boton.setForeground(Color.WHITE); boton.setFocusPainted(false); return boton;
                }
            });
            class BotonEditor extends AbstractCellEditor implements TableCellEditor {
                private final JButton boton = new JButton("Registrar nota"); private int fila;
                { boton.setBackground(new Color(79, 70, 229)); boton.setForeground(Color.WHITE); boton.addActionListener(e -> { fireEditingStopped(); new AlumnosF(alumnos.get(fila), seccion.getId_seccion(), calificacionService, matriculaService, () -> cargarAlumnosDeSeccion(seccion)).registrarNota(); }); }
                @Override public Object getCellEditorValue() { return "Registrar nota"; }
                @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column) { fila = row; return boton; }
            }
            tabla.getColumnModel().getColumn(columnas.length - 1).setCellEditor(new BotonEditor());
            add(new JScrollPane(tabla), BorderLayout.CENTER);
        } else {
            JLabel lblVacio = new JLabel("No hay alumnos registrados en esta sección.");
            lblVacio.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            add(lblVacio, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    /**
     * Clase interna estática que representa la tarjeta visual de cada sección.
     */
    public static class SeccionCard extends JPanel {

        private Seccion seccion;
        private JLabel txtCodigoSeccion;
        private JLabel txtAula;
        private JLabel txtCupoMax;
        private JLabel txtDisponibles;
        private JButton btnVerAlumnos;

        // Interfaz funcional para manejar el evento Clic de la tarjeta
        public interface OnSeccionClickListener {
            void onSeccionClick(Seccion seccion);
        }

        public SeccionCard(Seccion seccion, int matriculados, OnSeccionClickListener listener) {
            this.seccion = seccion;
            initComponents();

            // Seteamos los datos de la sección en los Labels
            txtCodigoSeccion.setText("Sección " + seccion.getCodigo_seccion());
            txtAula.setText("Aula: " + (seccion.getAula() != null ? seccion.getAula() : "S/N"));
            int disponibles = Math.max(0, seccion.getCupo_max() - matriculados);
            txtCupoMax.setText("Matriculados: " + matriculados + " / " + seccion.getCupo_max());
            txtDisponibles.setText("Disponibles: " + disponibles);

            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
            ));

            Dimension dim = new Dimension(410, 145);
            setPreferredSize(dim);
            setMinimumSize(dim);
            setMaximumSize(dim);
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            // Efecto Hover y Evento Clic
            this.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (listener != null) {
                        listener.onSeccionClick(seccion);
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(new Color(241, 245, 249)); // Slate-100
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(Color.WHITE);
                }
            });
            btnVerAlumnos.addActionListener(e -> listener.onSeccionClick(seccion));
        }

        private void initComponents() {
            txtCodigoSeccion = new JLabel();
            txtAula = new JLabel();
            txtCupoMax = new JLabel();
            txtDisponibles = new JLabel();
            btnVerAlumnos = new JButton("Ver alumnos y notas");

            txtCodigoSeccion.setFont(new Font("Segoe UI", Font.BOLD, 15));
            txtCodigoSeccion.setForeground(new Color(15, 23, 42));

            txtAula.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtAula.setForeground(new Color(100, 116, 139));

            txtCupoMax.setFont(new Font("Segoe UI", Font.BOLD, 12));
            txtCupoMax.setForeground(new Color(16, 185, 129));
            txtDisponibles.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            txtDisponibles.setForeground(new Color(100, 116, 139));
            btnVerAlumnos.setBackground(new Color(79, 70, 229));
            btnVerAlumnos.setForeground(Color.WHITE);
            btnVerAlumnos.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnVerAlumnos.setFocusPainted(false);

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                             .addComponent(txtCodigoSeccion)
                                             .addGroup(layout.createSequentialGroup()
                                                     .addComponent(txtAula)
                                                     .addGap(30, 30, 30)
                                                    .addComponent(txtCupoMax))
                                            .addComponent(txtDisponibles)
                                            .addComponent(btnVerAlumnos, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE))
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(txtCodigoSeccion)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                     .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                             .addComponent(txtAula)
                                             .addComponent(txtCupoMax))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(txtDisponibles)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(btnVerAlumnos, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }
    }
}
