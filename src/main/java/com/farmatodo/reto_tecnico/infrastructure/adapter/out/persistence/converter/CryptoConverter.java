package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.converter;

import com.farmatodo.reto_tecnico.application.config.FarmatodoProperties;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * JPA AttributeConverter for encrypting/decrypting sensitive data using AES-GCM.
 * Used to secure payment tokens and other PCI-compliant data at rest.
 *
 * AES-GCM provides both confidentiality and authenticity.
 * - Key size: 256 bits (derived from configuration)
 * - IV size: 12 bytes (96 bits) - recommended for GCM
 * - Tag size: 128 bits - authentication tag
 *
 * CRITICAL SECURITY: Encryption key MUST be:
 * 1. Stored in environment variable (never in code/config files)
 * 2. At least 32 characters (256 bits) for AES-256
 * 3. Rotated periodically per security policy
 */
@Converter
@Component
@RequiredArgsConstructor
@Slf4j
public class CryptoConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int AES_KEY_SIZE = 32; // 256 bits

    private final FarmatodoProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Encrypts plaintext to ciphertext before persisting to database.
     * Each encryption uses a unique IV (Initialization Vector) stored with the ciphertext.
     *
     * Format: Base64(IV || Ciphertext || AuthTag)
     *
     * @param plaintext the plaintext to encrypt (can be null)
     * @return Base64-encoded encrypted data, or null if input is null
     */
    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return null;
        }

        try {
            // Generate random IV for this encryption
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Encrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), parameterSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV + Ciphertext for storage
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);

            // Encode to Base64 for database storage
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Failed to encrypt sensitive data", e);
        }
    }

    /**
     * Decrypts ciphertext from database to plaintext.
     * Extracts IV from stored data and uses it for decryption.
     *
     * @param encryptedData Base64-encoded encrypted data from database
     * @return decrypted plaintext, or null if input is null
     */
    @Override
    public String convertToEntityAttribute(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return null;
        }

        try {
            // Decode from Base64
            byte[] decoded = Base64.getDecoder().decode(encryptedData);

            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            // Decrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), parameterSpec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Failed to decrypt sensitive data", e);
        }
    }

    /**
     * Derives AES SecretKey from configured encryption key.
     * Uses first 32 bytes of configured key for AES-256.
     *
     * @return SecretKey for AES encryption
     * @throws IllegalStateException if encryption key is not configured
     */
    private SecretKey getSecretKey() {
        String configuredKey = properties.getEncryption().getKey();

        if (configuredKey == null || configuredKey.isEmpty()) {
            throw new IllegalStateException(
                "Encryption key not configured. Set farmatodo.encryption.key environment variable."
            );
        }

        if (configuredKey.length() < AES_KEY_SIZE) {
            throw new IllegalStateException(
                String.format("Encryption key must be at least %d characters for AES-256", AES_KEY_SIZE)
            );
        }

        // Use first 32 bytes of configured key
        byte[] keyBytes = configuredKey.substring(0, AES_KEY_SIZE).getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "AES");
    }
}
