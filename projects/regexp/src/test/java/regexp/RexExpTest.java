package wcn.regexp;

import wcn.terminal.*;
import wcn.fsa.*;
import wcn.parser.*;

import java.util.Arrays;
import java.util.ArrayList;

import org.junit.Test;
import static org.junit.Assert.*;

public class RexExpTest {
    @Test public void testEscapeLiteral() throws ParserError {
        RexExp regexp=new RexExp("\\\\\\+");
        assertTrue(regexp.match("\\+"));
        assertFalse(regexp.match("+"));
    }

    @Test public void testLiteral() throws ParserError {
        RexExp regexp=new RexExp("z");
        assertTrue(regexp.match("z"));
        assertFalse(regexp.match(""));
        assertFalse(regexp.match("b"));
        assertFalse(regexp.match("zz"));
    }
    @Test public void testConcatenation() throws ParserError {
        RexExp regexp=new RexExp("ab");
        assertTrue(regexp.match("ab"));
        assertFalse(regexp.match("a"));
        assertFalse(regexp.match("b"));
        assertFalse(regexp.match("aba"));
        assertFalse(regexp.match("aab"));
    }

    @Test public void testOption() throws ParserError {
        RexExp regexp=new RexExp("ab?");
        assertTrue(regexp.match("ab"));
        assertTrue(regexp.match("a"));
        assertFalse(regexp.match(""));
        assertFalse(regexp.match("abb"));
    }

    @Test public void testGroup() throws ParserError {
        IDFA<UChar,Integer> dfa=RexExp.compile("b(ab)?", true);
        RexExp regexp=new RexExp("b(ab)?");
        assertTrue(regexp.match("bab"));
        assertTrue(regexp.match("b"));
        assertFalse(regexp.match("ba"));
        assertFalse(regexp.match(""));
        assertFalse(regexp.match("baba"));
    }    
    @Test public void testStar() throws ParserError {
        RexExp regexp=new RexExp("b(ab)*");
        assertTrue(regexp.match("bab"));
        assertTrue(regexp.match("b"));
        assertTrue(regexp.match("babab"));
        assertFalse(regexp.match("ba"));
        assertFalse(regexp.match(""));
        assertFalse(regexp.match("baba"));
    }    
    @Test public void testStarAlt() throws ParserError {
        RexExp regexp=new RexExp("b(ab)+?");
        assertTrue(regexp.match("bab"));
        assertTrue(regexp.match("b"));
        assertTrue(regexp.match("babab"));
        assertFalse(regexp.match("ba"));
        assertFalse(regexp.match(""));
        assertFalse(regexp.match("baba"));
    }    
    @Test public void testPlus() throws ParserError {
        RexExp regexp=new RexExp("b(ab)+");
        assertTrue(regexp.match("bab"));
        assertFalse(regexp.match("b"));
        assertTrue(regexp.match("babab"));
        assertFalse(regexp.match("ba"));
        assertFalse(regexp.match(""));
        assertFalse(regexp.match("baba"));
    }
    @Test public void testUnion() throws ParserError {
        RexExp regexp=new RexExp("(aa|b)+");
        assertTrue(regexp.match("aa"));
        assertTrue(regexp.match("b"));
        assertTrue(regexp.match("baa"));
        assertTrue(regexp.match("aab"));
        assertTrue(regexp.match("aabaa"));
        assertTrue(regexp.match("aaaa"));
        assertFalse(regexp.match("aaa"));
        assertFalse(regexp.match(""));
        assertFalse(regexp.match("a"));        
        assertFalse(regexp.match("ba"));
    }
    

}
