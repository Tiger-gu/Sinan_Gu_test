import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
/* Remark:
 * In a real production environment, a simple class with a simple
 * method probably does not need these many tests. it should be
 * fine as long as each branch of the conditional statement is
 * invoked at least one time(white-box testing)
 * 
 * However, since this is a technical assessment, I will make my testing
 * as comprehensive as possible.
 * 
 * The following tests are designed via following methods:
 * 
 *   (Equivalent class partitioning method)
 *   There are four "test categories":
 *   a) Not overlap at all
 *   b) Complete overlap - a line is completely contained within the other
 *   c) Partial Overlap
 *   d) Overlapped at a single dot (e.g. (1, 2) and (2, 3))
 * 
 *   There will be two test cases for each category. The two test cases will be
 *   specifically designed such that they are "boundary" cases.
 * 
 *   (Negative method)
 *   a) Dots such as (1,1) should throw excetions.
 *   b) Invalid lines such as (5, 1) should throw exceptions.
 */
public class OverlapTest {
    @Test
    public void notOverlap1() {
        Line line1 = new Line(3, 5);
        Line line2 = new Line(6, 9);
        assertEquals(false, line1.overlap(line2));
    }

    @Test
    public void notOverlap2() {
        Line line1 = new Line(9, 11);
        Line line2 = new Line(1, 8);
        assertEquals(false, line1.overlap(line2));
    }

    @Test
    public void completeOverlap1() {
        Line line1 = new Line(9, 13);
        Line line2 = new Line(8, 14);
        assertEquals(true, line1.overlap(line2));
    }

    @Test
    public void completeOverlap2() {
        Line line1 = new Line(20, 61);
        Line line2 = new Line(21, 60);
        assertEquals(true, line1.overlap(line2));
    }

    
    @Test
    public void partialOverlap1() {
        Line line1 = new Line(9, 15);
        Line line2 = new Line(4, 10);
        assertEquals(true, line1.overlap(line2));
    }

    @Test
    public void partialOverlap2() {
        Line line1 = new Line(20, 61);
        Line line2 = new Line(60, 72);
        assertEquals(true, line1.overlap(line2));
    }

    @Test
    public void overlapAtDot1() {
        Line line1 = new Line(20, 61);
        Line line2 = new Line(10, 20);
        assertEquals(true, line1.overlap(line2));
    }

    @Test
    public void overlapAtDot2() {
        Line line1 = new Line(20, 61);
        Line line2 = new Line(61, 70);
        assertEquals(true, line1.overlap(line2));
    }

    @Test
    public void invalidLine() {
        Exception exception = assertThrows(IllegalArgumentException.class, 
                                           () -> new Line(20, 19));
        assertEquals("Invalid line(x1, x2): x1 should be smaller than x2", 
                     exception.getMessage());
    }

    @Test
    public void dotAsLine() {
        Exception exception = assertThrows(IllegalArgumentException.class, 
                                           () -> new Line(7, 7));
        assertEquals("Invalid line(x1, x2): x1 should be smaller than x2", 
                     exception.getMessage());
    }
}
