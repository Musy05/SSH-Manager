// DataHandler.java

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DataHandler {

    private static final String SALT_FILE = "salt.dat";
    private static final String SETTINGS_FILE = "settings.properties";
    private static SecretKey secretKey;
    private static boolean encryptPasswords = true; // Default to true

    static {
        loadSettings();
        if (encryptPasswords) {
            initializeSecretKey();
        }
    }

    private static void initializeSecretKey() {
        try {
            // Carica il salt
            File saltFile = new File(SALT_FILE);
            byte[] salt;
            if (saltFile.exists()) {
                salt = Files.readAllBytes(saltFile.toPath());
            } else {
                salt = CryptoUtils.generateSalt();
                Files.write(saltFile.toPath(), salt);
            }

            // Chiede all'utente la password se necessario
            String password = JOptionPane.showInputDialog("Enter Password:");
            if (password != null && !password.isEmpty()) {
                secretKey = CryptoUtils.getKeyFromPassword(password, salt);
            } else {
                throw new RuntimeException("Password cannot be empty");
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace(); // Gestisce l'eccezione generica
        }
    }

    private static void initializeSecretKeyWithPassword(String password) {
        try {
            // Carica il salt
            File saltFile = new File(SALT_FILE);
            byte[] salt;
            if (saltFile.exists()) {
                salt = Files.readAllBytes(saltFile.toPath());
            } else {
                salt = CryptoUtils.generateSalt();
                Files.write(saltFile.toPath(), salt);
            }

            if (password != null && !password.isEmpty()) {
                secretKey = CryptoUtils.getKeyFromPassword(password, salt);
            } else {
                throw new RuntimeException("Password cannot be empty");
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace(); // Gestisce l'eccezione generica
        }
    }

    public static void loadFileContent() {
        MainClass.contentPanel.removeAll();
        MainClass.contentPanel.revalidate();
        MainClass.contentPanel.repaint();

        File file = new File("password.txt");
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String ip = parts[0];
                    String username = parts[1];
                    String password = encryptPasswords ? CryptoUtils.decrypt(parts[2], secretKey) : parts[2];
                    MainClass.addRow(ip, username, password, row, row);
                    MainClass.ipAddresses.add(ip);
                    row++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace(); // Gestisce l'eccezione generica
        }
    }

    public static void addConnection() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField ipField = new JTextField();
        JTextField usernameField = new JTextField();
        JTextField passwordField = new JTextField();
        panel.add(new JLabel("IP Address:"));
        panel.add(ipField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Connection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("password.txt", true))) {
                String encryptedPassword = encryptPasswords ? CryptoUtils.encrypt(passwordField.getText(), secretKey) : passwordField.getText();
                writer.write(ipField.getText() + "," + usernameField.getText() + "," + encryptedPassword);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace(); // Gestisce l'eccezione generica
            }
            loadFileContent();
        }
    }

    public static void editConnection(int index) {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField ipField = new JTextField(MainClass.ipAddresses.get(index));
        JTextField usernameField = new JTextField();
        JTextField passwordField = new JTextField();
        panel.add(new JLabel("IP Address:"));
        panel.add(ipField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Edit Connection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                List<String> lines = Files.readAllLines(new File("password.txt").toPath());
                String encryptedPassword = encryptPasswords ? CryptoUtils.encrypt(passwordField.getText(), secretKey) : passwordField.getText();
                lines.set(index, ipField.getText() + "," + usernameField.getText() + "," + encryptedPassword);
                Files.write(new File("password.txt").toPath(), lines);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace(); // Gestisce l'eccezione generica
            }
            loadFileContent();
        }
    }

    public static void deleteConnection(int index) {
        try {
            List<String> lines = Files.readAllLines(new File("password.txt").toPath());
            lines.remove(index);
            Files.write(new File("password.txt").toPath(), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadFileContent();
    }

    public static void toggleEncryption(boolean encrypt) {
        if (encryptPasswords != encrypt) {
            if (encrypt) {
                // Selezionando la crittografia, chiede una nuova password
                String newPassword = JOptionPane.showInputDialog("Enter New Password for Encryption:");
                if (newPassword != null && !newPassword.isEmpty()) {
                    initializeSecretKeyWithPassword(newPassword);
                    encryptPasswords = true;
                    reEncryptPasswords(); // Ri-crittografa tutte le password con la nuova chiave
                    saveSettings(); // Salva le impostazioni aggiornate
                } else {
                    JOptionPane.showMessageDialog(null, "Password cannot be empty. Encryption will not be enabled.");
                }
            } else {
                // Disabilitando la crittografia
                encryptPasswords = false;
                reEncryptPasswords(); // Decifra tutte le password
                saveSettings(); // Salva le impostazioni aggiornate
            }
        }
    }

    private static void reEncryptPasswords() {
        try {
            List<String> lines = Files.readAllLines(new File("password.txt").toPath());
            List<String> updatedLines = new ArrayList<>();
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String ip = parts[0];
                    String username = parts[1];
                    String password;
                    if (encryptPasswords) {
                        // Encrypting with the new secret key
                        password = CryptoUtils.encrypt(parts[2], secretKey);
                    } else {
                        // Decrypting with the old secret key
                        password = CryptoUtils.decrypt(parts[2], secretKey);
                    }
                    updatedLines.add(ip + "," + username + "," + password);
                }
            }
            Files.write(new File("password.txt").toPath(), updatedLines);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace(); // Gestisce l'eccezione generica
        }
    }

    private static void loadSettings() {
        Properties properties = new Properties();
        try (FileInputStream fileInput = new FileInputStream(SETTINGS_FILE)) {
            properties.load(fileInput);
            encryptPasswords = Boolean.parseBoolean(properties.getProperty("encryptPasswords", "true"));
        } catch (IOException e) {
            // Errore durante il caricamento delle impostazioni, utilizza le impostazioni predefinite
            encryptPasswords = true;
            saveSettings();
        }
    }

    private static void saveSettings() {
        Properties properties = new Properties();
        properties.setProperty("encryptPasswords", Boolean.toString(encryptPasswords));
        try (FileOutputStream fileOutput = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(fileOutput, "Application Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
