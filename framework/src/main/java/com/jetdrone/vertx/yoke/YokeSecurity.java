package com.jetdrone.vertx.yoke;

import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import java.security.*;
import java.util.Base64;

public abstract class YokeSecurity {

    protected YokeSecurity() {}

    /**
     * Creates a new Message Authentication Code
     * @param alias algorithm to use e.g.: HmacSHA256
     * @return Mac implementation
     */
    public abstract Mac getMac(final @NotNull String alias);

    public abstract Signature getSignature(final @NotNull String alias);

    /**
     * Creates a new Crypto KEY
     * @return Key implementation
     */
    public abstract Key getKey(final @NotNull String alias);

    /**
     * Creates a new Cipher
     * @return Cipher implementation
     */
    public static Cipher getCipher(final @NotNull Key key, int mode) {
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
        return val + "." + Base64.getEncoder().encodeToString(val.getBytes());
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
            return Base64.getEncoder().encodeToString(encVal);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(@NotNull String val, @NotNull Cipher cipher) {
        try {
            byte[] decordedValue = Base64.getDecoder().decode(val);
            byte[] decValue = cipher.doFinal(decordedValue);
            return new String(decValue);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}
