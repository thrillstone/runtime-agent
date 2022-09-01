package com.solace.maas.ep.runtime.agent.service.encryption;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@Component
@Slf4j
@Profile("!TEST")
public class EncryptionService {
    static final String PKCS_8_PRIVATE_PEM_HEADER = "-----BEGIN PRIVATE KEY-----";
    static final String PKCS_8_PRIVATE_PEM_FOOTER = "-----END PRIVATE KEY-----";
    static final String PKCS_8_PUBLIC_PEM_HEADER = "-----BEGIN PUBLIC KEY-----";
    static final String PKCS_8_PUBLIC_PEM_FOOTER = "-----END PUBLIC KEY-----";
    private final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private final String ALGORITHM = "RSA";
    private final int ALGORITHM_BITS = 1024;
    private String keyExportPath = "service/application/src/main/resources/rsa/";
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public Map<String, Key> parseRSAKeys(String privateKeyFile, String publicKeyFile)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Map<String, Key> keys = new HashMap<>();

        String privateKeyContent = Files.readString(Paths.get(privateKeyFile));
        String publicKeyContent = Files.readString(Paths.get(publicKeyFile));

        privateKeyContent = privateKeyContent
                .replaceAll("\\n", "")
                .replace(PKCS_8_PRIVATE_PEM_HEADER, "")
                .replace(PKCS_8_PRIVATE_PEM_FOOTER, "");

        publicKeyContent = publicKeyContent
                .replaceAll("\\n", "")
                .replace(PKCS_8_PUBLIC_PEM_HEADER, "")
                .replace(PKCS_8_PUBLIC_PEM_FOOTER, "");

        KeyFactory kf = KeyFactory.getInstance("RSA");

        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        PrivateKey prvKey = kf.generatePrivate(keySpecPKCS8);
        keys.put("private", prvKey);

        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(keySpecX509);
        keys.put("public", pubKey);

        return keys;
    }

    public void parseAndInitializeKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        Map<String, Key> keys = parseRSAKeys(keyExportPath + "private.pem", keyExportPath + "public.pem");
        this.privateKey = (PrivateKey) keys.get("private");
        this.publicKey = (PublicKey) keys.get("public");
    }

    public KeyPair generateRSAKeys() {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (keyPairGenerator != null) {
            SecureRandom secureRandom = new SecureRandom();
            keyPairGenerator.initialize(ALGORITHM_BITS, secureRandom);
            return keyPairGenerator.genKeyPair();
        }
        return null;
    }

    public void writeToFile(String path, String key) {
        if (Files.notExists(Path.of(path))) {
            File f = new File(path);
            f.getParentFile().mkdir();
        }

        try (FileWriter myWriter = new FileWriter(path, StandardCharsets.UTF_8)) {
            myWriter.write(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateAndExportKeysAsPEM() {
        KeyPair rsaKeyPair = generateRSAKeys();
        this.publicKey = Objects.requireNonNull(rsaKeyPair).getPublic();
        this.privateKey = Objects.requireNonNull(rsaKeyPair).getPrivate();

        log.info("Generating encryption key using " + this.privateKey.getAlgorithm() + " algorithm");

        String encodedPrivateKey =
                PKCS_8_PRIVATE_PEM_HEADER + "\n"
                        + Base64.getEncoder().encodeToString(rsaKeyPair.getPrivate().getEncoded()) + "\n"
                        + PKCS_8_PRIVATE_PEM_FOOTER + "\n";

        String encodedPublicKey =
                PKCS_8_PUBLIC_PEM_HEADER + "\n"
                        + Base64.getEncoder().encodeToString(rsaKeyPair.getPublic().getEncoded()) + "\n"
                        + PKCS_8_PUBLIC_PEM_FOOTER + "\n";

        writeToFile(keyExportPath + "private.pem", encodedPrivateKey);
        writeToFile(keyExportPath + "public.pem", encodedPublicKey);

        log.info("Encryption keys are exported to {} successfully", keyExportPath);
    }

    public byte[] encrypt(String message)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        Cipher encryptionCipher = Cipher.getInstance(TRANSFORMATION);
        encryptionCipher.init(Cipher.ENCRYPT_MODE, this.privateKey);

        return encryptionCipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
    }

    public String decrypt(byte[] encryptedMessage)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        Cipher decryptionCipher = Cipher.getInstance(TRANSFORMATION);
        decryptionCipher.init(Cipher.DECRYPT_MODE, this.publicKey);
        byte[] decryptionBytes = decryptionCipher.doFinal(encryptedMessage);

        return new String(decryptionBytes, StandardCharsets.UTF_8);
    }
}
