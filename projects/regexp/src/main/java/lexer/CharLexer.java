package wcn.lexer;

import wcn.fsa.*;
import wcn.terminal.*;

import java.util.function.Function;
import java.util.Iterator;
import java.util.List;

/**
 * Реализация лексера для Unicode символов в качестве терминалов.
 * Умеет считать номер строки и столбца во входном файле.
 */
public class CharLexer<F,P> extends Lexer<UChar,F,P> {
    public CharLexer(List<FSA<UChar,F,P>> lexemes) {
        super(lexemes);
    }
    public void reset() {
        super.reset();
        this.currentLine=this.line=1; 
        this.currentColumn=this.column=1;
    };
    @Override public CharLexerResult<F> parse_symbol(UChar symbol) throws CharLexerError {
        try {
            LexerResult<UChar, F> result=super.parse_symbol(symbol);
            CharLexerResult enchanced=null;
            if(result!=null) {
                enchanced=new CharLexerResult(result.getString(), result.getLexeme(), this.line, this.column);
                this.line=this.currentLine;
                this.column=this.currentColumn;
            };
            switch(symbol.toChar()) {
                case '\r': break;
                case '\n': this.currentLine++; this.currentColumn=1; break;
                default: this.currentColumn++; 
            };
            return enchanced;
        } catch(LexerError error) {
            throw new CharLexerError(error.toString(), this.currentLine, this.currentColumn);
        }
    }
    @Override public CharLexerResult<F> parse_eol() throws CharLexerError {
        try {
            LexerResult<UChar, F> result=super.parse_eol();
            if(result==null) return null;
            return new CharLexerResult(result.getString(), result.getLexeme(), this.line, this.column);
        } catch(LexerError error) {
            throw new CharLexerError(error.toString(), this.currentLine, this.currentColumn);
        }
    }
    /**
     * Номер строки и столбца текущего символа
     */
    private int currentLine;
    private int currentColumn;
    /**
     * Номер строки и столбца начала лексемы
     */
    private int line;
    private int column;
}
