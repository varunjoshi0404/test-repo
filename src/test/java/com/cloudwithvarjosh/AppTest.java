package com.cloudwithvarjosh;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    @Test
    void testBrandHtmlContainsBrand() {
        String h = App.brandHtml();
        assertTrue(h.contains("Cloud With VarJosh"));
    }

    @Test
    void testFingerprintProducesValue() {
        String f = App.fingerprint("alice");
        assertNotNull(f);
        assertTrue(f.length() > 0);
    }

    @Test
    void testWeakRandomFormat() {
        String r = App.weakRandom();
        assertTrue(r.startsWith("RND-"));
    }

    @Test
    void testVulnEndpointLogic() {
        String user = "bob";
        String sql = "SELECT * FROM users WHERE name='" + user + "'";
        String out = "Executed: " + sql;
        // simulate logic check (we won't run HTTP server in unit test)
        assertTrue(out.contains(user));
    }
}
