package wcn.lexer;

public class LexerError extends Exception {
    public LexerError(String message) {
        super(message);
    }

    public LexerError(String message, Throwable throwable) {
        super(message, throwable);
    }
}