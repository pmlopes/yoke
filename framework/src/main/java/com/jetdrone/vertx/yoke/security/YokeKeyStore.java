package com.jetdrone.vertx.yoke.security;

import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.*;

public final class YokeKeyStore {

    private final KeyStore ks;

    public YokeKeyStore(@NotNull String storeType, @NotNull String fileName, @NotNull String keyStorePassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        ks = KeyStore.getInstance(storeType);

        try (InputStream in = getClass().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new FileNotFoundException(fileName);
            }

            ks.load(in, keyStorePassword.toCharArray());
        }
    }
    public YokeKeyStore(@NotNull String fileName, @NotNull String keyStorePassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        this(KeyStore.getDefaultType(), fileName, keyStorePassword);
    }

    public SecretKey getSecretKey(@NotNull String alias, @NotNull String keyPassword) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return (SecretKey) ks.getKey(alias, keyPassword.toCharArray());
    }

    public Cipher getCipher(@NotNull String alias, @NotNull String keyPassword, int mode) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchPaddingException, InvalidKeyException {
        final SecretKey secretKey = getSecretKey(alias, keyPassword);

        Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
        cipher.init(mode, secretKey);

        return cipher;
    }

    public Mac getMac(@NotNull String alias, @NotNull String keyPassword) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, NoSuchPaddingException, InvalidKeyException {
        final SecretKey secretKey = getSecretKey(alias, keyPassword);

        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);

        return mac;
    }

    public Signature getSignature(@NotNull String alias, @NotNull String keyPassword) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InvalidKeyException {
        final PrivateKey privateKey = (PrivateKey) getKey(alias, keyPassword);
        final X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);

        Signature signature = Signature.getInstance(certificate.getSigAlgName());
        signature.initSign(privateKey);

        return signature;
    }

    public Key getKey(@NotNull String alias, @NotNull String keyPassword) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        return ks.getKey(alias, keyPassword.toCharArray());
    }
}
