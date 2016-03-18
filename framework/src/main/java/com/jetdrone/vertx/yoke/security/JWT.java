package com.jetdrone.vertx.yoke.security;

import com.jetdrone.vertx.yoke.YokeSecurity;
import io.vertx.core.json.JsonObject;

import javax.crypto.Mac;
import java.security.Signature;
import java.security.SignatureException;
import java.util.*;

public final class JWT {

    private interface Crypto {
        byte[] sign(byte[] payload);
        boolean verify(byte[] signature, byte[] payload);
    }

    private static final class CryptoMac implements Crypto {
        private final Mac mac;

        private CryptoMac(final Mac mac) {
            this.mac = mac;
        }

        @Override
        public byte[] sign(byte[] payload) {
            synchronized (mac) {
                return mac.doFinal(payload);
            }
        }

        @Override
        public boolean verify(byte[] signature, byte[] payload) {
            synchronized (mac) {
                return Arrays.equals(signature, mac.doFinal(payload));
            }
        }
    }

    private static final class CryptoSignature implements Crypto {
        private final Signature sig;

        private CryptoSignature(final Signature signature) {
            this.sig = signature;
        }

        @Override
        public byte[] sign(byte[] payload) {
            try {
                synchronized (sig) {
                    sig.update(payload);
                    return sig.sign();
                }
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean verify(byte[] signature, byte[] payload) {
            try {
                synchronized (sig) {
                    sig.update(payload);
                    return Arrays.equals(signature, sig.sign());
                }
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Map<String, Crypto> CRYPTO_MAP;

    public JWT(final YokeSecurity security) {

        Map<String, Crypto> tmp = new HashMap<>();
        try {
            tmp.put("HS256", new CryptoMac(security.getMac("HS256")));
        } catch (RuntimeException e) {
            // Algorithm not supported
        }
        try {
            tmp.put("HS384", new CryptoMac(security.getMac("HS384")));
        } catch (RuntimeException e) {
            // Algorithm not supported
        }
        try {
            tmp.put("HS512", new CryptoMac(security.getMac("HS512")));
        } catch (RuntimeException e) {
            // Algorithm not supported
        }
        try {
            tmp.put("RS256", new CryptoSignature(security.getSignature("RS256")));
        } catch (RuntimeException e) {
            // Algorithm not supported
        }

        CRYPTO_MAP = Collections.unmodifiableMap(tmp);
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
        JsonObject header = new JsonObject(new String(Base64.getUrlDecoder().decode(headerSeg)));
        JsonObject payload = new JsonObject(new String(Base64.getUrlDecoder().decode(payloadSeg)));

        if (!noVerify) {
            Crypto crypto = CRYPTO_MAP.get(header.getString("alg"));

            if (crypto == null) {
                throw new RuntimeException("Algorithm not supported");
            }

            // verify signature. `sign` will return base64 string.
            String signingInput = headerSeg + "." + payloadSeg;

            if (!crypto.verify(Base64.getUrlDecoder().decode(signatureSeg), signingInput.getBytes())) {
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
                .put("typ", "JWT")
                .put("alg", algorithm);


        // create segments, all segment should be base64 string
        String headerSegment = Base64.getUrlEncoder().encodeToString(header.encode().getBytes());
        String payloadSegment = Base64.getUrlEncoder().encodeToString(payload.encode().getBytes());
        String signingInput = headerSegment + "." + payloadSegment;
        String signSegment = Base64.getUrlEncoder().encodeToString(crypto.sign(signingInput.getBytes()));

        return headerSegment + "." + payloadSegment + "." + signSegment;
    }
}
