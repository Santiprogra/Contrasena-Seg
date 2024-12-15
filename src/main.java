
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

class Main extends JFrame {
    private JCheckBox lowercaseCheck, uppercaseCheck, numbersCheck, symbolsCheck;
    private JSlider lengthSlider;
    private JLabel passwordLabel, strengthLabel, lengthDescriptionLabel;
    private JButton generateButton, copyButton;
    private JProgressBar loadingBar;

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    public Main() {
        setTitle("Generador de Contraseñas Seguras");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(240, 240, 240));

        initComponents();
        addListeners();
        checkSelections();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Generador de Contraseñas", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;
        lowercaseCheck = createStyledCheckBox("Minúsculas");
        uppercaseCheck = createStyledCheckBox("Mayúsculas");
        numbersCheck = createStyledCheckBox("Números");
        symbolsCheck = createStyledCheckBox("Símbolos");

        gbc.gridx = 0; add(lowercaseCheck, gbc);
        gbc.gridx = 1; add(uppercaseCheck, gbc);
        gbc.gridy = 2; gbc.gridx = 0; add(numbersCheck, gbc);
        gbc.gridx = 1; add(symbolsCheck, gbc);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        lengthDescriptionLabel = new JLabel("Longitud de Contraseña: 12");
        add(lengthDescriptionLabel, gbc);

        lengthSlider = new JSlider(JSlider.HORIZONTAL, 6, 30, 12);
        lengthSlider.setMajorTickSpacing(6);
        lengthSlider.setPaintTicks(true);
        lengthSlider.addChangeListener(e -> lengthDescriptionLabel.setText("Longitud de Contraseña: " + lengthSlider.getValue()));
        gbc.gridy = 4; add(lengthSlider, gbc);

        gbc.gridy = 5;
        generateButton = createStyledButton("Generar Contraseña");
        add(generateButton, gbc);

        gbc.gridy = 6;
        loadingBar = new JProgressBar();
        loadingBar.setVisible(false);
        add(loadingBar, gbc);

        gbc.gridy = 7;
        passwordLabel = new JLabel("Contraseña: ", SwingConstants.CENTER);
        passwordLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        add(passwordLabel, gbc);

        gbc.gridy = 8;
        copyButton = createStyledButton("Copiar Contraseña");
        copyButton.setEnabled(false);
        add(copyButton, gbc);

        gbc.gridy = 9;
        strengthLabel = new JLabel("", SwingConstants.CENTER);
        strengthLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(strengthLabel, gbc);
    }

    private void addListeners() {
        generateButton.addActionListener(e -> generatePassword());

        copyButton.addActionListener(e -> {
            String password = passwordLabel.getText().replace("Contraseña: ", "").trim();
            if (!password.isEmpty()) copyToClipboard(password);
        });

        // Monitoreo de checkboxes
        ItemListener checkboxListener = e -> checkSelections();
        lowercaseCheck.addItemListener(checkboxListener);
        uppercaseCheck.addItemListener(checkboxListener);
        numbersCheck.addItemListener(checkboxListener);
        symbolsCheck.addItemListener(checkboxListener);
    }

    private void generatePassword() {
        loadingBar.setVisible(true);
        loadingBar.setIndeterminate(true);
        generateButton.setEnabled(false);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                try {
                    Thread.sleep(500);
                    return createSecurePassword();
                } catch (InterruptedException ex) {
                    return "";
                }
            }

            @Override
            protected void done() {
                try {
                    String password = get();
                    passwordLabel.setText("Contraseña: " + password);
                    copyButton.setEnabled(true);

                    String strength = calculatePasswordStrength(password);
                    strengthLabel.setText("Seguridad: " + strength);
                    actualizarColorFuerza(strength);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    loadingBar.setIndeterminate(false);
                    loadingBar.setVisible(false);
                    generateButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void checkSelections() {
        boolean anySelected = lowercaseCheck.isSelected() || uppercaseCheck.isSelected() || numbersCheck.isSelected() || symbolsCheck.isSelected();
        generateButton.setEnabled(anySelected);
    }

    private String createSecurePassword() {
        List<String> sets = new ArrayList<>();
        if (lowercaseCheck.isSelected()) sets.add(LOWERCASE);
        if (uppercaseCheck.isSelected()) sets.add(UPPERCASE);
        if (numbersCheck.isSelected()) sets.add(NUMBERS);
        if (symbolsCheck.isSelected()) sets.add(SYMBOLS);

        SecureRandom random = new SecureRandom();
        String allChars = String.join("", sets);
        StringBuilder password = new StringBuilder();

        random.ints(lengthSlider.getValue(), 0, allChars.length())
              .forEach(i -> password.append(allChars.charAt(i)));

        return shuffleString(password.toString());
    }

    private String calculatePasswordStrength(String password) {
        int strength = 0;
        if (password.length() >= 12) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) strength++;

        return switch (strength) {
            case 0, 1 -> "Muy Débil";
            case 2 -> "Débil";
            case 3 -> "Moderada";
            case 4 -> "Fuerte";
            default -> "Muy Fuerte";
        };
    }

    private void actualizarColorFuerza(String strength) {
        switch (strength) {
            case "Muy Débil" -> strengthLabel.setForeground(Color.RED);
            case "Débil" -> strengthLabel.setForeground(new Color(255, 140, 0));
            case "Moderada" -> strengthLabel.setForeground(Color.YELLOW);
            case "Fuerte" -> strengthLabel.setForeground(new Color(0, 200, 0));
            case "Muy Fuerte" -> strengthLabel.setForeground(new Color(0, 128, 0));
        }
    }

    private void copyToClipboard(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);

        strengthLabel.setText("¡Contraseña copiada!");
        Timer timer = new Timer(2000, (ActionEvent e) -> strengthLabel.setText(""));
        timer.setRepeats(false);
        timer.start();
    }

    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        SecureRandom rng = new SecureRandom();
        for (int i = chars.length - 1; i > 0; i--) {
            int index = rng.nextInt(i + 1);
            char temp = chars[index];
            chars[index] = chars[i];
            chars[i] = temp;
        }
        return new String(chars);
    }

    private JCheckBox createStyledCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setBackground(new Color(240, 240, 240));
        return checkBox;
    }

    private JButton createStyledButton(String text) {
    JButton button = new JButton(text);
    button.setFont(new Font("Arial", Font.BOLD, 14));
    button.setBackground(new Color(52, 152, 219));  // Fondo azul
    button.setForeground(Color.WHITE);  // Color del texto en blanco

    // Aseguramos que el texto sea visible y legible
    button.setOpaque(true);
    button.setBorderPainted(false);

    // Animación de hover
    button.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            button.setBackground(new Color(41, 128, 185));  // Fondo más oscuro cuando pasa el ratón
        }
        public void mouseExited(java.awt.event.MouseEvent evt) {
            button.setBackground(new Color(52, 152, 219));  // Vuelve al fondo azul original
        }
        });

        return button;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}

