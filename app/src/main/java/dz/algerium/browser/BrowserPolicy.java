package dz.algerium.browser;

import java.net.*;
import java.util.*;
import java.io.*;

public final class BrowserPolicy {
    private static final int MAX_PAGE_BYTES = 8 * 1024 * 1024;
    private static final int MAX_SCRIPT_BYTES = 2 * 1024 * 1024;
    private static final int MAX_FETCH_BYTES = 4 * 1024 * 1024;

    private BrowserPolicy() {}

    public static String normalize(String raw) {
        if (raw == null) return "about:home";
        String s = raw.trim();
        if (s.isEmpty()) return "about:home";
        if (s.equalsIgnoreCase("algerium://home")) return "about:home";
        if (s.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            return s;
        }
        if (s.matches("(?i)^(localhost|127\\.0\\.0\\.1)(:\\d+)?(/.*)?$")) {
            return "http://" + s;
        }
        if (s.matches("(?i)^((https?://)?([a-z0-9-]+\\.)+[a-z]{2,})(/.*)?$")) {
            return s.matches("(?i)^https?://.*") ? s : "https://" + s;
        }
        try {
            return "https://www.google.com/search?q=" + URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return "https://www.google.com/search?q=" + s.replace(" ", "+");
        }
    }

    public static boolean isHttpUrl(String u) {
        if (u == null) return false;
        try {
            URI x = new URI(u);
            return ("http".equalsIgnoreCase(x.getScheme()) || "https".equalsIgnoreCase(x.getScheme()))
                    && x.getHost() != null
                    && x.getUserInfo() == null;
        } catch (Exception e) {
            return false;
        }
    }

    public static String resolveHttp(String base, String relative) throws Exception {
        if (relative == null) throw new MalformedURLException("null URL");
        URI r = new URI(relative.trim());
        URI b = new URI(base);
        URI out = b.resolve(r);
        if (!isHttpUrl(out.toString())) throw new SecurityException("Blocked non-web resource");
        return out.toString();
    }

    public static boolean isSafeScriptUrl(String base, String src) {
        try {
            String u = resolveHttp(base, src);
            URI x = new URI(u);
            return x.getUserInfo() == null;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isPrivateOrLocalHost(String host) {
        if (host == null || host.isEmpty()) return true;
        String h = host.toLowerCase(Locale.US);
        if (h.equals("localhost") || h.endsWith(".localhost") || h.equals("local")) return true;
        try {
            InetAddress[] all = InetAddress.getAllByName(host);
            for (InetAddress a : all) {
                if (a.isAnyLocalAddress() || a.isLoopbackAddress() || a.isLinkLocalAddress()
                        || a.isSiteLocalAddress() || a.isMulticastAddress()) return true;
                byte[] b = a.getAddress();
                if (b.length == 16) {
                    int first = b[0] & 255, second = b[1] & 255;
                    if ((first & 0xfe) == 0xfc) return true; // fc00::/7
                }
            }
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    public static String readLimited(InputStream in, int maxBytes) throws IOException {
        if (in == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int total = 0, n;
        while ((n = in.read(buf)) != -1) {
            if (total + n > maxBytes) {
                int keep = maxBytes - total;
                if (keep > 0) out.write(buf, 0, keep);
                throw new IOException("Response too large");
            }
            out.write(buf, 0, n);
            total += n;
        }
        return new String(out.toByteArray(), "UTF-8");
    }

    public static int pageLimit() { return MAX_PAGE_BYTES; }
    public static int scriptLimit() { return MAX_SCRIPT_BYTES; }
    public static int fetchLimit() { return MAX_FETCH_BYTES; }
}
