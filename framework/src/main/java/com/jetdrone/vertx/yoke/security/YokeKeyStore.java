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
    private final String keyStoreFileName;

    public YokeKeyStore(@NotNull String storeType, @NotNull String fileName, @NotNull String keyStorePassword) {
        try {
            ks = KeyStore.getInstance(storeType);
            keyStoreFileName = fileName;

            try (InputStream in = getClass().getResourceAsStream(fileName)) {
                if (in == null) {
                    throw new FileNotFoundException(fileName);
                }

                ks.load(in, keyStorePassword.toCharArray());
            }
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String keyStoreFileName() {
        return keyStoreFileName;
    }

    public SecretKey getSecretKey(@NotNull String alias, @NotNull String keyPassword) {
        try {
            return (SecretKey) ks.getKey(alias, keyPassword.toCharArray());
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public Cipher getCipher(@NotNull String alias, @NotNull String keyPassword, int mode) {
        try {
            final SecretKey secretKey = getSecretKey(alias, keyPassword);

            Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
            cipher.init(mode, secretKey);

            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public Mac getMac(@NotNull String alias, @NotNull String keyPassword) {
        try {
            final SecretKey secretKey = getSecretKey(alias, keyPassword);

            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);

            return mac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public Signature getSignature(@NotNull String alias, @NotNull String keyPassword) {
        try {
            final PrivateKey privateKey = (PrivateKey) getKey(alias, keyPassword);
            final X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);

            Signature signature = Signature.getInstance(certificate.getSigAlgName());
            signature.initSign(privateKey);

            return signature;
        } catch (NoSuchAlgorithmException | InvalidKeyException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public Key getKey(@NotNull String alias, @NotNull String keyPassword) {
        try {
            return ks.getKey(alias, keyPassword.toCharArray());
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
