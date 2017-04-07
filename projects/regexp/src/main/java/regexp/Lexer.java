package wcn.regexp;

import wcn.terminal.*;
import wcn.parser.*;

import java.util.Iterator;

public class Lexer implements Iterator<Pair<Lexer.Symbol,Object>> {
    enum Symbol implements ICharSet<Symbol,Symbol> { 
        Literal, Plus, Star, Option, Bar, Open, Close; 
    };

    protected Iterator<UChar> iterator;
    public Lexer(Iterator<UChar> iterator) {
        this.iterator=iterator;
    }
    public boolean hasNext() { 
        return this.iterator.hasNext();
    }
    public Pair<Symbol,Object> next() {
        UChar letter=this.iterator.next();
        Symbol symbol;
        switch(letter.toChar()) {
            case '+': symbol=Lexer.Symbol.Plus; break;
            case '*': symbol=Lexer.Symbol.Star; break;
            case '?': symbol=Lexer.Symbol.Option; break;
            case '(': symbol=Lexer.Symbol.Open; break;
            case ')': symbol=Lexer.Symbol.Close; break;
            case '|': symbol=Lexer.Symbol.Bar; break;
            case '\\': 
                symbol=Lexer.Symbol.Literal;
                if(this.iterator.hasNext()) letter=this.iterator.next();
                break;
            default: symbol=Lexer.Symbol.Literal;
        };
        return new Pair(symbol, letter);
    }
}