package wcn.terminal;

import java.util.Arrays;
import java.util.ArrayList;

import org.junit.Test;
import static org.junit.Assert.*;

public class URangeTest {
    @Test public void testIntersection() {
        URange a=new URange('f', 'z');
        URange b=new URange('a', 'k');
        URange c=new URange('m', 't');
        assertEquals(new URange('f','k'), a.intersect(b));
        assertEquals(new URange('f','k'), b.intersect(a));
        assertEquals(new URange('m','t'), a.intersect(c));
        assertEquals(new URange('m','t'), c.intersect(a));
        assertEquals(null, c.intersect(b));
        assertEquals(null, b.intersect(c));
    }
    @Test public void testSubtraction() {
        URange a=new URange('f', 'z');
        URange b=new URange('a', 'k');
        URange c=new URange('m', 't');
        assertEquals(Arrays.asList(new URange('l','z')), new ArrayList(a.subtract(b)));
        assertEquals(Arrays.asList(new URange('a','e')), new ArrayList(b.subtract(a)));
        assertEquals(Arrays.asList(new URange('f','l'),new URange('u','z')), new ArrayList(a.subtract(c)));
        assertEquals(Arrays.asList(), new ArrayList(c.subtract(a)));
        assertEquals(Arrays.asList(new URange('m','t')), new ArrayList(c.subtract(b)));
        assertEquals(Arrays.asList(new URange('a','k')), new ArrayList(b.subtract(c)));
    }

}
