package com.example.bankcards.service;

/**
 * Интерфейс для шифрования и расшифровки номера банковской карты.
 */
public interface IPanCrypto {

    /**
     * Шифрует номер карты.
     *
     * @param pan номер карты в открытом виде
     * @return номер карты в зашифрованном виде
     */
    byte[] encrypt(String pan);

    /**
     * Расшифровывает номер карты.
     *
     * @param panEnc зашифрованный номер карты
     * @return номер карты в открытом виде
     */
    String decrypt(byte[] panEnc);
}
