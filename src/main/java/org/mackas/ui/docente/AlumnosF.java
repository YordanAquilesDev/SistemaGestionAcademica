package org.mackas.ui.docente;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import org.mackas.model.Alumno;
import org.mackas.model.Calificacion;
import org.mackas.model.DetalleMatricula;
import org.mackas.service.interf.ICalificacionDao;
import org.mackas.service.interf.IMatriculaDao;

public class AlumnosF extends JPanel {

    private Alumno alumno;
    private JLabel txtNombreCompleto;
    private JLabel txtDni;
    private JLabel txtEmail;
    private JLabel txtCodigo;
    private JButton btnCalificar; 
    
    private ICalificacionDao caliService ;
    private IMatriculaDao matrService ;

    int idSeccion = 0;

    public AlumnosF(Alumno alumno, int idSeccion,ICalificacionDao caliService,IMatriculaDao matriService) {
        this.alumno = alumno;
        this.idSeccion = idSeccion;
        this.caliService=caliService;
        this.matrService=matriService;
        initComponents();

        txtNombreCompleto.setText(alumno.getNombres() + " " + alumno.getApellidos());
        txtDni.setText("DNI: " + alumno.getDni());
        txtEmail.setText("✉ " + alumno.getEmail());
        txtCodigo.setText("Cod: " + alumno.getCodigo());

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        Dimension dim = new Dimension(480, 110);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        btnCalificar.addActionListener(e -> abrirFormularioCalificacion(alumno));
    }

    private void abrirFormularioCalificacion(Alumno a) {
        Frame parentFrame = (Frame) javax.swing.SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, "Registrar calificación", true);
        dialog.setSize(440, 355);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTituloModal = new JLabel("Registro de calificación");
        lblTituloModal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTituloModal.setForeground(new Color(15, 23, 42));
        JLabel lblAlumno = new JLabel(a.getNombres() + " " + a.getApellidos() + "  |  " + a.getCodigo());
        lblAlumno.setForeground(new Color(100, 116, 139));

        JLabel lblEvaluacion = new JLabel("Nombre Evaluación:");
        JTextField txtEval = new JTextField(15);
        txtEval.setText("Examen Parcial");

        JLabel lblNota = new JLabel("Nota (0 - 20):");
        JTextField txtNota = new JTextField(15);

        JLabel lblPonderado = new JLabel("Ponderado (Ej: 0.30):");
        JTextField txtPond = new JTextField(15);

        JButton btnGuardar = new JButton("Guardar Calificación");
        btnGuardar.setBackground(new Color(79, 70, 229));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Layout del modal
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; dialog.add(lblTituloModal, gbc);
        gbc.gridy = 1; dialog.add(lblAlumno, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(lblEvaluacion, gbc);
        gbc.gridx = 1; dialog.add(txtEval, gbc);

        gbc.gridx = 0; gbc.gridy = 3; dialog.add(lblNota, gbc);
        gbc.gridx = 1; dialog.add(txtNota, gbc);

        gbc.gridx = 0; gbc.gridy = 4; dialog.add(lblPonderado, gbc);
        gbc.gridx = 1; dialog.add(txtPond, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        dialog.add(btnGuardar, gbc);

        btnGuardar.addActionListener(e -> {
            try {
                String nombreEval = txtEval.getText().trim();
                if (nombreEval.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Ingrese el nombre de la evaluación.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double nota = Double.parseDouble(txtNota.getText().trim());
                double ponderado = Double.parseDouble(txtPond.getText().trim());
                if (nota < 0 || nota > 20) {
                    JOptionPane.showMessageDialog(dialog, "La nota debe estar entre 0 y 20.", "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (ponderado <= 0 || ponderado > 1) {
                    JOptionPane.showMessageDialog(dialog, "El ponderado debe ser mayor que 0 y menor o igual a 1.", "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                DetalleMatricula detaM = matrService.search(idSeccion, a.getIdAlumno());
                if (detaM == null) {
                    JOptionPane.showMessageDialog(dialog, "El alumno no cuenta con un detalle de matrícula en esta sección.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Calificacion cali = new Calificacion();
                cali.setId_detalle_matricula(detaM.getId_detalle_matricula());
                cali.setMombre_evaluacion(nombreEval);
                cali.setNota(nota);
                cali.setPonderado(ponderado);

                Calificacion resultado = caliService.save(cali);

                if (resultado != null) {
                    JOptionPane.showMessageDialog(dialog, "¡Calificación registrada con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "No se pudo registrar la calificación.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Ingrese valores numéricos válidos en Nota y Ponderado.", "Error de formato", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Ocurrió un error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    public void registrarNota() {
        abrirFormularioCalificacion(alumno);
    }

    private void initComponents() {
        txtNombreCompleto = new JLabel();
        txtDni = new JLabel();
        txtEmail = new JLabel();
        txtCodigo = new JLabel();
        btnCalificar = new JButton();

        txtNombreCompleto.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtNombreCompleto.setForeground(new Color(15, 23, 42));

        txtDni.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDni.setForeground(new Color(100, 116, 139));

        txtEmail.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        txtEmail.setForeground(new Color(79, 70, 229));

        txtCodigo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtCodigo.setForeground(new Color(16, 185, 129));

        btnCalificar.setText("Registrar nota");
        btnCalificar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCalificar.setBackground(new Color(79, 70, 229));
        btnCalificar.setForeground(Color.WHITE);
        btnCalificar.setFocusPainted(false);
        btnCalificar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(txtNombreCompleto)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 120, Short.MAX_VALUE)
                            .addComponent(txtCodigo))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(txtDni)
                            .addGap(25, 25, 25)
                            .addComponent(txtEmail)
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addGap(18, 18, 18)
                    .addComponent(btnCalificar, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(txtNombreCompleto)
                                .addComponent(txtCodigo))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(txtDni)
                                .addComponent(txtEmail)))
                        .addComponent(btnCalificar, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(12, Short.MAX_VALUE))
        );
    }
}
