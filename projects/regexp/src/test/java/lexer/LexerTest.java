package wcn.lexer;

import wcn.fsa.*;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;


public class LexerTest {
    public static List<Character> asList(String str) {
        return str.chars().mapToObj(i -> (char)i).collect(Collectors.toList());
    };
    public static<T> List<T> asList(Iterator<T> src) {
        List<T> result=new ArrayList();
        while(src.hasNext()) result.add(src.next());
        return result;
    };

    public CharLexer<Integer,Character> makeLexer() {
        IPredicateMultiMapFactory<Character,Integer,State> factory=new KeyPredicateMultiMapFactory();        
        Combinators<Character,Integer,Character> combinators=new Combinators(factory);
        FSA<Character,Integer,Character> first=combinators.literal(asList("ab"),0);
        FSA<Character,Integer,Character> second=combinators.literal(asList("c"),1);
        return new CharLexer(Arrays.asList(first, second));
    }

    @Test public void testSimple() throws LexerError {
        CharLexer<Integer, Character> lexer=makeLexer();
        List<CharLexerResult<Integer>> expected=Arrays.asList(
            new CharLexerResult(asList("ab"),0,1,1),
            new CharLexerResult(asList("c"),1,1,3),
            new CharLexerResult(asList("ab"),0,1,4)
        );
        List<CharLexerResult<Integer>> obtained=asList(lexer.parse(asList("abcab")));
        assertEquals(expected, obtained);
        expected=Arrays.asList(
           new CharLexerResult(asList("c"),1,1,1),
           new CharLexerResult(asList("ab"),0,1,2),
           new CharLexerResult(asList("ab"),0,1,4)
        );
        obtained=asList(lexer.parse(asList("cabab")));
        assertEquals(expected, obtained);
     }

    @Test public void testEOLFail() throws LexerError {
        CharLexer<Integer, Character> lexer=makeLexer();
        try {
            asList(lexer.parse(asList("d")));
            fail("Parsing should fail");
        } catch(LexerError error) {};
        try {
            asList(lexer.parse(asList("abd")));
            fail("Parsing should fail");
        } catch(LexerError error) {};
    } 

    @Test public void testInnerFail() throws LexerError {
        CharLexer<Integer, Character> lexer=makeLexer();
        try {
            asList(lexer.parse(asList("ac")));
            fail("Parsing should fail");
        } catch(LexerError error) {};
    }

}
