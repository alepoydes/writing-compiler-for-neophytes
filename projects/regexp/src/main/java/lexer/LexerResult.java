package wcn.lexer;

import java.util.List;
import java.lang.StringBuilder;

/**
 * Хранит в себе лексему, выделенную парсером.
 */
public class LexerResult<T,F> {
    /**
     * Создает хранилище для лексемы lexeme и соотвествующей ей подстроки string.
     */
    LexerResult(List<T> string, F lexeme) {
        this.string=string;
        this.lexeme=lexeme;
    }
    /**
     * Возвращает подстроку, свернутую в лексему
     */
    public List<T> getString() {
        return this.string;
    }
    /**
     * Возвращает лексему.
     */
    public F getLexeme() {
        return this.lexeme;
    }
    /**
     * Преобразует лексему в строку, используем для отладки
     */
    @Override public String toString() {
        StringBuilder result=new StringBuilder();
        result.append(this.lexeme);
        result.append("(");
        for(T symbol: this.string) result.append(symbol);
        result.append(")");
        return result.toString();
    };
    @Override public boolean equals(Object obj) { 
        if(obj==null) return false;
        if(!(obj instanceof LexerResult)) return false;
        LexerResult<T,F> result=(LexerResult)obj;
        if(this.lexeme!=result.lexeme) return false;
        return true;
    };
    /**
     * Хранилице для подстроки, свернутой в лексемуу
     */
    private List<T> string;
    /**
     * Хранилище для лексемы
     */
    private F lexeme;
}