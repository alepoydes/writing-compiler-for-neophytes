package rustless.command;

import rustless.*;
import rustless.type.*;

import org.antlr.v4.runtime.misc.*;

// Base class for AST nodes
public class Declare extends Command {
    // type and value can be null.
    public Declare(String name, boolean mutable, Type type, Command value) 
    throws ParseCancellationException {
        this.name=name;
        this.mutable=mutable;
        this.type=type;
        this.value=value;
    }
    public final String name;
    public final boolean mutable;
    public final Type type;
    public final Command value;
    public Value run(Context ctx) throws ExecutionError { 
        Value value=null;
        if(this.value!=null) {
            value=this.value.run(ctx);
            if(this.type!=null && value!=null && value.type!=null)
                if(!this.type.equals(value.type)) 
                    throw new ExecutionError(String.format(
                        "Type '%s' of variable '%s' does not match type '%s' of initialization.",
                        this.type.toString(),
                        this.name,
                        value.type.toString()
                    ));
        } else value=new Value();
        ctx.variables.put(this.name, value);
        return value;
    }
}