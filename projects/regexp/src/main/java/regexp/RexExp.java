/**
 * Аналог RexExp, но использует лексер, поэтому работает быстрее 
 * и понимает больше символов.
 */
package wcn.regexp;

import wcn.terminal.*;
import wcn.parser.*;
import wcn.fsa.*;

import java.util.Iterator;
import java.util.Arrays;

public class RexExp {
    protected static CFG<Lexer.Symbol> grammar;
    protected static CFG<Lexer.Symbol> getGrammar() {
        if(RexExp.grammar==null) RexExp.grammar=RexExp.grammar(0);
        return RexExp.grammar;
    }
    protected static CFG<Lexer.Symbol> grammar(Integer marker) {
        Combinators<UChar,Integer,UChar> c=new Combinators(new KeyPredicateMultiMap());
        /*
        <RexExp> ::= <group>                { $$=$1 }
        <RexExp> ::= <RexExp> <bar> <group> { $$=Union($1, $3) }
        <group>  ::=                        { $$=Literal("") }
        <group>  ::= <group> <term>         { $$=Concatenate($1,$2) }
        <term>   ::= <term> +               { $$=Plus($1) }
        <term>   ::= <term> *               { $$=Star($1) }
        <term>   ::= <term> ?               { $$=Option($1) }
        <term>   ::= ( <RexExp> )           { $$=$1 }
        <term>   ::= <literal>              { $$=Literal($1) }
        */
        CFG<Lexer.Symbol> cfg=new CFG();
        Nonterminal RexExp=cfg.newNonterminal("RexExp");
        Nonterminal group=cfg.newNonterminal("group");
        Nonterminal term=cfg.newNonterminal("term");
        Nonterminal literal=cfg.newNonterminal("literal");        
        cfg.appendRule((x)->x[0], RexExp, group);
        cfg.appendRule((x)->c.union(Arrays.asList(
                (FSA<UChar,Integer,UChar>)x[0],
                (FSA<UChar,Integer,UChar>)x[2]
            )), RexExp, RexExp, Lexer.Symbol.Bar, group);
        cfg.appendRule((x)->c.literal(Arrays.asList(),marker), group);
        cfg.appendRule((x)->c.concatenation(Arrays.asList(
                (FSA<UChar,Integer,UChar>)x[0],
                (FSA<UChar,Integer,UChar>)x[1]
            )), group, group, term);
        cfg.appendRule((x)->c.repeat(
                (FSA<UChar,Integer,UChar>)x[0]
            ), term, term, Lexer.Symbol.Plus);
        cfg.appendRule((x)->c.star(
                (FSA<UChar,Integer,UChar>)x[0],
                marker
            ), term, term, Lexer.Symbol.Star);
        cfg.appendRule((x)->c.option(
                (FSA<UChar,Integer,UChar>)x[0],
                marker
            ), term, term, Lexer.Symbol.Option);
        cfg.appendRule((x)->x[1], term, Lexer.Symbol.Open, RexExp, Lexer.Symbol.Close);
        cfg.appendRule((x)->c.literal(Arrays.asList((UChar)x[0]),marker), term, Lexer.Symbol.Literal);
        return cfg;
    }

    protected static LR<Lexer.Symbol> RexExpParser;
    protected static LR<Lexer.Symbol> getRexExpParser() {
        if(RexExp.RexExpParser==null) {
            CFG<Lexer.Symbol> cfg=RexExp.getGrammar();
            RexExp.RexExpParser=new LR(cfg, 1, false);
        };
        return new LR(RexExp.RexExpParser);
    }
    protected static IDFA<UChar, Integer> compile(String regexp) throws ParserError {
        return RexExp.compile(regexp, false);
    }
    protected static IDFA<UChar, Integer> compile(String regexp, boolean debug) throws ParserError {
        if(debug) System.err.println(String.format("Compiling RexExp: %s", regexp));
        LR<Lexer.Symbol> parser=RexExp.getRexExpParser();
        Iterator<UChar> input=UChar.asList(regexp).iterator();
        FSA<UChar,Integer,UChar> fsa=(FSA<UChar,Integer,UChar>)parser.parse(new Lexer(input),debug);
        // return determined automaton
        return new DFA(new KeyPredicateMap(), fsa, debug);
    }

    protected IDFA<UChar, Integer> automaton;
    public RexExp(String regexp) throws ParserError {
        this.automaton=RexExp.compile(regexp);
    }
    public boolean match(String string) {
        return this.automaton.match(UChar.asList(string));
    }
}