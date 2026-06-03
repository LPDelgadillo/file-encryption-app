package com.lpdelgadillo.encryption_api.domain.model;

/**
 * Entidad del dominio que representa el resultado
 * de una operación de encriptación o desencriptación.
 *
 * No conoce Spring, no conoce AES, no conoce HTTP.
 * Solo representa el dato puro del negocio.
 */
public class EncryptionResult {

    private final String fileName;
    private final byte[] processedData;
    private final String operation;
    private final long fileSizeBytes;

    public EncryptionResult(
            String fileName,
            byte[] processedData,
            String operation,
            long fileSizeBytes) {
        this.fileName      = fileName;
        this.processedData = processedData;
        this.operation     = operation;
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getFileName()      { return fileName; }
    public byte[] getProcessedData() { return processedData; }
    public String getOperation()     { return operation; }
    public long getFileSizeBytes()   { return fileSizeBytes; }
}