import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import javax.swing.*;

public class FingerPrintEditor {
    public static void removeKnownHostsLine(String line) {
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

    public static void loadKnownHostsContent(JPanel panel) {
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

    public static void addKnownHostsRow(JPanel panel, String line, int gridY) {
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

    public static void editFingerprint() {
        MainClass.knownHostsFrame = new JFrame("Edit Fingerprint");
        MainClass.knownHostsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        MainClass.knownHostsFrame.setSize(700, 300);
        MainClass.knownHostsFrame.setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(panel);
        MainClass.knownHostsFrame.add(scrollPane, BorderLayout.CENTER);

        loadKnownHostsContent(panel);

        MainClass.knownHostsFrame.setVisible(true);
    }

}
