package org.mackas.ui.docente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import org.mackas.Login;
import org.mackas.model.Alumno;
import org.mackas.model.Docente;
import org.mackas.model.Seccion;
import org.mackas.service.factory.ServiceFactory;
import org.mackas.service.interf.IAlumnoDao;
import org.mackas.service.interf.IMatriculaDao;
import org.mackas.service.interf.ISeccionDao;
import org.mackas.service.interf.IUsuarioDao;

public class DocenteUIF extends JFrame {
    private static final Color NAVY = new Color(15, 23, 42);
    private static final Color BACKGROUND = new Color(248, 250, 252);

    private final ISeccionDao seccionService;
    private final Docente docente;
    private final JPanel contenido = new JPanel(new BorderLayout());
    private final JLabel titulo = new JLabel();

    public DocenteUIF(Docente docente, ISeccionDao seccionService) {
        this.docente = docente;
        this.seccionService = seccionService;
        setTitle("Portal del Docente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        initComponents();
        mostrarResumen();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);
        setJMenuBar(crearMenu());
        add(crearCabecera(), BorderLayout.NORTH);

        contenido.setBackground(BACKGROUND);
        contenido.setBorder(BorderFactory.createEmptyBorder(0, 28, 28, 28));
        add(contenido, BorderLayout.CENTER);
    }

