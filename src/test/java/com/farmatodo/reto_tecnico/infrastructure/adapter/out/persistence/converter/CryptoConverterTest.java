package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.converter;

import com.farmatodo.reto_tecnico.application.config.FarmatodoProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CryptoConverter.
 * Tests AES-GCM encryption and decryption of sensitive data.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CryptoConverter Unit Tests")
class CryptoConverterTest {

    @Mock
    private FarmatodoProperties properties;

    @Mock
    private FarmatodoProperties.Encryption encryptionProperties;

    private CryptoConverter converter;

    // Valid 32-character key for AES-256
    private static final String VALID_KEY = "12345678901234567890123456789012";

    @BeforeEach
    void setUp() {
        lenient().when(properties.getEncryption()).thenReturn(encryptionProperties);
        converter = new CryptoConverter(properties);
    }

    @Test
    @DisplayName("Should encrypt and decrypt plaintext successfully")
    void shouldEncryptAndDecryptSuccessfully() {
        // Given
        when(encryptionProperties.getKey()).thenReturn(VALID_KEY);
        String plaintext = "tok_sensitive_payment_token_12345";

        // When
        String encrypted = converter.convertToDatabaseColumn(plaintext);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // Then
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEqualTo(plaintext);
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Should return null when encrypting null input")
    void shouldReturnNullWhenEncryptingNullInput() {
        // When
        String result = converter.convertToDatabaseColumn(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when encrypting empty input")
    void shouldReturnNullWhenEncryptingEmptyInput() {
        // When
        String result = converter.convertToDatabaseColumn("");

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when decrypting null input")
    void shouldReturnNullWhenDecryptingNullInput() {
        // When
        String result = converter.convertToEntityAttribute(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when decrypting empty input")
    void shouldReturnNullWhenDecryptingEmptyInput() {
        // When
        String result = converter.convertToEntityAttribute("");

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should produce different ciphertext for same plaintext (due to random IV)")
    void shouldProduceDifferentCiphertextForSamePlaintext() {
        // Given
        when(encryptionProperties.getKey()).thenReturn(VALID_KEY);
        String plaintext = "same_plaintext_value";

        // When
        String encrypted1 = converter.convertToDatabaseColumn(plaintext);
        String encrypted2 = converter.convertToDatabaseColumn(plaintext);

        // Then
        assertThat(encrypted1).isNotEqualTo(encrypted2); // Different IVs
        assertThat(converter.convertToEntityAttribute(encrypted1)).isEqualTo(plaintext);
        assertThat(converter.convertToEntityAttribute(encrypted2)).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Should throw exception when encryption key is null")
    void shouldThrowExceptionWhenEncryptionKeyIsNull() {
        // Given
        when(encryptionProperties.getKey()).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> converter.convertToDatabaseColumn("test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("encrypt");
    }

    @Test
    @DisplayName("Should throw exception when encryption key is empty")
    void shouldThrowExceptionWhenEncryptionKeyIsEmpty() {
        // Given
        when(encryptionProperties.getKey()).thenReturn("");

        // When & Then
        assertThatThrownBy(() -> converter.convertToDatabaseColumn("test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("encrypt");
    }

    @Test
    @DisplayName("Should throw exception when encryption key is too short")
    void shouldThrowExceptionWhenEncryptionKeyIsTooShort() {
        // Given: Key shorter than 32 characters
        when(encryptionProperties.getKey()).thenReturn("short_key");

        // When & Then
        assertThatThrownBy(() -> converter.convertToDatabaseColumn("test"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("encrypt");
    }

    @Test
    @DisplayName("Should handle special characters in plaintext")
    void shouldHandleSpecialCharactersInPlaintext() {
        // Given
        when(encryptionProperties.getKey()).thenReturn(VALID_KEY);
        String plaintext = "tök_$pecial_çhärs_áéíóú_日本語_🎉";

        // When
        String encrypted = converter.convertToDatabaseColumn(plaintext);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Should handle long plaintext")
    void shouldHandleLongPlaintext() {
        // Given
        when(encryptionProperties.getKey()).thenReturn(VALID_KEY);
        String plaintext = "a".repeat(1000); // 1000 character string

        // When
        String encrypted = converter.convertToDatabaseColumn(plaintext);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        // Then
        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("Should throw exception when decrypting invalid Base64")
    void shouldThrowExceptionWhenDecryptingInvalidBase64() {
        // Given
        when(encryptionProperties.getKey()).thenReturn(VALID_KEY);

        // When & Then
        assertThatThrownBy(() -> converter.convertToEntityAttribute("not_valid_base64!!!"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("decrypt");
    }

    @Test
    @DisplayName("Should throw exception when decrypting corrupted data")
    void shouldThrowExceptionWhenDecryptingCorruptedData() {
        // Given
        when(encryptionProperties.getKey()).thenReturn(VALID_KEY);
        String encrypted = converter.convertToDatabaseColumn("original_text");
        // Corrupt the encrypted data
        String corrupted = encrypted.substring(0, encrypted.length() - 5) + "XXXXX";

        // When & Then
        assertThatThrownBy(() -> converter.convertToEntityAttribute(corrupted))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("decrypt");
    }
}
