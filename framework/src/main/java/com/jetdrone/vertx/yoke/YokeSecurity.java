package com.jetdrone.vertx.yoke;

import com.jetdrone.vertx.yoke.util.Utils;
import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public final class YokeSecurity {

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

    private final KeyStore keyStore;
    private final Map<String, Key> keys;
    private final String UUID = java.util.UUID.randomUUID().toString();

    YokeSecurity(@NotNull final KeyStore keyStore, @NotNull final Map<String, Object> keyPasswords) {
        this.keyStore = keyStore;

        Map<String, Key> tmp = new HashMap<>();

        for (Map.Entry<String, Object> entry : keyPasswords.entrySet()) {
            try {
                if (keyStore.containsAlias(entry.getKey())) {
                    tmp.put(entry.getKey(), keyStore.getKey(entry.getKey(), ((String) entry.getValue()).toCharArray()));
                }
            } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
                throw new RuntimeException(e);
            }
        }

        keys = Collections.unmodifiableMap(tmp);
    }

    YokeSecurity(@NotNull final KeyStore keyStore, @NotNull final String keyPassword) {
        this.keyStore = keyStore;

        Map<String, Key> tmp = new HashMap<>();

        try {
            Enumeration<String> aliases = keyStore.aliases();

            while(aliases.hasMoreElements()) {
                String alias = aliases.nextElement();

                try {
                    tmp.put(alias, keyStore.getKey(alias, keyPassword.toCharArray()));
                } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        keys = Collections.unmodifiableMap(tmp);
    }

    YokeSecurity() {
        keyStore = null;
        keys = Collections.emptyMap();
    }

    /**
     * Creates a new Message Authentication Code
     * @param alias algorithm to use e.g.: HmacSHA256
     * @return Mac implementation
     */
    public Mac getMac(final @NotNull String alias) {
        try {
            final Key secretKey = keys.get(alias);

            Mac mac;

            if (secretKey == null) {
                mac = Mac.getInstance(getAlgorithm(alias));
                mac.init(new SecretKeySpec(UUID.getBytes(), mac.getAlgorithm()));
            } else {
                mac = Mac.getInstance(secretKey.getAlgorithm());
                mac.init(secretKey);
            }

            return mac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public Signature getSignature(final @NotNull String alias) {
        try {
            final PrivateKey privateKey = (PrivateKey) keys.get(alias);

            Signature signature;

            if (privateKey == null) {
                final KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

                signature = Signature.getInstance(getAlgorithm(alias));
                signature.initSign(keyPair.getPrivate());
            } else {
                final X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

                signature = Signature.getInstance(certificate.getSigAlgName());
                signature.initSign(privateKey);
            }

            return signature;
        } catch (NoSuchAlgorithmException | InvalidKeyException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new Crypto KEY
     * @return Key implementation
     */
    public Key getKey(final @NotNull String alias) {
        final Key key = keys.get(alias);

        if (key == null) {
            return new SecretKeySpec(UUID.getBytes(), getAlgorithm(alias));
        } else {
            return key;
        }
    }

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
