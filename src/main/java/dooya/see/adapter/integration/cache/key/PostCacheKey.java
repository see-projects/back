package dooya.see.adapter.integration.cache.key;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class PostCacheKey {
    private static final String PREFIX = "post";
    private static final HexFormat HEX = HexFormat.of();
    private static final String HASH_ALGORITHM = "MD5";

    private PostCacheKey() {}

    public static String detail(Long postId) {
        return PREFIX + ":detail:" + requirePositive(postId);
    }

    public static String publicList(int page, int size) {
        return PREFIX + ":list:public:" + page + ":" + size;
    }

    public static String categoryList(String category, int page, int size) {
        return PREFIX + ":list:category:" + normalize(category) + ":" + page + ":" + size;
    }

    public static String search(String normalizedQuery, int page, int size) {
        String hash = hashToHex(normalizedQuery);
        return PREFIX + ":search:" + hash + ":" + page + ":" + size;
    }

    public static String stats(Long postId) {
        return PREFIX + ":stats:" + requirePositive(postId);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "null";
        }
        return value.trim().toUpperCase();
    }

    private static long requirePositive(Long id) {
        Objects.requireNonNull(id, "postId must not be null");
        if (id <= 0) {
            throw new IllegalArgumentException("postId must be positive");
        }
        return id;
    }

    private static String hashToHex(String input) {
        if (input == null || input.isBlank()) {
            return "empty";
        }
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }
}
