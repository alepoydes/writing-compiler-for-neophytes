package rustless;

public class ExecutionError extends Exception {
    public ExecutionError(String message) {
        super(message);
    }

    public ExecutionError(String message, Throwable throwable) {
        super(message, throwable);
    }
}