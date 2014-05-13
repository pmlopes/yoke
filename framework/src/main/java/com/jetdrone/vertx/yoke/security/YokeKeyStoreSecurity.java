package com.jetdrone.vertx.yoke.security;

import org.jetbrains.annotations.NotNull;

import javax.crypto.*;
import java.security.*;
import java.security.cert.*;

public final class YokeKeyStoreSecurity extends YokeSecurity {

    private final KeyStore ks;

    public YokeKeyStoreSecurity(@NotNull KeyStore ks) {
        this.ks = ks;
    }

    @Override
    public Cipher getCipher(@NotNull Key secretKey, int mode) {
        try {
            Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
            cipher.init(mode, secretKey);

            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mac getMac(@NotNull String alias, @NotNull String keyPassword) {
        try {
            final Key secretKey = getKey(alias, keyPassword);

            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);

            return mac;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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

    @Override
    public Key getKey(@NotNull String alias, @NotNull String keyPassword) {
        try {
            return ks.getKey(alias, keyPassword.toCharArray());
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
