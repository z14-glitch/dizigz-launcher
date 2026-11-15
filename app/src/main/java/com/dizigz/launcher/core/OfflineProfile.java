package com.dizigz.launcher.core;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Pattern;

/** Offline-профиль: ник + детерминированный UUID (как у ванильного offline-режима). */
public final class OfflineProfile {
    private static final Pattern NAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");

    public final String name;
    public final UUID uuid;

    private OfflineProfile(String name, UUID uuid) { this.name = name; this.uuid = uuid; }

    public static boolean isValidName(String name) { return name != null && NAME.matcher(name).matches(); }

    public static OfflineProfile create(String name) {
        if (!isValidName(name)) throw new IllegalArgumentException("Ник: 3–16 символов, только A-Z, a-z, 0-9, _");
        UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        return new OfflineProfile(name, id);
    }
}
