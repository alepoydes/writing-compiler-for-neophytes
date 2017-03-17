package wcn.lexer;

import java.io.*;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.stream.Collectors;

public class App {
    public static List<Character> asList(String str) {
        return str.chars().mapToObj(i -> (char)i).collect(Collectors.toList());
    };
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
    static CharLexer<Token,Character> makeLexer() {
        IPredicateMapFactory<Character,Token,State> factory=new KeyPredicateMapFactory();        
        Combinators<Character,Token,Character> combinators=new Combinators(factory);
        FSA<Character,Token,Character> star=combinators.literal(asList("*"),Token.STAR);
        FSA<Character,Token,Character> plus=combinators.literal(asList("+"),Token.PLUS);
        FSA<Character,Token,Character> option=combinators.literal(asList("?"),Token.OPTION);
        FSA<Character,Token,Character> begin=combinators.literal(asList("("),Token.BEGIN);
        FSA<Character,Token,Character> end=combinators.literal(asList(")"),Token.END);
        FSA<Character,Token,Character> or=combinators.literal(asList("|"),Token.OR);
        FSA<Character,Token,Character> any=combinators.literal(asList("."),Token.ANY);
        HashSet<Character> reserved=new HashSet(Arrays.asList('*','+','?','(',')','|','\\','.'));
        HashSet<Character> ordinary=new HashSet();
        for(char c=32; c<127; c++) if(!reserved.contains(c)) ordinary.add(c);
        FSA<Character,Token,Character> symbolOrdinary=combinators.anyOf(ordinary,Token.LITERAL);
        FSA<Character,Token,Character> symbolEscaped=combinators.concatenation(Arrays.asList(
            combinators.literal(Arrays.asList('\\'),Token.LITERAL),
            combinators.anyOf(reserved,Token.LITERAL)
        ));
        FSA<Character,Token,Character> symbol=combinators.union(Arrays.asList(symbolOrdinary,symbolEscaped));
        return new CharLexer(Arrays.asList(star, plus, option, begin, end, or, symbol, any));
    };

    public static void main(String[] args) throws IOException {
        CharLexer<Token,Character> lexer=makeLexer();
        BufferedReader input=new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String str;
        System.out.println("Enter regexp:");
        while((str=input.readLine())!=null) {
            List<Character> list=asList(str);
            try {
                ILexerIterator<LexerResult<Character,Token>> iterator=lexer.parseE(list);
                while(iterator.hasNextE())
                    System.out.println(iterator.nextE()); 
            } catch(LexerError error) {
                System.out.println(error);
            };
        }
    }
}
