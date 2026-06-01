package com.upsjb.act2.util;

import java.util.Locale;

public final class TextUtil {

    private TextUtil() {
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public static String upperTrimToNull(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String joinNames(String... values) {
        StringBuilder builder = new StringBuilder();

        if (values == null) {
            return "";
        }

        for (String value : values) {
            String clean = trimToNull(value);
            if (clean != null) {
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(clean);
            }
        }

        return builder.toString();
    }
}