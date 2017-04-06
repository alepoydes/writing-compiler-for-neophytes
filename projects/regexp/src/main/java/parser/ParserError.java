package wcn.parser;

public class ParserError extends Exception {
    public ParserError(String message) {
        super(message);
    }

    public ParserError(String message, Throwable throwable) {
        super(message, throwable);
    }
}