import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

// ============================================================
//  TALLER: Patrón Prototype + Builder con GUI Swing
//  Caso de Estudio: Sistema de Gestión de Expedientes Médicos
//  Curso: Patrones de Software - Cuarto Semestre
// ============================================================

// ── PROTOTYPE ───────────────────────────────────────────────
interface ExpedientePrototype {
    ExpedientePrototype clonar();
}

class ExpedienteBase implements ExpedientePrototype {
    private String tipoPaciente;
    private String tipoAtencion;
    private List<String> examenesBase;

    public ExpedienteBase(String tipoPaciente, String tipoAtencion, List<String> examenesBase) {
        this.tipoPaciente  = tipoPaciente;
        this.tipoAtencion  = tipoAtencion;
        this.examenesBase  = new ArrayList<>(examenesBase);
    }

    private ExpedienteBase(ExpedienteBase o) {
        this.tipoPaciente  = o.tipoPaciente;
        this.tipoAtencion  = o.tipoAtencion;
        this.examenesBase  = new ArrayList<>(o.examenesBase);
    }

    @Override
    public ExpedientePrototype clonar() { return new ExpedienteBase(this); }

    public String getTipoPaciente()       { return tipoPaciente; }
    public String getTipoAtencion()       { return tipoAtencion; }
    public List<String> getExamenesBase() { return new ArrayList<>(examenesBase); }
}

class RegistroPrototipos {
    private Map<String, ExpedienteBase> catalogo = new LinkedHashMap<>();

    public void registrar(String clave, ExpedienteBase proto) { catalogo.put(clave, proto); }

    public ExpedienteBase obtenerClon(String clave) {
        ExpedienteBase p = catalogo.get(clave);
        if (p == null) throw new IllegalArgumentException("Plantilla no encontrada: " + clave);
        return (ExpedienteBase) p.clonar();
    }

    public Set<String> getClaves() { return catalogo.keySet(); }
}

// ── BUILDER ─────────────────────────────────────────────────
class ExpedienteMedico {
    String tipoPaciente, tipoAtencion, nombrePaciente, cedula, diagnostico,
           notasAdicionales, medicoResponsable;
    int    edad;
    List<String> examenes     = new ArrayList<>();
    List<String> medicamentos = new ArrayList<>();
    List<String> alergias     = new ArrayList<>();

    private ExpedienteMedico() {}

    public static class Builder {
        private ExpedienteMedico exp;

        public Builder(ExpedienteBase base) {
            exp = new ExpedienteMedico();
            exp.tipoPaciente = base.getTipoPaciente();
            exp.tipoAtencion = base.getTipoAtencion();
            exp.examenes.addAll(base.getExamenesBase());
        }

        public Builder conNombre(String v)          { exp.nombrePaciente = v;      return this; }
        public Builder conEdad(int v)               { exp.edad = v;                return this; }
        public Builder conCedula(String v)          { exp.cedula = v;              return this; }
        public Builder conDiagnostico(String v)     { exp.diagnostico = v;         return this; }
        public Builder conMedico(String v)          { exp.medicoResponsable = v;   return this; }
        public Builder conNotas(String v)           { exp.notasAdicionales = v;    return this; }
        public Builder agregarMedicamento(String v) { exp.medicamentos.add(v);     return this; }
        public Builder agregarAlergia(String v)     { exp.alergias.add(v);         return this; }
        public Builder agregarExamen(String v)      { exp.examenes.add(v);         return this; }

