package com.lpdelgadillo.encryption_api.infrastructure.adapter.out;

import com.lpdelgadillo.encryption_api.domain.port.out.EncryptionPort;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

/**
 * ADAPTADOR DE SALIDA — implementa EncryptionPort con AES-256-GCM.
 *
 * Esta clase es la ÚNICA que sabe sobre AES en toda la aplicación.
 * Si mañana cambias a RSA o ChaCha20, solo tocas este archivo.
 *
 * El dominio y la aplicación no se enteran del cambio.
 *
 * Algoritmo: AES-256-GCM con PBKDF2 para derivar la clave
 * ─────────────────────────────────────────────────────────
 * PBKDF2: convierte la contraseña del usuario en una clave
 *         de 256 bits segura usando 65536 iteraciones + salt
 * GCM:    encripta Y autentica — detecta si el archivo
 *         fue modificado después de encriptarlo
 */
@Component
public class AesEncryptionAdapter implements EncryptionPort {

    // Constantes del algoritmo — estándar de la industria
    private static final String ALGORITHM        = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM    = "PBKDF2WithHmacSHA256";
    private static final int    GCM_TAG_LENGTH   = 128;  // bits
    private static final int    GCM_IV_LENGTH    = 12;   // bytes
    private static final int    SALT_LENGTH      = 16;   // bytes
    private static final int    KEY_LENGTH       = 256;  // bits
    private static final int    ITERATIONS       = 65536;

    /**
     * Encripta bytes con AES-256-GCM.
     *
     * Formato del resultado:
     * [ SALT (16 bytes) | IV (12 bytes) | DATOS ENCRIPTADOS ]
     *
     * Salt e IV se guardan junto al archivo encriptado
     * porque se necesitan para desencriptar.
     * No son secretos — el secreto es la contraseña.
     */
    @Override
    public byte[] encrypt(byte[] data, String secretKey) {
        try {
            // 1. Genera salt aleatorio — único por cada encriptación
            byte[] salt = generateRandom(SALT_LENGTH);

            // 2. Genera IV aleatorio — único por cada encriptación
            byte[] iv = generateRandom(GCM_IV_LENGTH);

            // 3. Deriva clave de 256 bits desde la contraseña
            SecretKey key = deriveKey(secretKey, salt);

            // 4. Inicializa el cipher en modo ENCRYPT
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            // 5. Encripta los datos
            byte[] encryptedData = cipher.doFinal(data);

            // 6. Empaqueta: salt + iv + datos encriptados
            // ByteBuffer facilita concatenar arrays de bytes
            ByteBuffer byteBuffer = ByteBuffer.allocate(
                    SALT_LENGTH + GCM_IV_LENGTH + encryptedData.length
            );
            byteBuffer.put(salt);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);

            return byteBuffer.array();

        } catch (Exception e) {
            throw new RuntimeException("Error encrypting file: " + e.getMessage(), e);
        }
    }

    /**
     * Desencripta bytes previamente encriptados con este adaptador.
     *
     * Extrae salt e IV del inicio del archivo,
     * deriva la misma clave con la contraseña,
     * y desencripta verificando la autenticidad GCM.
     */
    @Override
    public byte[] decrypt(byte[] data, String secretKey) {
        try {
            // 1. Desempaqueta: extrae salt, iv y datos encriptados
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);

            byte[] salt = new byte[SALT_LENGTH];
            byteBuffer.get(salt);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);

            // 2. Deriva la misma clave usando la contraseña + salt
            SecretKey key = deriveKey(secretKey, salt);

            // 3. Inicializa el cipher en modo DECRYPT
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            // 4. Desencripta — GCM verifica autenticidad automáticamente
            // Si la contraseña es incorrecta o el archivo fue alterado
            // lanza AEADBadTagException
            return cipher.doFinal(encryptedData);

        } catch (javax.crypto.AEADBadTagException e) {
            // Error específico de GCM — contraseña incorrecta o archivo corrupto
            throw new RuntimeException("Invalid key or corrupted file", e);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting file: " + e.getMessage(), e);
        }
    }

    /**
     * Deriva una clave AES-256 desde una contraseña usando PBKDF2.
     *
     * PBKDF2 hace la contraseña resistente a ataques de fuerza bruta
     * ejecutando 65536 iteraciones de hash — lento a propósito.
     */
    private SecretKey deriveKey(String secretKey, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        KeySpec spec = new PBEKeySpec(
                secretKey.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Genera bytes aleatorios criptográficamente seguros.
     * SecureRandom usa fuentes de entropía del sistema operativo.
     */
    private byte[] generateRandom(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}