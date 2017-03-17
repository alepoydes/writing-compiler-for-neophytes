package wcn.lexer;

import java.util.Iterator;

public interface ILexerIterator<T> {
    boolean hasNextE() throws LexerError;
    T nextE() throws LexerError;
}