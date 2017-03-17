package wcn.lexer;

public class CharLexerError extends LexerError {
    /**
     * Аналогично LexerError, но хранит информацию о строке и столбце,
     * где возникла ошибка.
     */
    public CharLexerError(String message, int line, int column) {
        super(message);
        this.line=line;
        this.column=column;
    }

    public CharLexerError(String message, Throwable throwable) {
        super(message, throwable);
    }

    @Override public String getMessage() {
        return String.format("%s at line %d column %d", super.getMessage(), this.line, this.column);
    };

    private int line;
    private int column;
}