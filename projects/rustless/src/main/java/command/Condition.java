package rustless.command;

import rustless.*;
import rustless.type.*;

import org.antlr.v4.runtime.misc.*;

public class Condition extends Command {
    public Condition(Command condition, Command positive, Command negative) 
    throws ParseCancellationException {
        this.condition=condition;
        this.positive=positive;
        this.negative=negative;
    }
    public final Command condition;
    public final Command positive;
    public final Command negative;
    public Value run(Context ctx) throws ExecutionError { 
        Value f=this.condition.run(ctx);
        if(f==null) throw new ExecutionError("Condition is not an expression");
        if(f.toBoolean()) return this.positive.run(ctx);
        else if(this.negative!=null) return this.negative.run(ctx);
        else return null;
    }
}