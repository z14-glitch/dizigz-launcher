package com.dizigz.launcher.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Предварительная проверка совместимости «версия + загрузчик + оптимизатор».
 * ВАЖНО: это эвристика по известным правилам. Реальная доступность сборок
 * (Forge/NeoForge/Fabric/OptiFine) должна подтверждаться метаданными самих загрузчиков при установке.
 */
public final class Compat {
    public enum Loader { VANILLA, FABRIC, FORGE, NEOFORGE }
    public enum Optimizer { NONE, OPTIFINE, SODIUM }

    public static final class Result {
        public final List<String> errors = new ArrayList<>();
        public final List<String> warnings = new ArrayList<>();
        public boolean ok() { return errors.isEmpty(); }
    }

    private Compat() {}

    public static Result check(Loader loader, Optimizer opt, String mcVersion) {
        Result r = new Result();
        if (!McVersion.isReleaseId(mcVersion)) {
            r.errors.add("Пока поддерживаются только релизные версии (получено: " + mcVersion + ").");
            return r;
        }
        switch (loader) {
            case FABRIC:
                if (!McVersion.atLeast(mcVersion, "1.14")) r.errors.add("Fabric поддерживает версии от 1.14.");
                break;
            case FORGE:
                if (!McVersion.atLeast(mcVersion, "1.7.10")) r.errors.add("Forge в лаунчере доступен от 1.7.10.");
                break;
            case NEOFORGE:
                if (!McVersion.atLeast(mcVersion, "1.20.1")) r.errors.add("NeoForge существует только для 1.20.1 и новее.");
                else if (McVersion.compare(mcVersion, "1.20.1") == 0)
                    r.warnings.add("Для 1.20.1 NeoForge — ранние сборки; основная линия начинается с 1.20.2.");
                break;
            default: break;
        }
        if (opt == Optimizer.OPTIFINE) {
            if (!McVersion.atLeast(mcVersion, "1.7.10")) r.errors.add("OptiFine в лаунчере доступен от 1.7.10.");
            if (loader == Loader.FABRIC)
                r.errors.add("OptiFine не работает с Fabric нативно. Для Fabric используйте Sodium.");
            if (loader == Loader.NEOFORGE)
                r.errors.add("Официальной поддержки OptiFine на NeoForge нет. Используйте Sodium.");
            if (loader == Loader.FORGE)
                r.warnings.add("OptiFine + Forge работает не для всех версий — проверяется при установке.");
            if (loader == Loader.VANILLA)
                r.warnings.add("Наличие OptiFine для этой версии проверяется при установке.");
        }
        if (opt == Optimizer.SODIUM) {
            if (loader == Loader.VANILLA) r.errors.add("Sodium требует загрузчик (Fabric или NeoForge).");
            if (loader == Loader.FORGE)
                r.errors.add("Sodium не выпускается для Forge. Для Forge существует порт Embeddium.");
            if (loader == Loader.NEOFORGE)
                r.warnings.add("Sodium на NeoForge — только для поддерживаемых версий, проверяется при установке.");
        }
        return r;
    }
}
