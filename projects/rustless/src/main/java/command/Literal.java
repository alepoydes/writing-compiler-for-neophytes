package rustless.command;

import rustless.*;
import rustless.type.*;

import org.antlr.v4.runtime.misc.*;

// Base class for AST nodes
public class Literal extends Command {
    // type and value can be null.
    public Literal(Value value) 
    throws ParseCancellationException {
        this.value=value;
    }
    public final Value value;
    public Value run(Context ctx) throws ExecutionError { 
        return this.value;
    }
}