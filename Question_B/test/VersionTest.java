import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static utils.StringUtilities.versionCompare;

public class VersionTest {
    // compare versions of equal length
    @Test
    public void validFormat1() {
        String v1 = "1.1.3.1";
        String v2 = "2.1.3.1";
        assertEquals(versionCompare(v1, v2), -1);
    }

    @Test
    public void validFormat2() {
        String v1 = "2.1.3.1";
        String v2 = "1.1.3.1";
        assertEquals(versionCompare(v1, v2), 1);
    }

    @Test
    public void validFormat3() {
        String v1 = "4.1.3.1";
        String v2 = "4.2.3.1";
        assertEquals(versionCompare(v1, v2), -1);
    }

    @Test
    public void validFormat4() {
        String v1 = "4.2.3.1";
        String v2 = "4.1.3.1";
        assertEquals(versionCompare(v1, v2), 1);
    }

    @Test
    public void validFormat5() {
        String v1 = "4.2.3.1";
        String v2 = "4.2.5.1";
        assertEquals(versionCompare(v1, v2), -1);
    }

    @Test
    public void validFormat6() {
        String v1 = "4.2.5.1";
        String v2 = "4.2.3.1";
        assertEquals(versionCompare(v1, v2), 1);
    }

    @Test
    public void validFormat7() {
        String v1 = "4.2.5.4";
        String v2 = "4.2.5.8";
        assertEquals(versionCompare(v1, v2), -1);
    }

    @Test
    public void validFormat8() {
        String v1 = "4.2.5.8";
        String v2 = "4.2.5.4";
        assertEquals(versionCompare(v1, v2), 1);
    }
    @Test
    public void validFormat9() {
        String v1 = "4.2.5.8";
        String v2 = "4.2.5.8";
        assertEquals(versionCompare(v1, v2), 0);
    } 
    // compare versions of unequal length(tricky cases)
    @Test
    public void validFormat11() {
        String v1 = "4.2.5.0.0.0";
        String v2 = "4.2.5";
        assertEquals(versionCompare(v1, v2), 0);
    }

    @Test
    public void validFormat12() {
        String v1 = "4.2.5.0.0.0.9";
        String v2 = "4.2.5";
        assertEquals(versionCompare(v1, v2), 1);
    }

    @Test
    public void validFormat13() {
        String v1 = "4.2.5";
        String v2 = "4.2.5.0.0.4.5";
        assertEquals(versionCompare(v1, v2), -1);
    }

    // test white spaces
    @Test
    public void validFormat14() {
        String v1 = "4.2.5       ";
        String v2 = "4.2.5.0.0.4.5      ";
        assertEquals(versionCompare(v1, v2), -1);
    }
    
    // Tolerance of reasonable mistakes
    @Test
    public void validFormat15() {
        String v1 = "4.  2.5  ";
        String v2 = "4.2.5.   8.2  .4  ";
        assertEquals(versionCompare(v1, v2), -1);

    }
    // Test invalid version formats
    @Test
    public void invalidFormat1() {
        Exception e = assertThrows(IllegalArgumentException.class,
                                   ()->versionCompare("4.2.5.8.2abc", "1.1"));
        assertEquals("Invalid version format", e.getMessage());

    }
    @Test
    public void invalidFormat2() {
        Exception e = assertThrows(IllegalArgumentException.class,
                                   ()->versionCompare("1.1","4.2d&.5.8"));
        assertEquals("Invalid version format", e.getMessage());

    }
    @Test
    public void invalidFormat3() {
        Exception e = assertThrows(IllegalArgumentException.class,
                                   ()->versionCompare("1.1", "4.2.5.8.2. ."));
        assertEquals("Invalid version format", e.getMessage());

    }
    @Test
    public void invalidFormat4() {
        Exception e = assertThrows(IllegalArgumentException.class,
                                   ()->versionCompare("2.1", "4.2.5.8.2.xyz"));
        assertEquals("Invalid version format", e.getMessage());

    }
}
