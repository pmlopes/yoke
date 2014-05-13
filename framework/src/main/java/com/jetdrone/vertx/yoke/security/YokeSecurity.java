package com.jetdrone.vertx.yoke.security;

import com.jetdrone.vertx.yoke.util.Utils;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.util.HashMap;
import java.util.Map;

public class YokeSecurity {

    private static final Map<String, String> ALIAS_ALG_MAP = new HashMap<>();

    static {
        ALIAS_ALG_MAP.put("HS256", "HMacSHA256");
        ALIAS_ALG_MAP.put("HS384", "HMacSHA384");
        ALIAS_ALG_MAP.put("HS512", "HMacSHA512");
        ALIAS_ALG_MAP.put("RS256", "SHA256withRSA");
    }

    private static String getAlgorithm(String alias) {
        if (ALIAS_ALG_MAP.containsKey(alias)) {
            return ALIAS_ALG_MAP.get(alias);
        } else {
            return alias;
        }
    }

    /**
     * Creates a new Message Authentication Code
     * @param alias algorithm to use e.g.: HmacSHA256
     * @param secret The secret key used to create signatures
     * @return Mac implementation
     */
    public Mac getMac(final @NotNull String alias, final @NotNull String secret) {
        try {
            Mac hmac = Mac.getInstance(getAlgorithm(alias));
            hmac.init(new SecretKeySpec(secret.getBytes(), hmac.getAlgorithm()));
            return hmac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public Signature getSignature(final @NotNull String alias, final String secret) {
        try {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();

            Signature instance = Signature.getInstance(getAlgorithm(alias));
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
    public Key getKey(final @NotNull String alias, final @NotNull String secret) {
        return new SecretKeySpec(secret.getBytes(), getAlgorithm(alias));
    }

    /**
     * Creates a new Cipher
     * @return Cipher implementation
     */
    public Cipher getCipher(final @NotNull Key key, int mode) {
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
