package rustless.command;

import rustless.*;
import rustless.type.*;

import org.antlr.v4.runtime.misc.*;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

// Base class for AST nodes
public class Call extends Command {
    public Call(String name, Command... arguments) 
    throws ParseCancellationException {
        this(name, Arrays.asList(arguments));
    }
    public Call(String name, List<Command> arguments) 
    throws ParseCancellationException {
        this.name=name;
        this.arguments=arguments;
    }
    public final String name;
    public final List<Command> arguments;
    public Value run(Context ctx) throws ExecutionError { 
        List<Value> arguments=new ArrayList();
        for(Command cmd: this.arguments) arguments.add(cmd.run(ctx));
        Value value=ctx.call(this.name, arguments);
        return value;
    }
}