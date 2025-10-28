package io.github.sanvew.tg.init.data;

import io.github.sanvew.tg.init.data.exception.TelegramInitDataException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.HexFormat;

class CryptoUtils {
    private static final HexFormat hexFormat = HexFormat.of();

    // =================================================================================================================
    // Hmac256 digest related methods
    // =================================================================================================================
    static boolean verifyHmac256Hash(final byte[] msg, final byte[] botToken, final byte[] hashKey, String hash) {
        final byte[] secretKey = hmacDigest(botToken, hashKey);
        final byte[] computedHash = hmacDigest(msg, secretKey);
        return hexFormat.formatHex(computedHash).equals(hash);
    }

    private static byte[] hmacDigest(byte[] data, byte[] key) {
        try {
            final Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            hmacSHA256.init(new SecretKeySpec(key, hmacSHA256.getAlgorithm()));
            return hmacSHA256.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new TelegramInitDataException(e);
        }
    }

    // =================================================================================================================
    // ed25519 signature related methods
    // =================================================================================================================
    static boolean verifyEd25519Signature(final byte[] msg, final byte[] sign, PublicKey pubKey) {
        try {
            final Signature signature = Signature.getInstance("Ed25519");
            signature.initVerify(pubKey);
            signature.update(msg);
            return signature.verify(sign);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new TelegramInitDataException(e);
        }
    }

    static PublicKey importEd25519PublicKey(String hexPubKey) {
        try {
            final KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
            final byte[] pubKey = hexFormat.parseHex(hexPubKey);
            final boolean xOdd = ((pubKey[pubKey.length - 1] & 0xFF) >> 7) == 1;
            littleEndianToBigEndianInPlace(pubKey);
            final EdECPoint edECPoint = new EdECPoint(xOdd, new BigInteger(pubKey));
            final KeySpec pubKeySpec = new EdECPublicKeySpec(NamedParameterSpec.ED25519, edECPoint);
            return keyFactory.generatePublic(pubKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new TelegramInitDataException(e);
        }
    }

    private static void littleEndianToBigEndianInPlace(byte[] byteArr) {
        reverseInPlace(byteArr);
        byteArr[0] &= 127;
    }

    private static void reverseInPlace(final byte[] byteArr) {
        if (byteArr == null || byteArr.length <= 1) {
            return;
        }

        int start = 0, end = byteArr.length - 1;
        while (start < end) {
            byte tmp = byteArr[start];
            byteArr[start++] = byteArr[end];
            byteArr[end--] = tmp;
        }
    }
}
