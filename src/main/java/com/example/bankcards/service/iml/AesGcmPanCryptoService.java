package com.example.bankcards.service.iml;

import com.example.bankcards.service.IPanCrypto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AesGcmPanCryptoService implements IPanCrypto {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;
    private static final int TAG_LEN_BITS = 128;
    private static final byte VERSION_1 = 1;

    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmPanCryptoService(@Value("${crypto.pan.key-base64}") String keyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);

        if (!(keyBytes.length == 16 || keyBytes.length == 24 || keyBytes.length == 32)) {
            throw new IllegalArgumentException("PAN crypto key must be 16/24/32 bytes (base64-decoded)");
        }

        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public byte[] encrypt(String pan) {
        if (pan == null) throw new IllegalArgumentException("PAN is null");
        String normalized = pan.replaceAll("\\s+", "");
        if (!normalized.matches("\\d{13,19}")) { // реальный PAN 13..19
            throw new IllegalArgumentException("PAN must be 13..19 digits");
        }

        byte[] iv = new byte[IV_LEN];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LEN_BITS, iv));

            cipher.updateAAD(new byte[]{VERSION_1});

            byte[] ciphertext = cipher.doFinal(normalized.getBytes(StandardCharsets.UTF_8));

            ByteBuffer bb = ByteBuffer.allocate(1 + IV_LEN + ciphertext.length);
            bb.put(VERSION_1);
            bb.put(iv);
            bb.put(ciphertext);
            return bb.array();

        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt PAN", e);
        }
    }

    @Override
    public String decrypt(byte[] panEnc) {
        if (panEnc == null || panEnc.length < 1 + IV_LEN + 16) {
            throw new IllegalArgumentException("Invalid encrypted PAN payload");
        }

        try {
            ByteBuffer bb = ByteBuffer.wrap(panEnc);

            byte version = bb.get();
            if (version != VERSION_1) {
                throw new IllegalArgumentException("Unsupported PAN encryption version: " + version);
            }

            byte[] iv = new byte[IV_LEN];
            bb.get(iv);

            byte[] ciphertext = new byte[bb.remaining()];
            bb.get(ciphertext);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN_BITS, iv));
            cipher.updateAAD(new byte[]{version});

            byte[] plain = cipher.doFinal(ciphertext);
            return new String(plain, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt PAN", e);
        }
    }
}
