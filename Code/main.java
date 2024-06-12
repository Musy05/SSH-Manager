import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


public class main {
    private static JPanel contentPanel;
    private static JFrame knownHostsFrame;
    private static List<JLabel> statusLabels = new ArrayList<>();  // List to hold the status labels
    private static List<String> ipAddresses = new ArrayList<>();   // List to hold the IP addresses

    private static void createAndShowGUI() {
        // Crea il frame (finestra)
        JFrame frame = new JFrame("SSH Connection Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
    
        // Centra la finestra rispetto allo schermo
        frame.setLocationRelativeTo(null);
    
        // Crea un JPanel per visualizzare il contenuto del file
        contentPanel = new JPanel(new GridBagLayout());
        loadFileContent();
    
        // Aggiungi un JScrollPane per rendere il JPanel scrollabile
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        frame.add(scrollPane, BorderLayout.CENTER);
    
        // Crea un pannello per i pulsanti
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
    
        JButton addButton = new JButton("Add Connection");
        addButton.addActionListener(e -> addConnection());
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(addButton, gbc);
    
        JButton editButton = new JButton("Edit Fingerprint");
        editButton.addActionListener(e -> editFingerprint());
        gbc.gridx = 1;
        gbc.gridy = 0;
        buttonPanel.add(editButton, gbc);
    
        JButton updatePingButton = new JButton("Update Pings");
        updatePingButton.addActionListener(e -> updatePings());
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        buttonPanel.add(updatePingButton, gbc);
    
        frame.add(buttonPanel, BorderLayout.SOUTH);
    
        // Mostra la finestra
        frame.setVisible(true);
    }
    

    private static void loadFileContent() {
        File file = new File("password.txt");
        contentPanel.removeAll();
        statusLabels.clear();
        ipAddresses.clear();

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int lineCounter = 0;
                String ip = null, username = null, password = null;
                int gridY = 0;

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue; // salta le linee vuote
                    }

                    switch (lineCounter % 4) {
                        case 0:
                            ip = line;
                            break;
                        case 1:
                            username = line;
                            break;
                        case 2:
                            password = line;
                            break;
                        case 3:
                            addRow(ip, username, password, lineCounter / 4, gridY++);
                            ipAddresses.add(ip);
                            break;
                    }
                    lineCounter++;
                }

