package com.taobao.diamond.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class AESUtils {

    private static final String KEY_ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    public static String encrypt(String content, String encryptKey, String charsetName) throws Exception {
        byte[] key = Base64.decodeBase64(encryptKey.getBytes(charsetName));
        byte[] inputData = content.getBytes(charsetName);
        byte[] outputData = encrypt(inputData, key);
        return new String(Base64.encodeBase64(outputData), charsetName);
    }

    public static String decrypt(String content, String encryptKey, String charsetName) throws Exception {
        byte[] key = Base64.decodeBase64(encryptKey.getBytes(charsetName));
        byte[] contentBytes = Base64.decodeBase64(content.getBytes(charsetName));
        byte[] outputData = decrypt(contentBytes, key);
        return new String(outputData, charsetName);
    }

    private static Key toKey(byte[] key) {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    private static byte[] decrypt(byte[] data, byte[] key)
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
        IllegalBlockSizeException {
        Key k = toKey(key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, k);
        return cipher.doFinal(data);
    }

    private static byte[] encrypt(byte[] data, byte[] key)
        throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
        IllegalBlockSizeException {
        Key k = toKey(key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, k);
        return cipher.doFinal(data);
    }

}