        public ExpedienteMedico construir() {
            if (exp.nombrePaciente == null || exp.cedula == null || exp.diagnostico == null)
                throw new IllegalStateException("Nombre, cédula y diagnóstico son obligatorios.");
            return exp;
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  INTERFAZ GRÁFICA SWING
// ══════════════════════════════════════════════════════════════
public class SistemaExpedientesMedicosGUI extends JFrame {

    // ── Colores corporativos ──
    static final Color C_AZUL      = new Color(41,  128, 185);
    static final Color C_AZUL_OSC  = new Color(21,  67,  96);
    static final Color C_VERDE     = new Color(39,  174, 96);
    static final Color C_NARANJA   = new Color(230, 126, 34);
    static final Color C_FONDO     = new Color(236, 240, 241);
    static final Color C_BLANCO    = Color.WHITE;
    static final Color C_TEXTO     = new Color(44,  62,  80);
    static final Color C_GRIS_CLAR = new Color(189, 195, 199);
    static final Font  F_TITULO    = new Font("Segoe UI", Font.BOLD,  22);
    static final Font  F_SUBTIT    = new Font("Segoe UI", Font.BOLD,  14);
    static final Font  F_NORMAL    = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font  F_BOLD      = new Font("Segoe UI", Font.BOLD,  13);
    static final Font  F_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);

    // ── Datos del sistema ──
    private RegistroPrototipos registro = new RegistroPrototipos();
    private List<ExpedienteMedico> expedientes = new ArrayList<>();

    // ── Componentes principales ──
    private JPanel   panelCentral;
    private CardLayout cardLayout;
    private DefaultTableModel tableModel;
    private JTable   tabla;
    private JLabel   lblContador;

    public SistemaExpedientesMedicosGUI() {
        initPrototipos();
        initUI();
    }

    // ── Inicializar plantillas Prototype ──────────────────────
    private void initPrototipos() {
        registro.registrar("adulto-urgencias", new ExpedienteBase(
            "Adulto", "Urgencias",
            Arrays.asList("Hemograma completo", "Glucosa en sangre", "Electrocardiograma")));

        registro.registrar("pediatrico-consulta", new ExpedienteBase(
            "Pediátrico", "Consulta externa",
            Arrays.asList("Peso y talla", "Presión arterial", "Hemograma")));

        registro.registrar("geriatrico-hospitalizacion", new ExpedienteBase(
            "Geriátrico", "Hospitalización",
            Arrays.asList("Hemograma", "Panel metabólico", "Radiografía tórax", "Ecocardiograma")));
    }

    // ── Construcción de la UI ─────────────────────────────────
    private void initUI() {
        setTitle("Sistema de Expedientes Médicos — Patrón Prototype + Builder");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_FONDO);

        root.add(crearHeader(),      BorderLayout.NORTH);
        root.add(crearSidebar(),     BorderLayout.WEST);

        cardLayout   = new CardLayout();
        panelCentral = new JPanel(cardLayout);
        panelCentral.setBackground(C_FONDO);
        panelCentral.add(crearPanelDashboard(), "dashboard");
        panelCentral.add(crearPanelFormulario(),"formulario");
        panelCentral.add(crearPanelLista(),     "lista");
        panelCentral.add(crearPanelAcercaDe(), "acerca");

        root.add(panelCentral, BorderLayout.CENTER);
        root.add(crearFooter(), BorderLayout.SOUTH);

        setContentPane(root);
        cardLayout.show(panelCentral, "dashboard");
    }

    // ── HEADER ───────────────────────────────────────────────
    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0, C_AZUL_OSC, getWidth(),0, C_AZUL);
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Logo / título
        JLabel ico = new JLabel("🏥");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        JLabel titulo = new JLabel("  Sistema de Expedientes Médicos");
        titulo.setFont(F_TITULO);
        titulo.setForeground(C_BLANCO);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(ico);
        left.add(titulo);

        lblContador = new JLabel("Expedientes: 0");
        lblContador.setFont(F_SMALL);
        lblContador.setForeground(new Color(200, 230, 255));

        JLabel subtit = new JLabel("Patrón Prototype + Builder  |  Patrones de Software — 4° Semestre");
        subtit.setFont(F_SMALL);
        subtit.setForeground(new Color(180, 210, 255));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        subtit.setAlignmentX(Component.RIGHT_ALIGNMENT);
        lblContador.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(subtit);
        right.add(lblContador);

        header.add(left,  BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ── SIDEBAR ───────────────────────────────────────────────
    private JPanel crearSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(C_AZUL_OSC);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(190, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20,10,20,10));

