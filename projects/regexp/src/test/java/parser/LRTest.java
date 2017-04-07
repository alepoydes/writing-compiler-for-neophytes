package wcn.parser;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.lang.Iterable;
import java.util.Iterator;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

import org.junit.Before; 
import org.junit.Test;
import static org.junit.Assert.*;

public class LRTest {
    protected CFG<Character> cfg;
    protected Nonterminal s;
    protected Nonterminal a;
    @Before public void init() {
        this.cfg=new CFG();
        this.s=cfg.newNonterminal("s");
        this.a=cfg.newNonterminal("a");
        this.cfg.appendRule(this.s,this.a,'a');
        this.cfg.appendRule(this.s);
        this.cfg.appendRule(this.a,'b');
        this.cfg.appendRule(this.a,this.a,'c');
        this.cfg.appendRule(this.a,'d',this.a);
        //System.out.println("Grammar:");
        //System.out.println(this.cfg);
        // <s>* ::= <a>a | <empty>
        // <a> ::= <a>c | d<a> | b
    }

    @Test public void testBoundary0() throws ParserError {
        LR<Character> parser=new LR(this.cfg, 0);
        // Если не давать ни одного символа, то это тоже самое, что разбирать пустую строку.
        assertTrue(parser.isEOL());
        // Требуется один символ для заполнения буфера.
        assertTrue(parser.isHungry());
        parser.feed('b');
        assertFalse(parser.isHungry());
        // Теперь парсер знает, что это не конец строки
        assertFalse(parser.isEOL());
        // Сдвигаем символ
        parser.shift(new Lookahead('b'));
        // Теперь парсер думает, что достигнут конец строки.
        assertTrue(parser.isEOL());
        // и хочет новых символов
        assertTrue(parser.isHungry());
    }

    @Test public void testBoundary1() throws ParserError {
        LR<Character> parser=new LR(this.cfg, 1);
        // Если не давать ни одного символа, то это тоже самое, что разбирать пустую строку.
        assertTrue(parser.isEOL());
        // Требуется два символа
        assertTrue(parser.isHungry());
        parser.feed('d');
        assertTrue(parser.isHungry());
        // Теперь парсер знает, что это не конец строки
        assertFalse(parser.isEOL());
        // Добавляем еще один символ для предпросмотра
        parser.feed('b');
        assertFalse(parser.isHungry());
        assertFalse(parser.isEOL());
        // Сдвигаем символ
        parser.shift(new Lookahead('d','b'));
        // У парсера еще остался не сдвинутый символ
        assertFalse(parser.isEOL());
        // Но символов для предпросмотра не хватает
        assertTrue(parser.isHungry());
    }

    @Test(expected=ParserError.class) public void testWrongShift() throws ParserError {
        LR<Character> parser=new LR(this.cfg, 0);
        assertNull(parser.canShift());
        parser.shift(new Lookahead('b'));
    }

