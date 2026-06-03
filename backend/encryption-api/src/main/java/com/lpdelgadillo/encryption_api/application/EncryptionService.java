package com.lpdelgadillo.encryption_api.application;

import com.lpdelgadillo.encryption_api.domain.model.EncryptionResult;
import com.lpdelgadillo.encryption_api.domain.port.in.EncryptionUseCase;
import com.lpdelgadillo.encryption_api.domain.port.out.EncryptionPort;
import org.springframework.stereotype.Service;

/**
 * CAPA DE APLICACIÓN — implementa los casos de uso.
 *
 * Conoce el dominio (usa EncryptionResult, EncryptionUseCase)
 * pero NO conoce AES ni HTTP.
 *
 * Usa EncryptionPort para delegar la encriptación real
 * al adaptador de infraestructura — sin saber cuál es.
 *
 * @Service → Spring lo detecta e inyecta automáticamente
 */
@Service
public class EncryptionService implements EncryptionUseCase {

    // Puerto de salida — inyectado por Spring
    // En runtime será AesEncryptionAdapter
    // En tests puede ser un mock — eso es la magia de hexagonal
    private final EncryptionPort encryptionPort;

    public EncryptionService(EncryptionPort encryptionPort) {
        this.encryptionPort = encryptionPort;
    }

    /**
     * Caso de uso: encriptar archivo.
     *
     * 1. Valida que los datos no estén vacíos
     * 2. Delega la encriptación real al puerto de salida
     * 3. Construye y retorna el resultado del dominio
     */
    @Override
    public EncryptionResult encrypt(byte[] fileBytes, String fileName, String secretKey) {

        // Validación del dominio — reglas de negocio puras
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("File content cannot be empty");
        }
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("Secret key cannot be empty");
        }
        if (secretKey.length() < 8) {
            throw new IllegalArgumentException("Secret key must be at least 8 characters");
        }

        // Delega al puerto de salida — no sabe cómo encripta
        byte[] encryptedData = encryptionPort.encrypt(fileBytes, secretKey);

        // Construye el resultado del dominio
        return new EncryptionResult(
                fileName + ".encrypted",
                encryptedData,
                "ENCRYPT",
                encryptedData.length
        );
    }

    /**
     * Caso de uso: desencriptar archivo.
     *
     * 1. Valida que los datos no estén vacíos
     * 2. Delega la desencriptación al puerto de salida
     * 3. Construye y retorna el resultado del dominio
     */
   @Override
public EncryptionResult decrypt(byte[] fileBytes, String fileName, String secretKey) {

    if (fileBytes == null || fileBytes.length == 0) {
        throw new IllegalArgumentException("File content cannot be empty");
    }
    if (secretKey == null || secretKey.isBlank()) {
        throw new IllegalArgumentException("Secret key cannot be empty");
    }

    // Recupera el nombre original removiendo .encrypted
    // archivo.pdf.encrypted → archivo.pdf
    // reporte.xlsx.encrypted → reporte.xlsx
    String originalFileName = fileName.endsWith(".encrypted")
            ? fileName.substring(0, fileName.length() - ".encrypted".length())
            : fileName;

    byte[] decryptedData = encryptionPort.decrypt(fileBytes, secretKey);

    return new EncryptionResult(
            originalFileName,
            decryptedData,
            "DECRYPT",
            decryptedData.length
    );
}
}