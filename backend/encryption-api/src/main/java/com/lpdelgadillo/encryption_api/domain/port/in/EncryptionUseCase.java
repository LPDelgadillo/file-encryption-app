package com.lpdelgadillo.encryption_api.domain.port.in;

import com.lpdelgadillo.encryption_api.domain.model.EncryptionResult;

/**
 * PUERTO DE ENTRADA — define QUÉ puede hacer la aplicación.
 *
 * El Controller llama esta interfaz sin saber
 * cómo está implementada por debajo.
 */
public interface EncryptionUseCase {

    /**
     * Encripta los bytes de un archivo con AES-256-GCM.
     *
     * @param fileBytes  contenido del archivo en bytes
     * @param fileName   nombre original del archivo
     * @param secretKey  clave ingresada por el usuario
     * @return           EncryptionResult con bytes encriptados
     */
    EncryptionResult encrypt(byte[] fileBytes, String fileName, String secretKey);

    /**
     * Desencripta los bytes de un archivo previamente encriptado.
     *
     * @param fileBytes  contenido encriptado
     * @param fileName   nombre del archivo
     * @param secretKey  clave usada para encriptar
     * @return           EncryptionResult con bytes originales
     */
    EncryptionResult decrypt(byte[] fileBytes, String fileName, String secretKey);
}