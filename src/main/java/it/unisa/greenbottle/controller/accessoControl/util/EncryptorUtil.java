package it.unisa.greenbottle.controller.accessoControl.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Classe EncryptorUtil, utilizzata per la cifratura e decifratura di stringhe.
 * La chiave non risiede nel codice: viene letta dalla configurazione
 * (proprieta' {@code greenbottle.security.encryption-key}, sovrascrivibile tramite la
 * variabile d'ambiente {@code GREENBOTTLE_SECURITY_ENCRYPTION_KEY}).
 */
@Component
public class EncryptorUtil {

  private static final String ALGORITHM = "AES";
  private static final String TRANSFORMATION = "AES/GCM/NoPadding";
  private static final int TAG_LENGTH_BITS = 128;
  private static final int IV_LENGTH_BYTES = 12;

  private final SecretKey secretKey;
  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * Costruttore.
   *
   * @param encryptionKey chiave simmetrica (16, 24 o 32 caratteri) usata per cifrare e decifrare
   * @throws IllegalStateException se la chiave non e' configurata o ha una lunghezza non valida
   */
  public EncryptorUtil(@Value("${greenbottle.security.encryption-key:}") String encryptionKey) {
    final byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
      throw new IllegalStateException(
              "Chiave di cifratura non valida: impostare GREENBOTTLE_SECURITY_ENCRYPTION_KEY "
                      + "(16, 24 o 32 caratteri).");
    }
    this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
  }

  /**
   * Metodo per cifrare una stringa.
   *
   * @param text stringa da cifrare
   * @return stringa cifrata, in Base64, con IV anteposto al testo cifrato
   */
  public String encrypt(String text) {
    try {
      final byte[] iv = new byte[IV_LENGTH_BYTES];
      secureRandom.nextBytes(iv);
      final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
      final byte[] encryptedBytes = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
      final byte[] payload = new byte[iv.length + encryptedBytes.length];
      System.arraycopy(iv, 0, payload, 0, iv.length);
      System.arraycopy(encryptedBytes, 0, payload, iv.length, encryptedBytes.length);
      return Base64.getEncoder().encodeToString(payload);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Errore durante la crittografia", e);
    }
  }

  /**
   * Metodo per decifrare una stringa.
   *
   * @param encryptedText stringa cifrata
   * @return stringa decifrata
   */
  public String decrypt(String encryptedText) {
    try {
      final byte[] payload = Base64.getDecoder().decode(encryptedText);
      final byte[] iv = Arrays.copyOfRange(payload, 0, IV_LENGTH_BYTES);
      final byte[] cipherText = Arrays.copyOfRange(payload, IV_LENGTH_BYTES, payload.length);
      final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
      final byte[] decryptedBytes = cipher.doFinal(cipherText);
      return new String(decryptedBytes, StandardCharsets.UTF_8);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Errore durante la decrittografia", e);
    }
  }
}
