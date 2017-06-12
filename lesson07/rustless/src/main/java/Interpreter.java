package rustless;

import rustless.ast.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Interpreter {
    /** 
     * Import rust module with given name. 
     * At the moment, the module should be located in the working directory.
     */
    public void include(String name, Context ctx) {
        System.out.format("Loading '%s'\n", name);
        try {
            // create a CharStream that reads from standard input
            CharStream input = CharStreams.fromFileName(name);
            // create a lexer that feeds off of input CharStream
            RustLexer lexer = new RustLexer(input);
            // create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // create a parser that feeds off the tokens buffer
            RustParser parser = new RustParser(tokens);
            ParseTree tree = parser.repl(ctx); // begin parsing at module rule
        } catch(java.io.IOException e) {
            System.out.format("Failed to read '%s'\n", name);
        };
    }
    /** 
     * Read-eval-print loop.
     * Only <expr> are understood.
     */
    public void repl(Context ctx) {
        try {
            System.out.format("Welcome to Rust read-evaluate-print loop. Type 'quit' to break the loop.\n");
            BufferedReader buf=new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            while(true) {
                System.out.format("> ");
                String str=buf.readLine();
                if(str==null) break;
                // create a CharStream that reads from standard input
                CharStream input = CharStreams.fromString(str);
                // create a lexer that feeds off of input CharStream
                RustLexer lexer = new RustLexer(input);
                // create a buffer of tokens pulled from the lexer
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                // create a parser that feeds off the tokens buffer
                RustParser parser = new RustParser(tokens);
                //parser.setTrace(true);
                try {
                    RustParser.ReplContext replctx = parser.repl(ctx); // begin parsing at repl rule
                } catch(ParseCancellationException e) {
                    System.out.format("%s\n",e.getMessage());
                };
            }
        } catch(java.io.IOException e) {
            System.out.format("IO Error\n");
        };
    }
    public static void main(String[] args) throws java.io.IOException {
        Interpreter interpreter=new Interpreter();
        Context ctx = new Context();
        // Loading all sources provided in command line
        for(String name: args) interpreter.include(name, ctx);
        // Starting read-eval-print loop 
        interpreter.repl(ctx);
    }
}
