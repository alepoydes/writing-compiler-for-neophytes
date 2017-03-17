package wcn.fsa;

import wcn.terminal.*;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

public class CombinatorsTest {
    @Test public void testLiteral() {
        Combinators<Character,Integer,Character> combinators=new Combinators(new KeyPredicateMultiMap());
        FSA<Character,Integer,Character> automaton=combinators.literal(Arrays.asList('a','b','c'),0);
        assertTrue(automaton.match(Arrays.asList('a','b','c')));
        assertFalse(automaton.match(Arrays.asList('a','b')));
        assertFalse(automaton.match(Arrays.asList('a','b','c','d')));
        assertFalse(automaton.match(Arrays.asList('b','c','d')));
    }
    @Test public void testUnion() {
        Combinators<Character,Integer,Character> combinators=new Combinators(new KeyPredicateMultiMap());
        List<Character> s1=Arrays.asList('a','b');
        List<Character> s2=Arrays.asList('b');
        FSA<Character,Integer,Character> a1=combinators.literal(s1,0);
        FSA<Character,Integer,Character> a2=combinators.literal(s2,0);
        FSA<Character,Integer,Character> automaton=combinators.union(Arrays.asList(a1,a2));
        assertTrue(automaton.match(s1));
        assertTrue(automaton.match(s2));
        assertFalse(automaton.match(Arrays.asList('a')));
    }
    @Test public void testAnyOf() {
        Combinators<Character,Integer,Character> combinators=new Combinators(new KeyPredicateMultiMap());
        FSA<Character,Integer,Character> automaton=combinators.anyOf(Arrays.asList('a','b'),0);
        assertFalse(automaton.match(Arrays.asList('a','b')));
        assertTrue(automaton.match(Arrays.asList('a')));
        assertTrue(automaton.match(Arrays.asList('b')));  
        assertFalse(automaton.match(Arrays.asList('c'))); 
    }
    @Test public void testConcatenation() {
        Combinators<Character,Integer,Character> combinators=new Combinators(new KeyPredicateMultiMap());
        List<Character> s1=Arrays.asList('a','b');
        List<Character> s2=Arrays.asList('c');
        FSA<Character,Integer,Character> a1=combinators.literal(s1,0);
        FSA<Character,Integer,Character> a2=combinators.literal(s2,0);
        FSA<Character,Integer,Character> automaton=combinators.concatenation(Arrays.asList(a1,a2));
        assertFalse(automaton.match(s1));
        assertFalse(automaton.match(s2));
        assertTrue(automaton.match(Arrays.asList('a','b','c')));
        assertFalse(automaton.match(Arrays.asList('a','b','c','c')));
    }
    @Test public void testStar() {
        Combinators<Character,Integer,Character> combinators=new Combinators(new KeyPredicateMultiMap());
        List<Character> s1=Arrays.asList('a','b');
        FSA<Character,Integer,Character> a1=combinators.literal(s1,0);
        FSA<Character,Integer,Character> automaton=combinators.star(a1, 0);
        assertTrue(automaton.match(Arrays.asList()));
        assertTrue(automaton.match(s1));
        assertTrue(automaton.match(Arrays.asList('a','b','a','b')));
        assertFalse(automaton.match(Arrays.asList('a','b','a')));
        assertFalse(automaton.match(Arrays.asList('a')));
        assertFalse(automaton.match(Arrays.asList('b','a')));
    };

    @Test public void testRepeat() {
        Combinators<Character,Integer,Character> combinators=new Combinators(new KeyPredicateMultiMap());
        List<Character> s1=Arrays.asList('a','b');
        FSA<Character,Integer,Character> a1=combinators.literal(s1,0);
        FSA<Character,Integer,Character> automaton=combinators.repeat(a1);
        assertFalse(automaton.match(Arrays.asList()));
        assertTrue(automaton.match(s1));
        assertTrue(automaton.match(Arrays.asList('a','b','a','b')));
        assertFalse(automaton.match(Arrays.asList('a','b','a')));
        assertFalse(automaton.match(Arrays.asList('a')));
        assertFalse(automaton.match(Arrays.asList('b','a')));
    };
    @Test public void testOption() {
        Combinators<Character,Integer,Character> combinators=new Combinators(new KeyPredicateMultiMap());
        List<Character> s1=Arrays.asList('a','b');
        FSA<Character,Integer,Character> a1=combinators.literal(s1,0);
        FSA<Character,Integer,Character> automaton=combinators.option(a1,0);
        assertTrue(automaton.match(Arrays.asList()));
        assertTrue(automaton.match(s1));
        assertFalse(automaton.match(Arrays.asList('a','b','a','b')));
        assertFalse(automaton.match(Arrays.asList('a','b','a')));
        assertFalse(automaton.match(Arrays.asList('a')));
        assertFalse(automaton.match(Arrays.asList('b','a')));
    };

}
