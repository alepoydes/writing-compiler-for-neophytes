package wcn.regexp;

import wcn.terminal.*;
import wcn.fsa.*;
import wcn.parser.*;

import java.util.Arrays;
import java.util.ArrayList;

import org.junit.Test;
import static org.junit.Assert.*;

public class RegExpTest {
    @Test public void testLiteral() throws ParserError {
        IDFA<UChar,Integer> dfa=RegExp.compile("a", false);
        assertTrue(dfa.match(UChar.asList("a")));
        assertFalse(dfa.match(UChar.asList("")));
        assertFalse(dfa.match(UChar.asList("b")));
        assertFalse(dfa.match(UChar.asList("aa")));
    }
    @Test public void testConcatenation() throws ParserError {
        RegExp regexp=new RegExp("ab");
        assertTrue(regexp.match("ab"));
        assertFalse(regexp.match("a"));
        assertFalse(regexp.match("b"));
        assertFalse(regexp.match("aba"));
        assertFalse(regexp.match("aab"));
    }

    @Test public void testOption() throws ParserError {
        RegExp regexp=new RegExp("ab?");
        assertTrue(regexp.match("ab"));
        assertTrue(regexp.match("a"));
        assertFalse(regexp.match(""));
        assertFalse(regexp.match("abb"));
    }

    @Test public void testGroup() throws ParserError {
        IDFA<UChar,Integer> dfa=RegExp.compile("b(ab)?", true);
        RegExp regexp=new RegExp("b(ab)?");
        assertTrue(regexp.match("bab"));
        assertTrue(regexp.match("b"));
        assertFalse(regexp.match("ba"));
        assertFalse(regexp.match(""));
        assertFalse(regexp.match("baba"));
    }    
    @Test public void testStar() throws ParserError {
        RegExp regexp=new RegExp("b(ab)*");
        assertTrue(regexp.match("bab"));
        assertTrue(regexp.match("b"));
        assertTrue(regexp.match("babab"));
        assertFalse(regexp.match("ba"));
        assertFalse(regexp.match(""));
        assertFalse(regexp.match("baba"));
    }    
    @Test public void testStarAlt() throws ParserError {
        RegExp regexp=new RegExp("b(ab)+?");
        assertTrue(regexp.match("bab"));
        assertTrue(regexp.match("b"));
        assertTrue(regexp.match("babab"));
        assertFalse(regexp.match("ba"));
        assertFalse(regexp.match(""));
        assertFalse(regexp.match("baba"));
    }    
    @Test public void testPlus() throws ParserError {
        RegExp regexp=new RegExp("b(ab)+");
        assertTrue(regexp.match("bab"));
        assertFalse(regexp.match("b"));
        assertTrue(regexp.match("babab"));
        assertFalse(regexp.match("ba"));
        assertFalse(regexp.match(""));
        assertFalse(regexp.match("baba"));
    }
    @Test public void testUnion() throws ParserError {
        RegExp regexp=new RegExp("(aa|b)+");
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
