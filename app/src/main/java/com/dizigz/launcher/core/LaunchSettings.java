package com.dizigz.launcher.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Настройки памяти и графики с подбором под слабые телефоны. */
public final class LaunchSettings {
    public enum Graphics { LOW, MEDIUM, HIGH }

    public static final int MIN_RAM_MB = 512;
    public static final int MAX_RAM_MB = 4096;
    public static final int STEP_MB = 128;

    public int ramMb = 1024;
    public Graphics graphics = Graphics.LOW;

    /** Оставляем Android ~1.5 ГБ и не берём больше половины всей памяти. */
    public static int recommendedRamMb(long deviceTotalMb) {
        long v = Math.min(deviceTotalMb / 2, deviceTotalMb - 1536);
        return clampRam((int) v);
    }

    public static boolean deviceTooWeak(long deviceTotalMb) { return deviceTotalMb < 2048; }

    public static int clampRam(int mb) {
        int snapped = Math.round(mb / (float) STEP_MB) * STEP_MB;
        return Math.max(MIN_RAM_MB, Math.min(MAX_RAM_MB, snapped));
    }

    public List<String> jvmArgs() {
        List<String> a = new ArrayList<>();
        int xmx = clampRam(ramMb);
        a.add("-Xms" + Math.max(256, xmx / 2) + "M");
        a.add("-Xmx" + xmx + "M");
        if (xmx <= 2048) {
            a.add("-XX:+UseSerialGC");
        } else {
            a.add("-XX:+UseG1GC");
            a.add("-XX:MaxGCPauseMillis=50");
        }
        return a;
    }

    /** Значения для options.txt. Набор ключей зависит от версии игры — проверять при запуске. */
    public Map<String, String> optionsTxt() {
        Map<String, String> o = new LinkedHashMap<>();
        switch (graphics) {
            case LOW:
                o.put("renderDistance", "4"); o.put("maxFps", "30"); o.put("particles", "2");
                o.put("entityShadows", "false"); o.put("graphicsMode", "0");
                o.put("biomeBlendRadius", "0"); o.put("mipmapLevels", "0"); o.put("entityDistanceScaling", "0.5");
                break;
            case MEDIUM:
                o.put("renderDistance", "8"); o.put("maxFps", "60"); o.put("particles", "1");
                o.put("entityShadows", "false"); o.put("graphicsMode", "1");
                o.put("biomeBlendRadius", "1"); o.put("mipmapLevels", "2"); o.put("entityDistanceScaling", "0.75");
                break;
            default:
                o.put("renderDistance", "12"); o.put("maxFps", "120"); o.put("particles", "0");
                o.put("entityShadows", "true"); o.put("graphicsMode", "1");
                o.put("biomeBlendRadius", "2"); o.put("mipmapLevels", "4"); o.put("entityDistanceScaling", "1.0");
        }
        return o;
    }
        }
