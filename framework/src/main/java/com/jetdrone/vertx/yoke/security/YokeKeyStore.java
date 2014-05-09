package com.jetdrone.vertx.yoke.security;

import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.*;

public final class YokeKeyStore {

    private final KeyStore ks = getKeyStore();

    private static KeyStore getKeyStore() throws KeyStoreException {
        try {
            // try to load the Java Crypto Extensions
            return KeyStore.getInstance("jceks");
        } catch (KeyStoreException e0) {
            // fallback to default
            return KeyStore.getInstance(KeyStore.getDefaultType());
        }
    }

    public YokeKeyStore(@NotNull String fileName, @NotNull String keyStorePassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        try (InputStream in = getClass().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new FileNotFoundException(fileName);
            }

            ks.load(in, keyStorePassword.toCharArray());
        }
    }

    public SecretKey getSecretKey(@NotNull String alias, @NotNull String keyPassword) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return (SecretKey) ks.getKey(alias, keyPassword.toCharArray());
    }

    public Cipher getCipher(@NotNull String alias, @NotNull String keyPassword, int mode) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchPaddingException, InvalidKeyException {
        final SecretKey secretKey = getSecretKey(alias, keyPassword);

        Cipher c = Cipher.getInstance(secretKey.getAlgorithm());
        c.init(mode, secretKey);

        return c;
    }

    public Mac getMac(@NotNull String alias, @NotNull String keyPassword) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchPaddingException, InvalidKeyException {
        final SecretKey secretKey = getSecretKey(alias, keyPassword);

        Mac hmac = Mac.getInstance(secretKey.getAlgorithm());
        hmac.init(secretKey);

        return hmac;
    }

    public Signature getSignature(@NotNull String alias, @NotNull String keyPassword) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InvalidKeyException {
        final PrivateKey privateKey = (PrivateKey) getKey(alias, keyPassword);
        final X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);

        Signature instance = Signature.getInstance(certificate.getSigAlgName());
        instance.initSign(privateKey);

        return instance;
    }

    public Key getKey(@NotNull String alias, @NotNull String keyPassword) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return ks.getKey(alias, keyPassword.toCharArray());
    }
}