    private JMenuBar crearMenu() {
        JMenuBar menu = new JMenuBar();
        menu.setBackground(NAVY);
        menu.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JMenu resumen = crearOpcion("Resumen");
        JMenu secciones = crearOpcion("Mis secciones");
        JMenu salir = crearOpcion("Salir");
        resumen.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseClicked(java.awt.event.MouseEvent e) { mostrarResumen(); } });
        secciones.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseClicked(java.awt.event.MouseEvent e) { mostrarSecciones(); } });
        salir.addMouseListener(new java.awt.event.MouseAdapter() { public void mouseClicked(java.awt.event.MouseEvent e) { salir(); } });
        menu.add(resumen);
        menu.add(secciones);
        menu.add(Box.createHorizontalGlue());
        menu.add(salir);
        return menu;
    }

    private JMenu crearOpcion(String texto) {
        JMenu item = new JMenu(texto);
        item.setForeground(new Color(226, 232, 240));
        item.setFont(new Font("Segoe UI", Font.BOLD, 13));
        item.setOpaque(true);
        item.setBackground(NAVY);
        item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { item.setBackground(new Color(30, 41, 59)); }
            public void mouseExited(java.awt.event.MouseEvent e) { item.setBackground(NAVY); }
        });
        return item;
    }

    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(BACKGROUND);
        cabecera.setBorder(BorderFactory.createEmptyBorder(24, 28, 18, 28));
        String nombre = docente == null ? "Docente" : docente.getNombres() + " " + docente.getApellidos();
        String especialidad = docente == null || docente.getEspecialidad() == null ? "Perfil docente" : docente.getEspecialidad();
        JLabel perfil = new JLabel("<html><b style='font-size:20px'>" + nombre + "</b><br><span style='color:#64748b'>" + especialidad + "</span></html>");
        String codigo = docente == null || docente.getCodigo() == null ? "Sin código" : docente.getCodigo();
        JLabel etiqueta = new JLabel("DOCENTE  " + codigo, SwingConstants.CENTER);
        etiqueta.setOpaque(true);
        etiqueta.setBackground(new Color(224, 231, 255));
        etiqueta.setForeground(new Color(67, 56, 202));
        etiqueta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        etiqueta.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        cabecera.add(perfil, BorderLayout.WEST);
        cabecera.add(etiqueta, BorderLayout.EAST);
        return cabecera;
    }

    private void mostrarResumen() {
        titulo.setText("Panel académico");
        contenido.removeAll();
        if (!docenteValido()) {
            contenido.add(mensajePerfilInvalido(), BorderLayout.CENTER);
        } else {
            IAlumnoDao alumnoService = ServiceFactory.AlumnoServiceFactory();
            List<Seccion> secciones = seccionService.findByIdDocente(docente.getIdDocente());
            if (secciones == null) {
                secciones = java.util.Collections.emptyList();
            }
            int totalAlumnos = secciones.stream().mapToInt(s -> alumnoService.findByIdSeccion(s.getId_seccion()).size()).sum();
            JPanel panel = new JPanel(new BorderLayout(0, 20));
            panel.setBackground(BACKGROUND);
            JLabel bienvenida = new JLabel("<html><b style='font-size:22px'>Panel académico</b><br><span style='color:#64748b'>Gestiona tus secciones y registra calificaciones.</span></html>");
            JPanel metricas = new JPanel(new GridLayout(1, 4, 18, 0));
            metricas.setBackground(BACKGROUND);
            metricas.add(crearMetrica("Secciones asignadas", String.valueOf(secciones.size()), new Color(79, 70, 229)));
            metricas.add(crearMetrica("Alumnos activos", String.valueOf(totalAlumnos), new Color(14, 116, 144)));
            metricas.add(crearMetrica("Capacidad disponible", String.valueOf(secciones.stream().mapToInt(Seccion::getCupo_max).sum() - totalAlumnos), new Color(5, 150, 105)));
            metricas.add(crearMetrica("Ocupación", totalAlumnos + " / " + secciones.stream().mapToInt(Seccion::getCupo_max).sum(), new Color(202, 138, 4)));
            JButton verSecciones = new JButton("Ver mis secciones");
            verSecciones.setBackground(new Color(79, 70, 229));
            verSecciones.setForeground(Color.WHITE);
            verSecciones.setFont(new Font("Segoe UI", Font.BOLD, 13));
            verSecciones.setFocusPainted(false);
            verSecciones.addActionListener(e -> mostrarSecciones());
            panel.add(bienvenida, BorderLayout.NORTH);
            panel.add(metricas, BorderLayout.CENTER);
            JPanel inferior = new JPanel(new BorderLayout(18, 0));
            inferior.setBackground(BACKGROUND);
            JPanel progreso = new JPanel(new GridLayout(Math.max(1, secciones.size()), 1, 0, 8));
            progreso.setBackground(Color.WHITE);
            progreso.setBorder(BorderFactory.createTitledBorder("Ocupación por sección"));
            for (Seccion seccion : secciones) {
                int matriculados = alumnoService.findByIdSeccion(seccion.getId_seccion()).size();
                JProgressBar barra = new JProgressBar(0, seccion.getCupo_max());
                barra.setValue(matriculados);
                barra.setString(seccion.getCodigo_seccion() + "   " + matriculados + " / " + seccion.getCupo_max() + " alumnos");
                barra.setStringPainted(true);
                barra.setForeground(new Color(79, 70, 229));
                progreso.add(barra);
            }
            JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
            acciones.setBackground(BACKGROUND);
            acciones.add(verSecciones);
            inferior.add(progreso, BorderLayout.CENTER);
            inferior.add(acciones, BorderLayout.SOUTH);
            panel.add(inferior, BorderLayout.SOUTH);
            contenido.add(panel, BorderLayout.NORTH);
        }
        refrescarContenido();
    }

    private JPanel crearMetrica(String etiqueta, String valor, Color acento) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 8));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)), BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        JLabel valorLabel = new JLabel(valor);
        valorLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valorLabel.setForeground(acento);
        JLabel etiquetaLabel = new JLabel(etiqueta);
        etiquetaLabel.setForeground(new Color(100, 116, 139));
        tarjeta.add(etiquetaLabel, BorderLayout.NORTH);
        tarjeta.add(valorLabel, BorderLayout.CENTER);
        return tarjeta;
    }

    private void mostrarSecciones() {
        contenido.removeAll();
        if (docenteValido()) {
            SeccionF secciones = new SeccionF(docente, seccionService, ServiceFactory.AlumnoServiceFactory());
            contenido.add(new JScrollPane(secciones), BorderLayout.CENTER);
        } else {
            contenido.add(mensajePerfilInvalido(), BorderLayout.CENTER);
        }
        refrescarContenido();
    }

    private boolean docenteValido() { return docente != null && docente.getIdDocente() > 0; }
    private JPanel mensajePerfilInvalido() { JPanel p = new JPanel(); p.add(new JLabel("No se pudo cargar un perfil docente válido.")); return p; }
    private void refrescarContenido() { contenido.revalidate(); contenido.repaint(); }
    private void salir() { IUsuarioDao usuarios = ServiceFactory.UserServiceFactory(); IMatriculaDao matriculas = ServiceFactory.MatriculaServiceFactory(); new Login(usuarios, matriculas).setVisible(true); dispose(); }
}
