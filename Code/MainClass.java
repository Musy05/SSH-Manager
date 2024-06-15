// MainClass.java

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainClass {
    public static JPanel contentPanel;
    public static JFrame knownHostsFrame;
    public static List<JLabel> statusLabels = new ArrayList<>(); // List to hold the status labels
    public static List<String> ipAddresses = new ArrayList<>(); // List to hold the IP addresses
    private static Settings settings = new Settings(); // Add settings instance

    private static void createAndShowGUI() {
        // Crea il frame (finestra)
        JFrame frame = new JFrame("SSH Connection Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Centra la finestra rispetto allo schermo
        frame.setLocationRelativeTo(null);

        // Crea un JPanel per visualizzare il contenuto del file
        contentPanel = new JPanel(new GridBagLayout());
        DataHandler.loadFileContent();

        // Aggiungi un JScrollPane per rendere il JPanel scrollabile
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Crea un pannello per i pulsanti
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JButton addButton = new JButton("Add Connection");
        addButton.addActionListener(e -> DataHandler.addConnection());
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(addButton, gbc);

        JButton editButton = new JButton("Edit Fingerprint");
        editButton.addActionListener(e -> FingerPrintEditor.editFingerprint());
        gbc.gridx = 1;
        gbc.gridy = 0;
        buttonPanel.add(editButton, gbc);

        JButton updatePingButton = new JButton("Update Pings");
        updatePingButton.addActionListener(e -> HostConnection.updatePings());
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        buttonPanel.add(updatePingButton, gbc);

        JButton settingsButton = new JButton("Settings");
        settingsButton.addActionListener(e -> settings.showSettingsDialog());
        gbc.gridx = 3;
        gbc.gridy = 0;
        buttonPanel.add(settingsButton, gbc);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Mostra la finestra
        frame.setVisible(true);
    }

    public static void addRow(String ip, String username, String password, int index, int gridY) {
        JPanel rowPanel = new JPanel(new GridBagLayout());
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Color.GRAY); // Imposta il colore iniziale come grigio

        JLabel pingLabel = new JLabel(" ");
        pingLabel.setForeground(Color.BLUE); // Imposta il colore del testo del ping su blu

        JLabel ipLabel = new JLabel("IP: " + ip + " | Username: " + username);

        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton connectButton = new JButton("Connect");

        editButton.addActionListener(e -> DataHandler.editConnection(index));
        deleteButton.addActionListener(e -> DataHandler.deleteConnection(index));
        connectButton.addActionListener(e -> HostConnection.connectToSSH(ip, username, password));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        rowPanel.add(statusLabel, gbc);

        gbc.gridx = 1;
        rowPanel.add(pingLabel, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST; // Imposta l'allineamento a sinistra
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        rowPanel.add(ipLabel, gbc);

        gbc.gridx = 3;
        gbc.anchor = GridBagConstraints.EAST; // Imposta l'allineamento a destra
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        rowPanel.add(editButton, gbc);

        gbc.gridx = 4;
        rowPanel.add(deleteButton, gbc);

        gbc.gridx = 5;
        rowPanel.add(connectButton, gbc);

        GridBagConstraints panelGbc = new GridBagConstraints();
        panelGbc.gridx = 0;
        panelGbc.gridy = gridY;
        panelGbc.anchor = GridBagConstraints.WEST;
        panelGbc.insets = new Insets(5, 5, 5, 5);
        panelGbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(rowPanel, panelGbc);

        statusLabels.add(statusLabel); // Add the status label to the list

        // Esegui il ping in un thread separato per non bloccare l'interfaccia utente
        new Thread(() -> {
            long pingTime = HostConnection.pingHost(ip);
            if (pingTime != -1) {
                statusLabel.setBackground(Color.GREEN); // Se il ping ha successo, imposta il colore verde
                pingLabel.setText(pingTime + " ms"); // Aggiorna il tempo di ping
            } else {
                statusLabel.setBackground(Color.RED); // Se il ping fallisce, imposta il colore rosso
                pingLabel.setText("N/A ms"); // Aggiorna il tempo di ping
            }
        }).start();
    }

    public static void main(String[] args) {
        CryptoUtils.checkFirstRun();
        if (CryptoUtils.isFirstRun) {
            CryptoUtils.setPassword();
        }

        SwingUtilities.invokeLater(MainClass::createAndShowGUI);
    }
}
