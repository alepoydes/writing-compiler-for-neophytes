package wcn.regexp;

import wcn.terminal.*;
import wcn.parser.*;
import wcn.fsa.*;

import java.util.Arrays;

public class RegExp {
    protected static CFG<UChar> grammar;
    public static CFG<UChar> getGrammar() {
        if(RegExp.grammar==null) RegExp.grammar=RegExp.grammar(0);
        return RegExp.grammar;
    }
    public static CFG<UChar> grammar(Integer marker) {
        Combinators<UChar,Integer,UChar> c=new Combinators(new KeyPredicateMultiMap());
        /*
        <regexp> ::= <group>                { $$=$1 }
        <regexp> ::= <regexp> <bar> <group> { $$=Union($1, $3) }
        <group>  ::=                        { $$=Literal("") }
        <group>  ::= <group> <term>         { $$=Concatenate($1,$2) }
        <term>   ::= <term> +               { $$=Plus($1) }
        <term>   ::= <term> *               { $$=Star($1) }
        <term>   ::= <term> ?               { $$=Option($1) }
        <term>   ::= ( <regexp> )           { $$=$1 }
        <term>   ::= <literal>              { $$=Literal($1) }
        */
        CFG<UChar> cfg=new CFG();
        Nonterminal regexp=cfg.newNonterminal("regexp");
        Nonterminal group=cfg.newNonterminal("group");
        Nonterminal term=cfg.newNonterminal("term");
        Nonterminal literal=cfg.newNonterminal("literal");        
        UChar bar=new UChar('|');
        UChar plus=new UChar('+');
        UChar star=new UChar('*');
        UChar option=new UChar('?');
        UChar bra=new UChar('(');
        UChar ket=new UChar(')');
        cfg.appendRule((x)->x[0], regexp, group);
        cfg.appendRule((x)->c.union(Arrays.asList(
                (FSA<UChar,Integer,UChar>)x[0],
                (FSA<UChar,Integer,UChar>)x[2]
            )), regexp, regexp, bar, group);
        cfg.appendRule((x)->c.literal(Arrays.asList(),marker), group);
        cfg.appendRule((x)->c.concatenation(Arrays.asList(
                (FSA<UChar,Integer,UChar>)x[0],
                (FSA<UChar,Integer,UChar>)x[1]
            )), group, group, term);
        cfg.appendRule((x)->c.repeat(
                (FSA<UChar,Integer,UChar>)x[0]
            ), term, term, plus);
        cfg.appendRule((x)->c.star(
                (FSA<UChar,Integer,UChar>)x[0],
                marker
            ), term, term, star);
        cfg.appendRule((x)->c.option(
                (FSA<UChar,Integer,UChar>)x[0],
                marker
            ), term, term, option);
        cfg.appendRule((x)->x[1], term, bra, regexp, ket);
        cfg.appendRule((x)->c.literal(Arrays.asList((UChar)x[0]),marker), term, literal);
        for(char l='a'; l<='b'; l++) cfg.appendRule((x)->x[0], literal, new UChar(l));
        return cfg;
    }

    protected static LR<UChar> regexpParser;
    public static LR<UChar> getRegExpParser() {
        if(RegExp.regexpParser==null) {
            CFG<UChar> cfg=RegExp.getGrammar();
            RegExp.regexpParser=new LR(cfg, 1, false);
        };
        return new LR(RegExp.regexpParser);
    }
    public static IDFA<UChar, Integer> compile(String regexp) throws ParserError {
        return RegExp.compile(regexp, false);
    }
    public static IDFA<UChar, Integer> compile(String regexp, boolean debug) throws ParserError {
        if(debug) System.err.println(String.format("Compiling regexp: %s", regexp));
        LR<UChar> parser=RegExp.getRegExpParser();
        FSA<UChar,Integer,UChar> fsa=(FSA<UChar,Integer,UChar>)parser.parse(
            new CopyingIterator(UChar.asList(regexp).iterator()),
            debug);
        return new DFA(new KeyPredicateMap(), fsa, debug);
    }

    protected IDFA<UChar, Integer> automaton;
    public RegExp(String regexp) throws ParserError {
        this.automaton=RegExp.compile(regexp);
    }
    public boolean match(String string) {
        return this.automaton.match(UChar.asList(string));
    }
}