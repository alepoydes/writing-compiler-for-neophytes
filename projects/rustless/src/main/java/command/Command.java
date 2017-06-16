package rustless.command;

import rustless.*;

// Base class for AST nodes
public class Command {
    public Value run(Context ctx) throws ExecutionError { 
        return null;
    }
}