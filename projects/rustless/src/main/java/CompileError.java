package rustless;

public class CompileError extends Exception {
    public CompileError(String message) {
        super(message);
    }

    public CompileError(String message, Throwable throwable) {
        super(message, throwable);
    }
}