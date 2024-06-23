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
        JFrame frame = new JFrame("SSH Connection Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.setContentPane(mainPanel);

        contentPanel = new JPanel(new GridBagLayout());
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(contentPanel, BorderLayout.NORTH);

        DataHandler.loadFileContent();

        JScrollPane scrollPane = new JScrollPane(containerPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        Dimension buttonSize = new Dimension(150, 30); // Dimensione fissa per tutti i pulsanti

        JButton addButton = new JButton("Add Connection");
        addButton.setPreferredSize(buttonSize);
        addButton.addActionListener(e -> DataHandler.addConnection());
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(addButton, gbc);

        JButton closeButton = new JButton("Close All");
        closeButton.setPreferredSize(buttonSize);
        closeButton.addActionListener(e -> HostConnection.closeAllConnections());
        gbc.gridx = 0;
        gbc.gridy = 1;
        buttonPanel.add(closeButton, gbc);

        JButton updatePingButton = new JButton("Update Pings");
        updatePingButton.setPreferredSize(buttonSize);
        updatePingButton.addActionListener(e -> HostConnection.updatePings());
        gbc.gridx = 0;
        gbc.gridy = 2;
        buttonPanel.add(updatePingButton, gbc);

        JButton editButton = new JButton("Edit Fingerprint");
        editButton.setPreferredSize(buttonSize);
        editButton.addActionListener(e -> FingerPrintEditor.editFingerprint());
        gbc.gridx = 0;
        gbc.gridy = 3;
        buttonPanel.add(editButton, gbc);

        JButton settingsButton = new JButton("Settings");
        settingsButton.setPreferredSize(buttonSize);
        settingsButton.addActionListener(e -> settings.showSettingsDialog());
        gbc.gridx = 0;
        gbc.gridy = 4;
        buttonPanel.add(settingsButton, gbc);

        frame.add(buttonPanel, BorderLayout.EAST);

        // Mostra la finestra
        frame.setVisible(true);
    }

    public static void addRow(String ip, String username, String password, int index, int gridY) {
        JPanel rowPanel = new JPanel(new GridBagLayout());

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(Color.GRAY);

        JLabel pingLabel = new JLabel(" ");
        pingLabel.setForeground(Color.BLUE);

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
        gbc.anchor = GridBagConstraints.WEST;
        rowPanel.add(statusLabel, gbc);

        gbc.gridx = 1;
        rowPanel.add(pingLabel, gbc);

        gbc.gridx = 2;
        gbc.weightx = 1.0;
        rowPanel.add(ipLabel, gbc);

        /*gbc.gridx = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(Box.createGlue(), gbc);

        contentPanel.revalidate();
        contentPanel.repaint();*/

        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        rowPanel.add(editButton, gbc);

        gbc.gridx = 4;
        rowPanel.add(deleteButton, gbc);

        gbc.gridx = 5;
        rowPanel.add(connectButton, gbc);


        GridBagConstraints panelGbc = new GridBagConstraints();
        panelGbc.gridx = 0;
        panelGbc.gridy = gridY;
        panelGbc.anchor = GridBagConstraints.NORTHWEST;
        panelGbc.insets = new Insets(5, 5, 5, 5);
        panelGbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(rowPanel, panelGbc);

        statusLabels.add(statusLabel);
        new Thread(() -> {
            long pingTime = HostConnection.pingHost(ip);
            if (pingTime != -1) {
                statusLabel.setBackground(Color.GREEN);
                pingLabel.setText(pingTime + " ms");
            } else {
                statusLabel.setBackground(Color.RED);
                pingLabel.setText("N/A ms");
            }
        }).start();
    }
    public static void main(String[] args) {
        CryptoUtils.checkFirstRun();
        if (CryptoUtils.isFirstRun) {
            JarExtractor.extractFile("/resources/connect_ssh.vbs", "./resources/");
            JarExtractor.extractFile("/resources/close_all_connection.vbs", "./resources/");
            CryptoUtils.setPassword();
        }
        SwingUtilities.invokeLater(MainClass::createAndShowGUI);
    }
}
