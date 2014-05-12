package com.jetdrone.vertx.yoke.util;

import com.jetdrone.vertx.yoke.security.YokeKeyStore;
import com.jetdrone.vertx.yoke.security.YokeSecurity;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Base64;

import javax.crypto.Mac;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class JWT {

    private interface Crypto {
        byte[] sign(byte[] payload);
        boolean verify(byte[] signature, byte[] payload);
    }

    private final class CryptoMac implements Crypto {
        final Mac mac;

        private CryptoMac(final Mac mac) {
            this.mac = mac;
        }

        @Override
        public byte[] sign(byte[] payload) {
            return mac.doFinal(payload);
        }

        @Override
        public boolean verify(byte[] signature, byte[] payload) {
            return Arrays.equals(signature, mac.doFinal(payload));
        }
    }

    private final class CryptoSignature implements Crypto {
        final Signature sig;

        private CryptoSignature(final Signature signature) {
            this.sig = signature;
        }

        @Override
        public byte[] sign(byte[] payload) {
            try {
                sig.update(payload);
                return sig.sign();
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean verify(byte[] signature, byte[] payload) {
            try {
                sig.update(payload);
                return sig.verify(signature);
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Map<String, Crypto> CRYPTO_MAP = new HashMap<>();

    public JWT(final String secret) {
        CRYPTO_MAP.put("HS256", new CryptoMac(YokeSecurity.newMac("HmacSHA256", secret)));
        CRYPTO_MAP.put("HS384", new CryptoMac(YokeSecurity.newMac("HmacSHA384", secret)));
        CRYPTO_MAP.put("HS512", new CryptoMac(YokeSecurity.newMac("HmacSHA512", secret)));
        CRYPTO_MAP.put("RS256", new CryptoSignature(YokeSecurity.newSignature("SHA256withRSA")));
    }

    public JWT(final YokeKeyStore keystore, final String keyPassword) {
        CRYPTO_MAP.put("HS256", new CryptoMac(keystore.getMac("HS256", keyPassword)));
        CRYPTO_MAP.put("HS384", new CryptoMac(keystore.getMac("HS384", keyPassword)));
        CRYPTO_MAP.put("HS512", new CryptoMac(keystore.getMac("HS512", keyPassword)));
        CRYPTO_MAP.put("RS256", new CryptoSignature(keystore.getSignature("RS256", keyPassword)));
    }

    public JsonObject decode(final String token) {
        return decode(token, false);
    }

    public JsonObject decode(final String token, boolean noVerify) {
        String[] segments = token.split("\\.");
        if (segments.length != 3) {
            throw new RuntimeException("Not enough or too many segments");
        }

        // All segment should be base64
        String headerSeg = segments[0];
        String payloadSeg = segments[1];
        String signatureSeg = segments[2];

        // base64 decode and parse JSON
        JsonObject header = new JsonObject(base64urlDecode(headerSeg));
        JsonObject payload = new JsonObject(base64urlDecode(payloadSeg));

        if (!noVerify) {
            Crypto crypto = CRYPTO_MAP.get(header.getString("alg"));

            if (crypto == null) {
                throw new RuntimeException("Algorithm not supported");
            }

            // verify signature. `sign` will return base64 string.
            String signingInput = headerSeg + "." + payloadSeg;

            if (!crypto.verify(Base64.decode(base64urlUnescape(signatureSeg), Base64.DONT_BREAK_LINES), signingInput.getBytes())) {
                throw new RuntimeException("Signature verification failed");
            }
        }

        return payload;
    }

    public String encode(JsonObject payload) {
        return encode(payload, "HS256");
    }

    public String encode(JsonObject payload, String algorithm) {
        Crypto crypto = CRYPTO_MAP.get(algorithm);

        if (crypto == null) {
            throw new RuntimeException("Algorithm not supported");
        }

        // header, typ is fixed value.
        JsonObject header = new JsonObject()
                .putString("typ", "JWT")
                .putString("alg", algorithm);


        // create segments, all segment should be base64 string
        String headerSegment = base64urlEncode(header.encode());
        String payloadSegment = base64urlEncode(payload.encode());
        String signingInput = headerSegment + "." + payloadSegment;
        String signSegment = base64urlEscape(Base64.encodeBytes(crypto.sign(signingInput.getBytes()), Base64.DONT_BREAK_LINES));

        return headerSegment + "." + payloadSegment + "." + signSegment;
    }

    private static String base64urlDecode(String str) {
        return new String(Base64.decode(base64urlUnescape(str), Base64.DONT_BREAK_LINES));
    }

    private static String base64urlUnescape(String str) {
        int padding = 5 - str.length() % 4;
        StringBuilder sb = new StringBuilder(str.length() + padding);
        sb.append(str);
        for (int i = 0; i < padding; i++) {
            sb.append('=');
        }
        return sb.toString().replaceAll("\\-", "+").replaceAll("_", "/");
    }

    private static String base64urlEncode(String str) {
        return base64urlEscape(Base64.encodeBytes(str.getBytes(), Base64.DONT_BREAK_LINES));
    }

    private static String base64urlEscape(String str) {
        return str.replaceAll("\\+", "-").replaceAll("/", "_").replaceAll("=", "");
    }
}
