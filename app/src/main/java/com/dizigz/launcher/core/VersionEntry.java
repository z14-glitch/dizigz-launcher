package com.dizigz.launcher.core;

public final class VersionEntry {
    public enum Type {
        RELEASE, SNAPSHOT, OLD_BETA, OLD_ALPHA, UNKNOWN;
        static Type of(String s) {
            if ("release".equals(s)) return RELEASE;
            if ("snapshot".equals(s)) return SNAPSHOT;
            if ("old_beta".equals(s)) return OLD_BETA;
            if ("old_alpha".equals(s)) return OLD_ALPHA;
            return UNKNOWN;
        }
    }

    public final String id;
    public final Type type;
    public final String url;
    public final String releaseTime;
    public final String sha1;

    VersionEntry(String id, Type type, String url, String releaseTime, String sha1) {
        this.id = id; this.type = type; this.url = url; this.releaseTime = releaseTime; this.sha1 = sha1;
    }
}
