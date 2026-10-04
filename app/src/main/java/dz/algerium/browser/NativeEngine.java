package dz.algerium.browser;
public final class NativeEngine {
    static { System.loadLibrary("algerium_native"); }
    private NativeEngine() {}
    public static native String extractText(String html, String url);
}