        String[][] menus = {
            {"🏠", "Dashboard"},
            {"➕", "Nuevo Expediente"},
            {"📋", "Ver Expedientes"},
            {"ℹ️",  "Acerca de"}
        };
        String[] cards = {"dashboard","formulario","lista","acerca"};

        for (int i = 0; i < menus.length; i++) {
            final String card = cards[i];
            JButton btn = crearBotonMenu(menus[i][0] + "  " + menus[i][1]);
            btn.addActionListener(e -> cardLayout.show(panelCentral, card));
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(8));
        }
        sidebar.add(Box.createVerticalGlue());

        JLabel credit = new JLabel("<html><center><small>UC Colombia<br/>2026</small></center></html>");
        credit.setForeground(new Color(150,180,210));
        credit.setFont(F_SMALL);
        credit.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(credit);

        return sidebar;
    }

    private JButton crearBotonMenu(String texto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(52, 152, 219, 180));
                } else {
                    g2.setColor(new Color(52, 73, 94, 120));
                }
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(F_NORMAL);
        btn.setForeground(C_BLANCO);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(170, 42));
        btn.setPreferredSize(new Dimension(170, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── DASHBOARD ─────────────────────────────────────────────
    private JPanel crearPanelDashboard() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_FONDO);
        p.setBorder(new EmptyBorder(24,24,24,24));

        JLabel lbl = new JLabel("Panel Principal");
        lbl.setFont(F_TITULO);
        lbl.setForeground(C_TEXTO);
        p.add(lbl, BorderLayout.NORTH);

        // Tarjetas de estadísticas
        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        cards.setBorder(new EmptyBorder(20, 0, 20, 0));

        cards.add(crearTarjetaStat("📁", "Expedientes\nRegistrados", "0", C_AZUL,   "statExpedientes"));
        cards.add(crearTarjetaStat("🧬", "Plantillas\nPrototype",    "3", C_VERDE,  null));
        cards.add(crearTarjetaStat("🏥", "Tipos de\nAtención",       "3", C_NARANJA,null));

        p.add(cards, BorderLayout.CENTER);

        // Panel explicativo
        JPanel info = new JPanel(new GridLayout(1, 2, 16, 0));
        info.setOpaque(false);
        info.add(crearTarjetaInfo("🔵 Patrón Prototype",
            "Permite clonar plantillas de expedientes base según el tipo de paciente " +
            "(Adulto, Pediátrico, Geriátrico) sin crear objetos desde cero. " +
            "Cada clon es independiente en memoria.", C_AZUL));
        info.add(crearTarjetaInfo("🟠 Patrón Builder",
            "Construye el expediente paso a paso: datos personales, diagnóstico, " +
            "medicamentos, alergias y exámenes adicionales. " +
            "Garantiza que el objeto solo se crea cuando está completo.", C_NARANJA));

        p.add(info, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearTarjetaStat(String icono, String titulo, String valor, Color color, String tag) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BLANCO);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.setColor(color);
                g2.fillRoundRect(0,0,6,getHeight(),6,6);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 16));

        JLabel icoL = new JLabel(icono);
        icoL.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));

        JLabel valL = new JLabel(valor);
        valL.setFont(new Font("Segoe UI", Font.BOLD, 40));
        valL.setForeground(color);
        if (tag != null) valL.setName(tag);

        JLabel titL = new JLabel("<html>" + titulo.replace("\n","<br>") + "</html>");
        titL.setFont(F_SMALL);
        titL.setForeground(new Color(120,120,120));

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(valL);
        right.add(titL);

        card.add(icoL, BorderLayout.WEST);
        card.add(right, BorderLayout.CENTER);

        // Guardar referencia para actualizar
        if (tag != null) card.setName(tag + "_card");

        return card;
    }

    private JPanel crearTarjetaInfo(String titulo, String texto, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BLANCO);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16,18,16,18));

        JLabel titL = new JLabel(titulo);
        titL.setFont(F_BOLD);
        titL.setForeground(color);
        titL.setBorder(new EmptyBorder(0,0,8,0));

        JTextArea ta = new JTextArea(texto);
        ta.setFont(F_NORMAL);
        ta.setForeground(C_TEXTO);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        ta.setOpaque(false);

        card.add(titL, BorderLayout.NORTH);
        card.add(ta,   BorderLayout.CENTER);
        return card;
    }

    // ── FORMULARIO ────────────────────────────────────────────
    private JTextField fNombre, fCedula, fEdad, fMedico, fDiagnostico,
                       fMed1, fMed2, fAlergia, fExtraExam, fNotas;
    private JComboBox<String> cbPlantilla;

    private JPanel crearPanelFormulario() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(C_FONDO);
        outer.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel lbl = new JLabel("➕  Nuevo Expediente Médico");
        lbl.setFont(F_TITULO);
        lbl.setForeground(C_TEXTO);
        outer.add(lbl, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Plantilla
        cbPlantilla = new JComboBox<>(registro.getClaves().toArray(new String[0]));
        cbPlantilla.setFont(F_NORMAL);

        // Campos
        fNombre     = crearCampo();
        fCedula     = crearCampo();
        fEdad       = crearCampo();
        fMedico     = crearCampo();
        fDiagnostico= crearCampo();
        fMed1       = crearCampo();
        fMed2       = crearCampo();
        fAlergia    = crearCampo();
        fExtraExam  = crearCampo();
        fNotas      = crearCampo();

        Object[][] filas = {
            {"Plantilla (Prototype):", cbPlantilla,    "Nombre completo:",   fNombre},
            {"Cédula / ID:",           fCedula,         "Edad (años):",       fEdad},
            {"Médico responsable:",    fMedico,         "Diagnóstico:",       fDiagnostico},
            {"Medicamento 1:",         fMed1,           "Medicamento 2:",     fMed2},
            {"Alergia conocida:",      fAlergia,        "Examen adicional:",  fExtraExam},
            {"Notas clínicas:",        fNotas,          null,                 null}
        };

        int row = 0;
        for (Object[] f : filas) {
            gbc.gridx=0; gbc.gridy=row; gbc.weightx=0.15;
            form.add(crearLabel((String)f[0]), gbc);

            gbc.gridx=1; gbc.weightx=0.35;
            form.add((Component)f[1], gbc);

            if (f[2] != null) {
                gbc.gridx=2; gbc.weightx=0.15;
                form.add(crearLabel((String)f[2]), gbc);

                gbc.gridx=3; gbc.weightx=0.35;
                form.add((Component)f[3], gbc);
            }
            row++;
        }

        // Botones
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton btnLimpiar = crearBoton("🗑  Limpiar", C_GRIS_CLAR, C_TEXTO);
        JButton btnGuardar = crearBoton("💾  Guardar Expediente", C_AZUL, C_BLANCO);

        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnGuardar.addActionListener(e -> guardarExpediente());

        btnPanel.add(btnLimpiar);
        btnPanel.add(btnGuardar);

        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=4; gbc.weightx=1;
        form.add(btnPanel, gbc);

        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private JTextField crearCampo() {
        JTextField tf = new JTextField();
        tf.setFont(F_NORMAL);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_GRIS_CLAR, 1, true),
            new EmptyBorder(5,8,5,8)));
        tf.setPreferredSize(new Dimension(200, 32));
        return tf;
    }

    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(F_BOLD);
        l.setForeground(C_TEXTO);
        return l;
    }

    private JButton crearBoton(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(F_BOLD);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 38));
        return btn;
    }

    private void limpiarFormulario() {
        for (JTextField tf : new JTextField[]{fNombre,fCedula,fEdad,fMedico,
                fDiagnostico,fMed1,fMed2,fAlergia,fExtraExam,fNotas})
            tf.setText("");
        cbPlantilla.setSelectedIndex(0);
    }

    private void guardarExpediente() {
        try {
            String plantilla = (String) cbPlantilla.getSelectedItem();
            ExpedienteBase base = registro.obtenerClon(plantilla);

            ExpedienteMedico.Builder builder = new ExpedienteMedico.Builder(base)
                .conNombre(fNombre.getText().trim())
                .conCedula(fCedula.getText().trim())
                .conDiagnostico(fDiagnostico.getText().trim())
                .conMedico(fMedico.getText().trim())
                .conNotas(fNotas.getText().trim());

            try { builder.conEdad(Integer.parseInt(fEdad.getText().trim())); }
            catch (NumberFormatException ex) { builder.conEdad(0); }

            if (!fMed1.getText().isBlank())      builder.agregarMedicamento(fMed1.getText().trim());
            if (!fMed2.getText().isBlank())      builder.agregarMedicamento(fMed2.getText().trim());
            if (!fAlergia.getText().isBlank())   builder.agregarAlergia(fAlergia.getText().trim());
            if (!fExtraExam.getText().isBlank()) builder.agregarExamen(fExtraExam.getText().trim());

            ExpedienteMedico exp = builder.construir();
            expedientes.add(exp);
            actualizarTabla();
            lblContador.setText("Expedientes: " + expedientes.size());

            JOptionPane.showMessageDialog(this,
                "✅  Expediente de " + exp.nombrePaciente + " guardado exitosamente.\n" +
                "Plantilla usada (Prototype): " + plantilla + "\n" +
                "Patrón Builder aplicado correctamente.",
                "Expediente Guardado", JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
            cardLayout.show(panelCentral, "lista");

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, "⚠️  " + ex.getMessage(),
                "Campos incompletos", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ── LISTA DE EXPEDIENTES ──────────────────────────────────
    private JPanel crearPanelLista() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_FONDO);
        p.setBorder(new EmptyBorder(24,24,24,24));

        JLabel lbl = new JLabel("📋  Expedientes Registrados");
        lbl.setFont(F_TITULO);
        lbl.setForeground(C_TEXTO);
        lbl.setBorder(new EmptyBorder(0,0,16,0));
        p.add(lbl, BorderLayout.NORTH);

        String[] cols = {"#","Paciente","Cédula","Edad","Tipo","Atención","Médico","Diagnóstico","Medicamentos","Alergias"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(tableModel);
        tabla.setFont(F_NORMAL);
        tabla.setRowHeight(30);
        tabla.getTableHeader().setFont(F_BOLD);
        tabla.getTableHeader().setBackground(C_AZUL);
        tabla.getTableHeader().setForeground(C_BLANCO);
        tabla.setSelectionBackground(new Color(174, 214, 241));
        tabla.setGridColor(new Color(220,220,220));
        tabla.setIntercellSpacing(new Dimension(8,4));
        tabla.setFillsViewportHeight(true);

        // Colores alternados
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if (!sel) comp.setBackground(r%2==0 ? C_BLANCO : new Color(245,248,250));
                setBorder(new EmptyBorder(0,8,0,8));
                return comp;
            }
        });

        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabla.getSelectedRow();
                    if (row >= 0) mostrarDetalleExpediente(expedientes.get(row));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(C_GRIS_CLAR,1,true));
        p.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("  💡 Doble clic sobre un expediente para ver el detalle completo");
        hint.setFont(F_SMALL);
        hint.setForeground(new Color(130,130,130));
        p.add(hint, BorderLayout.SOUTH);

        return p;
    }

    private void actualizarTabla() {
        tableModel.setRowCount(0);
        int i = 1;
        for (ExpedienteMedico e : expedientes) {
            tableModel.addRow(new Object[]{
                i++,
                e.nombrePaciente,
                e.cedula,
                e.edad + " años",
                e.tipoPaciente,
                e.tipoAtencion,
                e.medicoResponsable,
                e.diagnostico,
                String.join(", ", e.medicamentos),
                e.alergias.isEmpty() ? "Ninguna" : String.join(", ", e.alergias)
            });
        }
    }

    private void mostrarDetalleExpediente(ExpedienteMedico e) {
        JPanel p = new JPanel(new GridLayout(0,2,8,8));
        p.setBorder(new EmptyBorder(10,10,10,10));

        Object[][] datos = {
            {"Paciente",      e.nombrePaciente},
            {"Cédula",        e.cedula},
            {"Edad",          e.edad + " años"},
            {"Tipo paciente", e.tipoPaciente},
            {"Tipo atención", e.tipoAtencion},
            {"Médico",        e.medicoResponsable},
            {"Diagnóstico",   e.diagnostico},
            {"Exámenes",      String.join(" | ", e.examenes)},
            {"Medicamentos",  String.join(" | ", e.medicamentos)},
            {"Alergias",      e.alergias.isEmpty() ? "Ninguna" : String.join(", ", e.alergias)},
            {"Notas",         e.notasAdicionales != null ? e.notasAdicionales : "—"}
        };

        for (Object[] d : datos) {
            JLabel k = new JLabel((String)d[0] + ":");
            k.setFont(F_BOLD);
            k.setForeground(C_AZUL);
            JLabel v = new JLabel("<html>" + d[1] + "</html>");
            v.setFont(F_NORMAL);
            p.add(k); p.add(v);
        }

        JOptionPane.showMessageDialog(this, p,
            "Detalle — " + e.nombrePaciente, JOptionPane.PLAIN_MESSAGE);
    }

    // ── ACERCA DE ─────────────────────────────────────────────
    private JPanel crearPanelAcercaDe() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_FONDO);
        p.setBorder(new EmptyBorder(30,40,30,40));

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_BLANCO);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30,40,30,40));

        JLabel titulo = new JLabel("ℹ️  Acerca de este Taller", SwingConstants.CENTER);
        titulo.setFont(F_TITULO);
        titulo.setForeground(C_AZUL);
        titulo.setBorder(new EmptyBorder(0,0,20,0));

        String html = "<html><body style='font-family:Segoe UI; font-size:13px; color:#2c3e50;'>"
            + "<b>Taller:</b> Patrón Prototype y Builder aplicado en un caso de estudio real<br><br>"
            + "<b>Caso de estudio:</b> Sistema de Gestión de Expedientes Médicos<br><br>"
            + "<b>Patrón Prototype:</b><br>"
            + "• Crea plantillas base (Adulto-Urgencias, Pediátrico-Consulta, Geriátrico-Hospitalización)<br>"
            + "• Cada nuevo expediente clona la plantilla correspondiente<br>"
            + "• Los clones son independientes en memoria<br><br>"
            + "<b>Patrón Builder:</b><br>"
            + "• Construye el expediente completo paso a paso<br>"
            + "• Datos personales + datos clínicos encadenados con métodos fluidos<br>"
            + "• Validación obligatoria antes de construir el objeto final<br><br>"
            + "<b>Curso:</b> Patrones de Software — Cuarto Semestre<br>"
            + "<b>Docente:</b> Jhonatan Andres Mideros Narvaez<br>"
            + "<b>Universidad Cooperativa de Colombia — 2026</b>"
            + "</body></html>";

        JLabel desc = new JLabel(html);
        desc.setVerticalAlignment(SwingConstants.TOP);

        card.add(titulo, BorderLayout.NORTH);
        card.add(desc,   BorderLayout.CENTER);

        p.add(card, BorderLayout.CENTER);
        return p;
    }

    
    // ── FOOTER ─────────────────────────────
    // ───────────────────
    private JPanel crearFooter() {
        
        JPanel footer = new JPanel(new FlowLay
            out(FlowLayout.CENTER));
        footer.setBackground(C_AZUL_OSC);
        
        footer.setPreferredSize(new Dimension(
            0, 32));
        JLabel lbl = new JLabel("Universidad C
        ooperativa de Colombia  |  Patrones de Software  |  2026");
        lbl.setFont(F_SMALL);
        
        lbl.setForeground(new Color(180, 210, 
            255));
        footer.add(lbl);
        
        return footer;
        
    }
    

    
    // ── MAIN ───────────────────────────────
    // ──────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            SistemaExpedientesMedicosGUI app = new SistemaExpedientesMedicosGUI();
            app.setVisible(true);
        });
    }
}