package at.porscheinformatik.idp.openidconnect;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class PartnerNetOpenIdConnectStateUtilsTest {

    @Test
    public void testBuildState() {
        assertThrows(IllegalArgumentException.class, () -> PartnerNetOpenIdConnectStateUtils.buildState(null, "cst"));
        assertThrows(IllegalArgumentException.class, () -> PartnerNetOpenIdConnectStateUtils.buildState("", "cst"));

        assertEquals(encodeBase64("rnd"), PartnerNetOpenIdConnectStateUtils.buildState("rnd", null));
        assertEquals(encodeBase64("rnd:"), PartnerNetOpenIdConnectStateUtils.buildState("rnd", ""));
        assertEquals(encodeBase64("rnd:cst"), PartnerNetOpenIdConnectStateUtils.buildState("rnd", "cst"));
    }

    @Test
    public void testGetCustomState() {
        assertNull(PartnerNetOpenIdConnectStateUtils.getCustomState((String) null));
        assertNull(PartnerNetOpenIdConnectStateUtils.getCustomState(new MockHttpServletRequest()));
        assertNull(PartnerNetOpenIdConnectStateUtils.getCustomState(encodeBase64("rnd")));

        assertEquals("", PartnerNetOpenIdConnectStateUtils.getCustomState(encodeBase64("rnd:")));
        assertEquals("cst", PartnerNetOpenIdConnectStateUtils.getCustomState(encodeBase64("rnd:cst")));
        assertEquals(
            "cst:with:delimiter",
            PartnerNetOpenIdConnectStateUtils.getCustomState(encodeBase64("rnd:cst:with:delimiter"))
        );
    }

    private String encodeBase64(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }
}
