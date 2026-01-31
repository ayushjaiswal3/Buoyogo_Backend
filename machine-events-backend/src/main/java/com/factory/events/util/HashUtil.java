package com.factory.events.util;

import com.factory.events.dto.EventRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    public static String payloadHash(EventRequest e) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String payload =
                    e.getEventId() + "|" +
                            e.getEventTime() + "|" +
                            e.getMachineId() + "|" +
                            e.getFactoryId() + "|" +
                            e.getLineId() + "|" +
                            e.getDurationMs() + "|" +
                            e.getDefectCount();

            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("HASH_ERROR", ex);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
