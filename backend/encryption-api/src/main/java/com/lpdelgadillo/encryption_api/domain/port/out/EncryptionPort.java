package com.lpdelgadillo.encryption_api.domain.port.out;

/**
 * PUERTO DE SALIDA — define QUÉ necesita el dominio del exterior.
 *
 * El dominio dice "necesito algo que sepa encriptar bytes"
 * pero NO sabe si es AES, RSA u otro algoritmo.
 * Eso lo decide la infraestructura.
 */
public interface EncryptionPort {

    /**
     * Encripta bytes usando el algoritmo de infraestructura.
     *
     * @param data       bytes a encriptar
     * @param secretKey  clave de encriptación
     * @return           bytes encriptados
     */
    byte[] encrypt(byte[] data, String secretKey);

    /**
     * Desencripta bytes usando el algoritmo de infraestructura.
     *
     * @param data       bytes encriptados
     * @param secretKey  clave usada para encriptar
     * @return           bytes originales
     */
    byte[] decrypt(byte[] data, String secretKey);
}