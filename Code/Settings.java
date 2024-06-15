// Settings.java

import javax.swing.*;
import java.io.*;
import java.util.Properties;

public class Settings {
    private static final String SETTINGS_FILE = "settings.properties";

    private boolean encryptPasswords;

    public Settings() {
        loadSettings();
    }

    private void loadSettings() {
        Properties properties = new Properties();
        try (FileInputStream fileInput = new FileInputStream(SETTINGS_FILE)) {
            properties.load(fileInput);
            encryptPasswords = Boolean.parseBoolean(properties.getProperty("encryptPasswords", "true"));
        } catch (IOException e) {
            // Errore durante il caricamento delle impostazioni, utilizza le impostazioni predefinite
            encryptPasswords = true;
            saveSettings(); // Salva le impostazioni predefinite
        }
    }

    public void saveSettings() {
        Properties properties = new Properties();
        properties.setProperty("encryptPasswords", Boolean.toString(encryptPasswords));
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

    // UI elements to toggle encryption setting
    public void showSettingsDialog() {
        JCheckBox encryptPasswordsCheckbox = new JCheckBox("Encrypt Passwords", encryptPasswords);
        encryptPasswordsCheckbox.addActionListener(e -> setEncryptPasswords(encryptPasswordsCheckbox.isSelected()));

        JPanel panel = new JPanel();
        panel.add(encryptPasswordsCheckbox);

        int result = JOptionPane.showConfirmDialog(null, panel, "Settings", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            setEncryptPasswords(encryptPasswordsCheckbox.isSelected());
        }
    }
}