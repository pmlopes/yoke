package com.jetdrone.vertx.yoke.security;

import com.jetdrone.vertx.yoke.util.Utils;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.*;

public final class YokeSecurity {

    /**
     * Creates a new Message Authentication Code
     * @param algorithm algorithm to use e.g.: HmacSHA256
     * @param secret The secret key used to create signatures
     * @return Mac implementation
     */
    public static Mac newMac(final @NotNull String algorithm, final @NotNull String secret) {
        try {
            Mac hmac = Mac.getInstance(algorithm);
            hmac.init(new SecretKeySpec(secret.getBytes(), hmac.getAlgorithm()));
            return hmac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static Signature newSignature(final @NotNull String algorithm) {
        try {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();

            Signature instance = Signature.getInstance(algorithm);
            instance.initSign(privateKey);

            return instance;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new Crypto KEY
     * @param secret The secret key used to create signatures
     * @return Key implementation
     */
    public static Key newKey(final @NotNull String algorithm, final @NotNull String secret) {
        return new SecretKeySpec(secret.getBytes(), algorithm);
    }

    /**
     * Creates a new Cipher
     * @return Cipher implementation
     */
    public static Cipher newCipher(final @NotNull Key key, int mode) {
        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(mode, key);
            return cipher;
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Signs a String value with a given MAC
     */
    public static String sign(@NotNull String val, @NotNull Mac mac) {
        mac.reset();
        return val + "." + Utils.base64(mac.doFinal(val.getBytes()));
    }

    /**
     * Returns the original value is the signature is correct. Null otherwise.
     */
    public static String unsign(@NotNull String val, @NotNull Mac mac) {
        int idx = val.lastIndexOf('.');

        if (idx == -1) {
            return null;
        }

        String str = val.substring(0, idx);
        if (val.equals(sign(str, mac))) {
            return str;
        }
        return null;
    }

    public static String encrypt(@NotNull String val, @NotNull Cipher cipher) {
        try {
            byte[] encVal = cipher.doFinal(val.getBytes());
            return Utils.base64(encVal);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(@NotNull String val, @NotNull Cipher cipher) {
        try {
            byte[] decordedValue = DatatypeConverter.parseBase64Binary(val);
            byte[] decValue = cipher.doFinal(decordedValue);
            return new String(decValue);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

}
