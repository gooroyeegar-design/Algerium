package dz.algerium.browser;

import org.junit.Test;
import static org.junit.Assert.*;

public class BrowserPolicyTest {
    @Test public void searchTermsBecomeSearches() {
        assertTrue(BrowserPolicy.normalize("tea").startsWith("https://www.google.com/search?q="));
    }

    @Test public void unsafeSchemesAreRejected() {
        assertFalse(BrowserPolicy.isHttpUrl("file:///etc/passwd"));
        assertFalse(BrowserPolicy.isHttpUrl("javascript:alert(1)"));
        assertFalse(BrowserPolicy.isHttpUrl("https://user:pass@example.com"));
    }

    @Test public void localhostAndLoopbackArePrivate() {
        assertTrue(BrowserPolicy.isPrivateOrLocalHost("localhost"));
        assertTrue(BrowserPolicy.isPrivateOrLocalHost("127.0.0.1"));
        assertTrue(BrowserPolicy.isPrivateOrLocalHost("::1"));
    }

    @Test public void relativeResolutionStaysWebOnly() throws Exception {
        assertEquals("https://example.com/a.js",
                BrowserPolicy.resolveHttp("https://example.com/index.html", "/a.js"));
        try {
            BrowserPolicy.resolveHttp("https://example.com/index.html", "file:///etc/passwd");
            fail("file URL must be rejected");
        } catch (SecurityException expected) {}
    }
}
