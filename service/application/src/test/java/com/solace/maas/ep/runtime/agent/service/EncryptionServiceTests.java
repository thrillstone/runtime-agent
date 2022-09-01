package com.solace.maas.ep.runtime.agent.service;

import com.solace.maas.ep.runtime.agent.TestConfig;
import com.solace.maas.ep.runtime.agent.service.encryption.EncryptionService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestConfig.class)
public class EncryptionServiceTests {

    @InjectMocks
    EncryptionService encryptionService;

    String keyPath = "src/test/testEncryption/";

    @AfterEach
    public void clean() {
        try {
            Files.deleteIfExists(Path.of(keyPath + "private.pem"));
            Files.deleteIfExists(Path.of(keyPath + "public.pem"));
            Files.deleteIfExists(Path.of(keyPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Test
    public void testExportAndParseRSAKeysThenEncryptionAndDecryption() {
        String password = "top secret password";

        encryptionService.setKeyExportPath(String.valueOf(keyPath));

        encryptionService.generateAndExportKeysAsPEM();
        encryptionService.parseAndInitializeKeys();

        byte[] cipher = encryptionService.encrypt(password);
        String decryptedPassword = encryptionService.decrypt(cipher);

        assertEquals(password, decryptedPassword);
        assertThatNoException();
    }
}

