package wcn.lexer;

import wcn.fsa.*;
import wcn.terminal.*;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;


public class LexerTest {
    public static<T> List<T> asList(Iterator<T> src) {
        List<T> result=new ArrayList();
        while(src.hasNext()) result.add(src.next());
        return result;
    };

    public CharLexer<Integer,UChar> makeLexer() {
        Combinators<UChar,Integer,UChar> combinators=new Combinators(new KeyPredicateMultiMap());
        FSA<UChar,Integer,UChar> first=combinators.literal(UChar.asList("ab"),0);
        FSA<UChar,Integer,UChar> second=combinators.literal(UChar.asList("c"),1);
        return new CharLexer(Arrays.asList(first, second));
    }

    @Test public void testSimple() throws LexerError {
        CharLexer<Integer, UChar> lexer=makeLexer();
        List<CharLexerResult<Integer>> expected=Arrays.asList(
            new CharLexerResult(UChar.asList("ab"),0,1,1),
            new CharLexerResult(UChar.asList("c"),1,1,3),
            new CharLexerResult(UChar.asList("ab"),0,1,4)
        );
        List<CharLexerResult<Integer>> obtained=asList(lexer.parse(UChar.asList("abcab")));
        assertEquals(expected, obtained);
        expected=Arrays.asList(
           new CharLexerResult(UChar.asList("c"),1,1,1),
           new CharLexerResult(UChar.asList("ab"),0,1,2),
           new CharLexerResult(UChar.asList("ab"),0,1,4)
        );
        obtained=asList(lexer.parse(UChar.asList("cabab")));
        assertEquals(expected, obtained);
     }

    @Test public void testEOLFail() throws LexerError {
        CharLexer<Integer, UChar> lexer=makeLexer();
        try {
            asList(lexer.parse(UChar.asList("d")));
            fail("Parsing should fail");
        } catch(LexerError error) {};
        try {
            asList(lexer.parse(UChar.asList("abd")));
            fail("Parsing should fail");
        } catch(LexerError error) {};
    } 

    @Test public void testInnerFail() throws LexerError {
        CharLexer<Integer, UChar> lexer=makeLexer();
        try {
            asList(lexer.parse(UChar.asList("ac")));
            fail("Parsing should fail");
        } catch(LexerError error) {};
    }

}