                // Aggiungi l'ultimo set di informazioni se il file non termina con una riga vuota
                if (lineCounter % 4 != 0) {
                    addRow(ip, username, password, lineCounter / 4, gridY++);
                    ipAddresses.add(ip);
                }
            } catch (IOException e) {
                System.err.println("Error! Cannot read password.txt: " + e.getMessage());
            }
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(Box.createGlue(), gbc); // Aggiungi uno spazio che riempie il vuoto

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private static void addRow(String ip, String username, String password, int index, int gridY) {
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
    
        editButton.addActionListener(e -> editConnection(index));
        deleteButton.addActionListener(e -> deleteConnection(index));
        connectButton.addActionListener(e -> connectToSSH(ip, username, password));
    
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
    
        statusLabels.add(statusLabel);  // Add the status label to the list
    
        // Esegui il ping in un thread separato per non bloccare l'interfaccia utente
        new Thread(() -> {
            long pingTime = pingHost(ip);
            if (pingTime != -1) {
                statusLabel.setBackground(Color.GREEN); // Se il ping ha successo, imposta il colore verde
                pingLabel.setText(pingTime + " ms");  // Aggiorna il tempo di ping
            } else {
                statusLabel.setBackground(Color.RED); // Se il ping fallisce, imposta il colore rosso
                pingLabel.setText("N/A ms");  // Aggiorna il tempo di ping
            }
        }).start();
    }
    
    

    private static long pingHost(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            long start = System.currentTimeMillis();
            boolean reachable = address.isReachable(1000); // Timeout di 1 secondo
            long end = System.currentTimeMillis();
            return reachable ? (end - start) : -1;
        } catch (IOException e) {
            return -1;
        }
    }    

    private static void updatePings() {
        for (int i = 0; i < ipAddresses.size(); i++) {
            String ip = ipAddresses.get(i);
            JLabel statusLabel = statusLabels.get(i);
            JPanel rowPanel = (JPanel) contentPanel.getComponent(i);  // Ottieni il pannello della riga
            JLabel pingLabel = (JLabel) rowPanel.getComponent(1);  // Ottieni la JLabel del ping
    
            // Esegui il ping in un thread separato per non bloccare l'interfaccia utente
            new Thread(() -> {
                long pingTime = pingHost(ip);
                if (pingTime != -1) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setBackground(Color.GREEN); // Se il ping ha successo, imposta il colore verde
                        pingLabel.setText(pingTime + " ms");  // Aggiorna il tempo di ping
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setBackground(Color.RED); // Se il ping fallisce, imposta il colore rosso
                        pingLabel.setText("Ping: N/A ms");  // Aggiorna il tempo di ping
                    });
                }
            }).start();
        }
    }
    
    

    private static void connectToSSH(String ip, String username, String password) {
        try {
            // Crea il comando per eseguire lo script VBS
            String command = String.format("wscript connect_ssh.vbs %s %s %s", ip, username, password);
            
            // Esegui il comando
            Process process = Runtime.getRuntime().exec(command);
            
            // Attendi che il processo termini
            process.waitFor();
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    private static void addConnection() {
        // Chiedi all'utente IP, username e password
        String ip = JOptionPane.showInputDialog("Enter IP Address:");
        if (ip == null || ip.isEmpty()) return;

        String username = JOptionPane.showInputDialog("Enter Username:");
        if (username == null || username.isEmpty()) return;

        String password = JOptionPane.showInputDialog("Enter Password:");
        if (password == null || password.isEmpty()) return;

        // Salva le informazioni nel file
        try (FileWriter writer = new FileWriter("password.txt", true)) {
            writer.write(ip + "\n");
            writer.write(username + "\n");
            writer.write(password + "\n");
            writer.write("\n");  // Aggiungi una riga vuota per separare i set di informazioni
        } catch (IOException e) {
            System.err.println("Error! Cannot write password.txt: " + e.getMessage());
        }

        // Aggiungi la nuova connessione alla GUI
        loadFileContent();
    }

    private static void removeKnownHostsLine(String line) {
        try {
            File knownHostsFile = new File(System.getProperty("user.home") + "/.ssh/known_hosts");
            List<String> lines = new ArrayList<>();

            // Leggi tutte le righe tranne quella da eliminare
            try (BufferedReader reader = new BufferedReader(new FileReader(knownHostsFile))) {
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    if (!currentLine.equals(line)) {
                        lines.add(currentLine);
                    }
                }
            }

            // Scrivi le righe rimanenti nel file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(knownHostsFile))) {
                for (String currentLine : lines) {
                    writer.write(currentLine);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadKnownHostsContent(JPanel panel) {
        try {
            File knownHostsFile = new File(System.getProperty("user.home") + "/.ssh/known_hosts");
            if (!knownHostsFile.exists()) {
                JOptionPane.showMessageDialog(null, "known_hosts file does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(knownHostsFile));
            String line;
            int gridY = 0;
            List<String> lines = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                lines.add(line);
                addKnownHostsRow(panel, line, gridY++);
            }
            reader.close();

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = GridBagConstraints.RELATIVE;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            panel.add(Box.createGlue(), gbc); // Aggiungi uno spazio che riempie il vuoto

            panel.revalidate();
            panel.repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addKnownHostsRow(JPanel panel, String line, int gridY) {
        JButton deleteButton = new JButton("Delete");
        JLabel label = new JLabel(line);

        deleteButton.addActionListener(e -> {
            // Rimuovi la riga dal file known_hosts
            removeKnownHostsLine(line);
            // Rimuovi la riga dal pannello
            panel.remove(label);
            panel.remove(deleteButton);
            panel.revalidate();
            panel.repaint();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(deleteButton, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(label, gbc);
    }

    private static void editFingerprint() {
        knownHostsFrame = new JFrame("Edit Fingerprint");
        knownHostsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        knownHostsFrame.setSize(700, 300);
        knownHostsFrame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(panel);
        knownHostsFrame.add(scrollPane, BorderLayout.CENTER);

        loadKnownHostsContent(panel);

        knownHostsFrame.setVisible(true);
    }

    private static void editConnection(int index) {
        File file = new File("password.txt");
        if (file.exists()) {
            try {
                List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
                int startIdx = index * 4;
                String ip = JOptionPane.showInputDialog("Edit IP Address:", lines.get(startIdx));
                if (ip == null || ip.isEmpty()) return;

                String username = JOptionPane.showInputDialog("Edit Username:", lines.get(startIdx + 1));
                if (username == null || username.isEmpty()) return;

                String password = JOptionPane.showInputDialog("Edit Password:", lines.get(startIdx + 2));
                if (password == null || password.isEmpty()) return;

                lines.set(startIdx, ip);
                lines.set(startIdx + 1, username);
                lines.set(startIdx + 2, password);

                Files.write(file.toPath(), lines);
                loadFileContent();
            } catch (IOException e) {
                System.err.println("Error! Cannot edit password.txt: " + e.getMessage());
            }
        }
    }

    private static void deleteConnection(int index) {
        File file = new File("password.txt");
        if (file.exists()) {
            try {
                List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
                int startIdx = index * 4;
                for (int i = 0; i < 4; i++) {
                    lines.remove(startIdx);
                }
                Files.write(file.toPath(), lines);
                loadFileContent();
            } catch (IOException e) {
                System.err.println("Error! Cannot delete from password.txt: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(main::createAndShowGUI);
    }
}
