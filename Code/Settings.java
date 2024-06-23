import javax.swing.*;
import java.io.*;
import java.util.Properties;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Settings {
    private static final String SETTINGS_FILE = "settings.properties";

    private boolean encryptPasswords;
    private int enterDelay;

    public Settings() {
        loadSettings();
    }

    private void loadSettings() {
        Properties properties = new Properties();
        try (FileInputStream fileInput = new FileInputStream(SETTINGS_FILE)) {
            properties.load(fileInput);
            encryptPasswords = Boolean.parseBoolean(properties.getProperty("encryptPasswords", "true"));
            enterDelay = Integer.parseInt(properties.getProperty("enterDelay", "500"));
        } catch (IOException e) {
            // Errore durante il caricamento delle impostazioni, utilizza le impostazioni predefinite
            encryptPasswords = true;
            enterDelay = 500;
            saveSettings(); // Salva le impostazioni predefinite
        }
    }

    public void saveSettings() {
        Properties properties = new Properties();
        properties.setProperty("encryptPasswords", Boolean.toString(encryptPasswords));
        properties.setProperty("enterDelay", Integer.toString(enterDelay));
        try (FileOutputStream fileOutput = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(fileOutput, "Application Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isEncryptPasswords() {
        return encryptPasswords;
    }

    public void setEncryptPasswords(boolean encryptPasswords) {
        if (this.encryptPasswords != encryptPasswords) {
            this.encryptPasswords = encryptPasswords;
            saveSettings();
            DataHandler.toggleEncryption(encryptPasswords);
        }
    }

    public int getEnterDelay() {
        return enterDelay;
    }

    public void setEnterDelay(int enterDelay) {
        if (this.enterDelay != enterDelay) {
            this.enterDelay = enterDelay;
            saveSettings();
        }
    }

    // UI elements to toggle encryption setting
    public void showSettingsDialog() {
        JDialog settingsDialog = new JDialog();
        settingsDialog.setTitle("Settings");
        settingsDialog.setSize(300, 150);
        settingsDialog.setModal(true);
        settingsDialog.setLayout(new GridBagLayout());
        settingsDialog.setLocationRelativeTo(null);
        settingsDialog.setResizable(false); // Impedisce il ridimensionamento della finestra

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JCheckBox encryptPasswordsCheckbox = new JCheckBox("", encryptPasswords);
        encryptPasswordsCheckbox.addActionListener(e -> {
            setEncryptPasswords(encryptPasswordsCheckbox.isSelected());
        });

        JLabel enterDelayLabel = new JLabel("'Enter' Delay (ms):");
        JTextField enterDelayField = new JTextField(Integer.toString(enterDelay), 3);
       /*  enterDelayField.setMinimumSize(new Dimension(60, 20)); // Imposta una dimensione minima
        enterDelayField.setMaximumSize(new Dimension(60, 20)); // Imposta una dimensione massima*/

        enterDelayField.addActionListener(e -> {
            try {
                int newEnterDelay = Integer.parseInt(enterDelayField.getText());
                setEnterDelay(newEnterDelay);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(settingsDialog, "Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Posiziona "Encrypt Passwords" a sinistra
        gbc.gridx = 0;
        gbc.gridy = 0;
        settingsDialog.add(new JLabel("Encrypt Passwords"), gbc);

        // Posiziona la checkbox a destra
        gbc.gridx = 1;
        settingsDialog.add(encryptPasswordsCheckbox, gbc);

        // Nuova riga per "Enter Delay (ms):"
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        settingsDialog.add(enterDelayLabel, gbc);

        // Posiziona il campo di testo a destra
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        settingsDialog.add(enterDelayField, gbc);

        // Aggiungi un window listener per salvare le impostazioni quando la finestra viene chiusa
        settingsDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    int newEnterDelay = Integer.parseInt(enterDelayField.getText());
                    setEnterDelay(newEnterDelay);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(settingsDialog, "Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
                }
                saveSettings();
            }
        });

        settingsDialog.setVisible(true);
    }
}
