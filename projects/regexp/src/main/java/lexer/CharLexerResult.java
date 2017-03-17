package wcn.lexer;

import wcn.terminal.*;

import java.util.List;

/**
 * Аналог LexerResult, но также хранит информацию о
 * позиции начала лексемы.
 */
public class CharLexerResult<F> extends LexerResult<UChar, F> {
    /**
     * Создает хранилище для лексемы lexeme и соотвествующей ей подстроки string.
     */
    CharLexerResult(List<UChar> string, F lexeme, int line, int column) {
        super(string, lexeme);
        this.line=line;
        this.column=column;
    }
    /**
     * Возвращает номер строки
     */
    public int getLine() {
        return this.line;
    }
    /**
     * Возвращает номер столбца
     */
    public int getColumn() {
        return this.column;
    }
    @Override public boolean equals(Object obj) { 
        return super.equals(obj);
    };    
    /**
     * Преобразует лексему в строку, используем для отладки
     */
    @Override public String toString() {
        return String.format("/%d:%d/%s", this.line, this.column,  super.toString());
    };
    /**
     * Хранишище номера строки и столбца
     */
    private int line;
    private int column;    
}