    @Test public void testReduction() throws ParserError {
        LR<Character> parser=new LR(this.cfg, 0);
        System.out.println("Automaton:");
        System.out.println(parser.automaton);
        
        parser.feed('d');
        parser.feed('b');
        parser.feed('a');

        System.out.println("Parsing:");
        System.out.println(parser);
        Set<Rule<Character>> reductions=parser.listReductions();
        assertEquals(new HashSet(Arrays.asList(
            new Rule(null, this.s, Arrays.asList(), 0, Arrays.asList())
            )), reductions);
        Lookahead<Character> shift=parser.canShift();
        assertEquals(new Lookahead('d'), shift);
        parser.shift(shift);

        System.out.println(parser);
        reductions=parser.listReductions();
        assertEquals(new HashSet(), reductions);
        shift=parser.canShift();
        assertEquals(new Lookahead('b'), shift);
        parser.shift(shift);

        System.out.println(parser);
        reductions=parser.listReductions();
        assertEquals(new HashSet(Arrays.asList(
            new Rule(null, this.a, Arrays.asList(new Term('b')), 1, Arrays.asList())            
            )), reductions);
        shift=parser.canShift();
        assertNull(shift);
        assertTrue(parser.reduce(reductions.iterator().next()));
        
        System.out.println(parser);
        reductions=parser.listReductions();
        assertEquals(new HashSet(Arrays.asList(
            )), reductions);
        shift=parser.canShift();
        assertEquals(new Lookahead(this.a), shift);
        parser.shift(shift);
        
        System.out.println(parser);
        reductions=parser.listReductions();
        assertEquals(new HashSet(Arrays.asList(
            new Rule(null, this.a, Arrays.asList(new Term('d'), new Term(this.a)), 2, Arrays.asList())            
            )), reductions);
        shift=parser.canShift();
        assertNull(shift);
        assertTrue(parser.reduce(reductions.iterator().next()));
        
        System.out.println(parser);
        reductions=parser.listReductions();
        assertEquals(new HashSet(Arrays.asList(
            new Rule(null, this.s, Arrays.asList(), 0, Arrays.asList())
            )), reductions);
        shift=parser.canShift();
        assertEquals(new Lookahead(this.a), shift);
        parser.shift(shift);
        
        System.out.println(parser);
        reductions=parser.listReductions();
        assertEquals(new HashSet(Arrays.asList(
            )), reductions);
        shift=parser.canShift();
        assertEquals(new Lookahead('a'), shift);
        parser.shift(shift);
        
        System.out.println(parser);
        reductions=parser.listReductions();
        assertEquals(new HashSet(Arrays.asList(
            new Rule(null, this.s, Arrays.asList(new Term(this.a), new Term('a')), 2, Arrays.asList())            
            )), reductions);
        shift=parser.canShift();
        assertNull(shift);
        assertTrue(parser.reduce(reductions.iterator().next()));
        
        System.out.println(parser);
        reductions=parser.listReductions();
        assertEquals(new HashSet(Arrays.asList(
            new Rule(null, this.s, Arrays.asList(), 0, Arrays.asList())
            )), reductions);
        shift=parser.canShift();
        assertNull(shift);
        assertFalse(parser.isEOL());

        Object result=parser.finish();
        assertNotNull(result);
    }

    @Test public void testParse1() throws ParserError {
        System.err.println("\ntestParse1");
        LR<Character> parser=new LR(this.cfg, 0);
        List<Character> string=Arrays.asList('d','b','a');
        assertNotNull(parser.parse(new CopyingIterator(string),true));
        string=Arrays.asList('b','a');
        assertNotNull(parser.parse(new CopyingIterator(string),true));
        string=Arrays.asList();
        assertNotNull(parser.parse(new CopyingIterator(string),true));
        string=Arrays.asList('d','b','c','a');
        assertNotNull(parser.parse(new CopyingIterator(string),true));
    }

    @Test(expected=ParserError.class) public void testParse2() throws ParserError {
        System.err.println("\ntestParse2");
        LR<Character> parser=new LR(this.cfg, 0);
        List<Character> string=Arrays.asList('d','b');
        parser.parse(new CopyingIterator(string),true);
    }

    @Test(expected=ParserError.class) public void testParse3() throws ParserError {
        System.err.println("\ntestParse3");
        LR<Character> parser=new LR(this.cfg, 0);
        List<Character> string=Arrays.asList('a','a');
        parser.parse(new CopyingIterator(string),true);
    }

    @Test public void testParseLR1() throws ParserError {
        System.err.println("\ntestParseLR1");
        LR<Character> parser=new LR(this.cfg, 1);
        List<Character> string=Arrays.asList('d','b','a');
        assertNotNull(parser.parse(new CopyingIterator(string),true));
        string=Arrays.asList('b','a');
        assertNotNull(parser.parse(new CopyingIterator(string),true));
        string=Arrays.asList();
        assertNotNull(parser.parse(new CopyingIterator(string),true));
        string=Arrays.asList('d','b','c','a');
        assertNotNull(parser.parse(new CopyingIterator(string),true));
    }

    @Test public void testParseLR2() throws ParserError {
        System.err.println("\ntestParseLR2");
        LR<Character> parser=new LR(this.cfg, 2);
        List<Character> string=Arrays.asList('d','b','a');
        assertNotNull(parser.parse(new CopyingIterator(string),true));
        string=Arrays.asList('b','a');
        assertNotNull(parser.parse(new CopyingIterator(string),true));
        string=Arrays.asList();
        assertNotNull(parser.parse(new CopyingIterator(string),true));
        string=Arrays.asList('d','b','c','a');
        assertNotNull(parser.parse(new CopyingIterator(string),true));
    }
}