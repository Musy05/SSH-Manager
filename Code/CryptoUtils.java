import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.swing.JOptionPane;
import java.io.*;
import java.nio.file.Files;

public class CryptoUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 128;
    private static final int SALT_SIZE = 16;
    private static final int IV_SIZE = 16;
    private static final int ITERATION_COUNT = 65536;
    private static final String PASSWORD_FILE = "password.txt";
    public static boolean isFirstRun = false;
    private static SecretKey secretKey;
    
    public static void checkFirstRun() {
        File passwordFile = new File(PASSWORD_FILE);
        isFirstRun = !passwordFile.exists();
    }

    public static void setPassword() {
        String password = JOptionPane.showInputDialog("Enter a new password:");
        try {
            byte[] salt = generateSalt();
            Files.write(new File("salt.dat").toPath(), salt);
            getKeyFromPassword(password, salt);
            initializeCrypto(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initializeCrypto(String password) {
        try {
            byte[] salt = Files.readAllBytes(new File("salt.dat").toPath());
            getKeyFromPassword(password, salt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SecretKey getKeyFromPassword(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_SIZE);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), ALGORITHM);
    }

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);
        return salt;
    }

    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        byte[] encryptedDataWithIv = new byte[IV_SIZE + encryptedData.length];
        System.arraycopy(iv, 0, encryptedDataWithIv, 0, IV_SIZE);
        System.arraycopy(encryptedData, 0, encryptedDataWithIv, IV_SIZE, encryptedData.length);
        return Base64.getEncoder().encodeToString(encryptedDataWithIv);
    }

    public static String decrypt(String data, SecretKey key) throws Exception {
        byte[] encryptedDataWithIv = Base64.getDecoder().decode(data);
        byte[] iv = new byte[IV_SIZE];
        byte[] encryptedData = new byte[encryptedDataWithIv.length - IV_SIZE];
        System.arraycopy(encryptedDataWithIv, 0, iv, 0, IV_SIZE);
        System.arraycopy(encryptedDataWithIv, IV_SIZE, encryptedData, 0, encryptedData.length);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData);
    }
}
