import java.io.*;
import java.nio.file.*;

public class JarExtractor {

    public static void extractFile(String resourcePath, String outputDirPath) {
        try (InputStream is = JarExtractor.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }
            File outputFile = new File(outputDirPath, Paths.get(resourcePath).getFileName().toString());
            Files.createDirectories(outputFile.getParentFile().toPath());
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
