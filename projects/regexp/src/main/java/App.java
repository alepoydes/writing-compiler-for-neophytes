package wcn;

import wcn.fsa.*;
import wcn.lexer.*;
import wcn.terminal.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;

public class App {
    public static<T> List<T> asList(Iterator<T> src) {
        List<T> result=new ArrayList();
        while(src.hasNext()) result.add(src.next());
        return result;
    };
    /**
     * Лексемы для регулярных выражений
     */
    enum Token {
        LITERAL, STAR, PLUS, BEGIN, END, OR, OPTION, ANY;
    }
    /**
     * Лексер для регулярных выражений в общепринятой записи.
     */
    static CharLexer<Token,UChar> makeLexer() {
        Combinators<UChar,Token,UChar> combinators=new Combinators(new KeyPredicateMultiMap());
        FSA<UChar,Token,UChar> star=combinators.literal(UChar.asList("*"),Token.STAR);
        FSA<UChar,Token,UChar> plus=combinators.literal(UChar.asList("+"),Token.PLUS);
        FSA<UChar,Token,UChar> option=combinators.literal(UChar.asList("?"),Token.OPTION);
        FSA<UChar,Token,UChar> begin=combinators.literal(UChar.asList("("),Token.BEGIN);
        FSA<UChar,Token,UChar> end=combinators.literal(UChar.asList(")"),Token.END);
        FSA<UChar,Token,UChar> or=combinators.literal(UChar.asList("|"),Token.OR);
        FSA<UChar,Token,UChar> any=combinators.literal(UChar.asList("."),Token.ANY);
        HashSet<UChar> reserved=new HashSet(UChar.asList("*+?()|\\."));
        HashSet<UChar> ordinary=new HashSet();
        for(char c=32; c<127; c++) {
            UChar uc=new UChar(c);
            if(!reserved.contains(uc)) ordinary.add(uc);
        };
        FSA<UChar,Token,UChar> symbolOrdinary=combinators.anyOf(ordinary,Token.LITERAL);
        FSA<UChar,Token,UChar> symbolEscaped=combinators.concatenation(Arrays.asList(
            combinators.literal(UChar.asList("\\"),Token.LITERAL),
            combinators.anyOf(reserved,Token.LITERAL)
        ));
        FSA<UChar,Token,UChar> symbol=combinators.union(Arrays.asList(symbolOrdinary,symbolEscaped));
        return new CharLexer(Arrays.asList(star, plus, option, begin, end, or, symbol, any));
    };

    public static void main(String[] args) throws IOException {
        CharLexer<Token,UChar> lexer=makeLexer();
        BufferedReader input=new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String str;
        System.out.println("Enter regexp:");
        while((str=input.readLine())!=null) {
            List<UChar> list=UChar.asList(str);
            try {
                ILexerIterator<LexerResult<UChar,Token>> iterator=lexer.parseE(list);
                while(iterator.hasNextE())
                    System.out.println(iterator.nextE()); 
            } catch(LexerError error) {
                System.out.println(error);
            };
        }
    }
}
