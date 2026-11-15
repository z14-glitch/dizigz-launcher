package com.dizigz.launcher.core;

import java.util.regex.Pattern;

/** Числовое сравнение id релизов: работает и для 1.8.9, и для новой схемы вида 26.1.2. */
public final class McVersion {
    private static final Pattern RELEASE = Pattern.compile("^\\d+(\\.\\d+){1,2}$");

    private McVersion() {}

    public static boolean isReleaseId(String id) { return id != null && RELEASE.matcher(id).matches(); }

    public static int compare(String a, String b) {
        int[] x = parts(a), y = parts(b);
        for (int k = 0; k < 3; k++) if (x[k] != y[k]) return Integer.compare(x[k], y[k]);
        return 0;
    }

    public static boolean atLeast(String id, String min) { return compare(id, min) >= 0; }

    private static int[] parts(String id) {
        if (!isReleaseId(id)) throw new IllegalArgumentException("Не релизная версия: " + id);
        String[] p = id.split("\\.");
        int[] r = new int[3];
        for (int k = 0; k < p.length; k++) r[k] = Integer.parseInt(p[k]);
        return r;
    }
}
