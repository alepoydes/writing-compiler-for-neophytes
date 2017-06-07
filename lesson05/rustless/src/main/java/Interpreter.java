package rustless;

import rustless.ast.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Interpreter {
    /** 
     * Import rust module with given name. 
     * At the moment, the module should be located in the working directory.
     */
    public void include(String name) {
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
            ParseTree tree = parser.module(); // begin parsing at module rule
            System.out.println(tree.toStringTree(parser)); // print LISP-style tree
        } catch(java.io.IOException e) {
            System.out.format("Failed to read '%s'\n", name);
        };
    }
    /** 
     * Read-eval-print loop.
     * Only <expr> are understood.
     */
    public void repl() {
        try {
            System.out.format("Welcome to Rust REPL.\n");
            // create a CharStream that reads from standard input
            CharStream input = CharStreams.fromStream(System.in);
            // create a lexer that feeds off of input CharStream
            RustLexer lexer = new RustLexer(input);
            // create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // create a parser that feeds off the tokens buffer
            RustParser parser = new RustParser(tokens);
            while(true) {
                System.out.format("> ");
                ParseTree tree = parser.expr(); // begin parsing at expr rule
                System.out.format("\n%s\n", tree.toStringTree(parser)); // print LISP-style tree
            }
        } catch(java.io.IOException e) {
            System.out.format("IO Error\n");
        };
    }
    public static void main(String[] args) throws java.io.IOException {
        Interpreter interpreter=new Interpreter();
        // Loading all sources provided in command line
        for(String name: args) interpreter.include(name);
        // Starting read-eval-print loop 
        interpreter.repl();        
    }
}
