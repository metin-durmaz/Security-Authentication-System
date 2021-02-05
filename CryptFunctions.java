import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * CryptFunctions class includes functions related to encryption and decryption
 **/
public class CryptFunctions {

    /**
     * It was written to hash given text.
     *
     * @param text --> Given Text
     * @return --> Hashed text
     */
    public static String SHA1Hash(String text) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");

        return Base64.getEncoder().encodeToString(md.digest(text.getBytes()));
    }

    /**
     * It was written to encrypt using the RSA algorithm.
     *
     * @param text      --> plainText
     * @param publicKey --> Public Key
     * @return --> Base 64 encrypted text
     **/
    public static String encryptRSA(String text, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes()));
    }

    /**
     * It was written to decrypt using the RSA algorithm.
     *
     * @param text       --> plainText
     * @param privateKey --> Private Key
     * @return --> Base 64 decrypted text
     **/
    public static String decryptRSA(String text, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(cipher.doFinal(Base64.getDecoder().decode(text.getBytes())));
    }

    /**
     * A certificate instance was created using the certificate path.
     * The publicKey was accessed from within the instance.
     *
     * @param certName --> Certificate file name
     * @return --> publicKey
     **/
    public static PublicKey getPublicKey(String certName) throws Exception {
        File file = new File("cert");
        FileInputStream in = new FileInputStream(file.getAbsolutePath() + File.separator + certName + ".cert");
        CertificateFactory factory = CertificateFactory.getInstance("X509");
        X509Certificate certificate = (X509Certificate) factory.generateCertificate(in);
        return certificate.getPublicKey();
    }

    /**
     * It creates private key by using the text taken from 5 different privatKeyFile.
     *
     * @param keyName --> privateKeyFile name
     * @return --> Produced private key from txt file
     **/
    public static PrivateKey getPrivateKeyFromTXT(String keyName) throws Exception {
        File keyFile = new File("keys");
        String privateKeyContent = new String(Files.readAllBytes(Paths.get(keyFile.getAbsolutePath() + File.separator + keyName + ".txt")));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));

        return kf.generatePrivate(keySpecPKCS8);
    }

    /**
     * It takes the private key from the keyStore file with .jks extension.
     *
     * @return --> privateKey
     **/
    public static PrivateKey getPrivateKeyFromJKS(String keyStoreName) throws Exception {
        FileInputStream in = new FileInputStream(keyStoreName + ".jks");
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(in, "metindurmaz".toCharArray());
        return (PrivateKey) (keyStore.getKey(keyStoreName, "metindurmaz".toCharArray()));
    }

    /**
     * It was written to encrypt given text.
     *
     * @param input --> plainText
     * @param key   --> Session Key
     * @return --> cipherText
     */
    public static String encryptAES(String input, String key) throws Exception {

        SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, sKeySpec);
        byte[] cipherText = cipher.doFinal(input.getBytes());

        return Base64.getEncoder().encodeToString(cipherText);
    }

    /**
     * It was written to decrypt given text.
     *
     * @param input --> cipherText
     * @param key   --> Session Key
     * @return --> plainText
     */
    public static String decryptAES(String input, String key) throws Exception {

        SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, sKeySpec);

        return new String(cipher.doFinal(Base64.getDecoder().decode(input)));
    }

    /**
     * It was written to get instant timeStamp data.
     * The created date format was used in the required methods and while preparing the logFile.
     *
     * @return --> Formatted timeStamp
     **/
    public static String timeStampGetter() {
        java.sql.Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return dateFormat.format(timestamp);
    }

}