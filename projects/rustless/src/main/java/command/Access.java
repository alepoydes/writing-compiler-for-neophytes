package rustless.command;

import rustless.*;
import rustless.type.*;

import org.antlr.v4.runtime.misc.*;

// Base class for AST nodes
public class Access extends Command {
    // type and value can be null.
    public Access(String name) 
    throws ParseCancellationException {
        this.name=name;
    }
    public final String name;
    public Value run(Context ctx) throws ExecutionError { 
        Value value=ctx.variables.get(this.name); 
        if(value==null) 
            throw new ExecutionError(String.format("Variable '%s' is not defined",this.name));
        return value;
    }
}