package rustless.command;

import rustless.*;
import rustless.type.*;

import java.util.List;

import org.antlr.v4.runtime.misc.*;

// Block containing several commands
public class Block extends Command {
    // type and value can be null.
    public Block(List<Command> cmds) 
    throws ParseCancellationException {
        this.cmds=cmds;
    }
    public final List<Command> cmds;
    public Value run(Context ctx) throws ExecutionError { 
        Context newctx=new Context(ctx);
        Value value=null;
        for(Command cmd: this.cmds) value=cmd.run(newctx);
        return value;
    }
}