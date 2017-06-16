package rustless.command;

import rustless.*;
import rustless.type.*;

import org.antlr.v4.runtime.misc.*;

// Base class for AST nodes
public class Assign extends Command {
    // type and value can be null.
    public Assign(String name, Command expr) 
    throws ParseCancellationException {
        this.name=name;
        this.expr=expr;
    }
    public final String name;
    public final Command expr;
    public Value run(Context ctx) throws ExecutionError { 
        Value value=this.expr.run(ctx);
         if(!ctx.variables.containsKey(this.name)) 
            throw new ExecutionError(String.format("Variable '%s' is not defined",this.name));
        ctx.variables.put(this.name, value);
        return value;
    }
}