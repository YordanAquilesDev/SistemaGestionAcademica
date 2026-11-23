package org.mackas.ui.docente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.table.DefaultTableModel;
import org.mackas.model.Alumno;
import org.mackas.model.Docente;
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
        JPanel panelGrid = new JPanel(new GridLayout(0, 2, 25, 25));
        panelGrid.setBackground(new Color(248, 250, 252));

        List<Seccion> seccionDocente = null;
        if (docente != null) {
            if (docente.getIdDocente() > 0) {
                seccionDocente = seccionService.findByIdDocente(docente.getIdDocente());
            }
        }

        if (seccionDocente != null && !seccionDocente.isEmpty()) {
            for (Seccion sec : seccionDocente) {
                SeccionCard seccionCard = new SeccionCard(sec,
                        seleccionada -> cargarAlumnosDeSeccion(seleccionada));
                panelGrid.add(seccionCard);
            }
        } else {
            JLabel lblVacio = new JLabel("No hay secciones asignadas para este docente.");
            lblVacio.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            panelGrid.add(lblVacio);
        }
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
            String[] columnas = {"Código", "Alumno", "DNI", "Correo", "Evaluaciones", "Promedio", "Acción"};
            DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
                @Override public boolean isCellEditable(int fila, int columna) { return false; }
            };
            for (Alumno alumno : alumnosSeccion) {
                List<org.mackas.model.Calificacion> notas = calificacionService.findByIdCursoAlumno(seccion.getId_curso(), alumno.getIdAlumno());
                int evaluaciones = notas == null ? 0 : notas.size();
                double promedio = notas == null || notas.isEmpty() ? 0 : notas.stream().mapToDouble(org.mackas.model.Calificacion::getNota).average().orElse(0);
                modelo.addRow(new Object[]{alumno.getCodigo(), alumno.getNombres() + " " + alumno.getApellidos(), alumno.getDni(), alumno.getEmail(), evaluaciones, String.format("%.2f", promedio), "Registrar nota"});
            }
            final List<Alumno> alumnos = alumnosSeccion;
            JTable tabla = new JTable(modelo);
            tabla.setRowHeight(36);
            tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            tabla.getTableHeader().setBackground(new Color(15, 23, 42));
            tabla.getTableHeader().setForeground(Color.WHITE);
            tabla.getColumnModel().getColumn(6).setPreferredWidth(130);
            tabla.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    int fila = tabla.rowAtPoint(e.getPoint());
                    if (fila >= 0 && tabla.columnAtPoint(e.getPoint()) == 6) {
                        new AlumnosF(alumnos.get(fila), seccion.getId_seccion(), calificacionService, matriculaService).registrarNota();
                    }
                }
            });
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

        // Interfaz funcional para manejar el evento Clic de la tarjeta
        public interface OnSeccionClickListener {
            void onSeccionClick(Seccion seccion);
        }

        public SeccionCard(Seccion seccion, OnSeccionClickListener listener) {
            this.seccion = seccion;
            initComponents();

            // Seteamos los datos de la sección en los Labels
            txtCodigoSeccion.setText("Sección: " + seccion.getCodigo_seccion());
            txtAula.setText("🏫 Aula: " + (seccion.getAula() != null ? seccion.getAula() : "S/N"));
            txtCupoMax.setText("👥 Cupos: " + seccion.getCupo_max());

            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
            ));

            Dimension dim = new Dimension(480, 95);
            setPreferredSize(dim);
            setMinimumSize(dim);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
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
        }

        private void initComponents() {
            txtCodigoSeccion = new JLabel();
            txtAula = new JLabel();
            txtCupoMax = new JLabel();

            txtCodigoSeccion.setFont(new Font("Segoe UI", Font.BOLD, 15));
            txtCodigoSeccion.setForeground(new Color(15, 23, 42));

            txtAula.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            txtAula.setForeground(new Color(100, 116, 139));

            txtCupoMax.setFont(new Font("Segoe UI", Font.BOLD, 12));
            txtCupoMax.setForeground(new Color(16, 185, 129));

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
                                                    .addComponent(txtCupoMax)))
                                    .addContainerGap(200, Short.MAX_VALUE))
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
                                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }
    }
}
