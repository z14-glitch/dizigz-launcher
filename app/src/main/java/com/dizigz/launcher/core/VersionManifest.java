package com.dizigz.launcher.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Разбор Mojang version_manifest_v2.json. Записи с недоверенными URL отбрасываются. */
public final class VersionManifest {
    public final String latestRelease;
    public final String latestSnapshot;
    public final List<VersionEntry> versions;
    public final int rejected;

    private VersionManifest(String r, String s, List<VersionEntry> v, int rejected) {
        this.latestRelease = r; this.latestSnapshot = s;
        this.versions = Collections.unmodifiableList(v); this.rejected = rejected;
    }

    public static boolean isTrustedUrl(String url) {
        try {
            URI u = new URI(url);
            String h = u.getHost();
            return "https".equals(u.getScheme()) && h != null
                    && (h.equals("piston-meta.mojang.com") || h.equals("launchermeta.mojang.com")
                        || h.equals("piston-data.mojang.com"));
        } catch (Exception e) { return false; }
    }

    @SuppressWarnings("unchecked")
    public static VersionManifest parse(String json) {
        Map<String, Object> root = Json.parseObject(json);
        Object latest = root.get("latest");
        String rel = null, snap = null;
        if (latest instanceof Map) {
            rel = (String) ((Map<String, Object>) latest).get("release");
            snap = (String) ((Map<String, Object>) latest).get("snapshot");
        }
        Object arr = root.get("versions");
        if (!(arr instanceof List)) throw new IllegalArgumentException("В манифесте нет списка versions");
        List<VersionEntry> out = new ArrayList<>();
        int rejected = 0;
        for (Object o : (List<Object>) arr) {
            if (!(o instanceof Map)) { rejected++; continue; }
            Map<String, Object> m = (Map<String, Object>) o;
            String id = (String) m.get("id");
            String url = (String) m.get("url");
            if (id == null || url == null || !isTrustedUrl(url)) { rejected++; continue; }
            out.add(new VersionEntry(id, VersionEntry.Type.of((String) m.get("type")), url,
                    (String) m.get("releaseTime"), (String) m.get("sha1")));
        }
        return new VersionManifest(rel, snap, out, rejected);
    }

    public List<VersionEntry> ofType(VersionEntry.Type t) {
        List<VersionEntry> r = new ArrayList<>();
        for (VersionEntry e : versions) if (e.type == t) r.add(e);
        return r;
    }

    public VersionEntry find(String id) {
        for (VersionEntry e : versions) if (e.id.equals(id)) return e;
        return null;
    }
}

