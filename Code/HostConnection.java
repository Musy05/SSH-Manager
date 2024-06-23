import java.io.IOException;
import java.net.InetAddress;
import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class HostConnection {

    public static void connectToSSH(String ip, String username, String password) {
        try {
            // Usa ProcessBuilder per costruire ed eseguire il comando
            Settings settings = new Settings();
            String enterDelay = Integer.toString(settings.getEnterDelay());
            ProcessBuilder processBuilder = new ProcessBuilder("wscript", "./resources/connect_ssh.vbs", ip, username, password, enterDelay);
            
            // Esegui il comando
            Process process = processBuilder.start();
            
            // Attendi che il processo termini
            process.waitFor();
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static long pingHost(String ip) {
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

    public static void updatePings() {
        for (int i = 0; i < MainClass.ipAddresses.size(); i++) {
            String ip = MainClass.ipAddresses.get(i);
            JLabel statusLabel = MainClass.statusLabels.get(i);
            JPanel rowPanel = (JPanel) MainClass.contentPanel.getComponent(i);  // Ottieni il pannello della riga
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

    public static void closeAllConnections() {
        try {
            // Usa ProcessBuilder per costruire ed eseguire il comando
            ProcessBuilder processBuilder = new ProcessBuilder("wscript", "./resources/close_all_connection.vbs");
            
            // Esegui il comando
            Process process = processBuilder.start();
            
            // Attendi che il processo termini
            process.waitFor();
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Task failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
