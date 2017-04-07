package wcn.parser;

import wcn.fsa.*;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.lang.Iterable;
import java.util.Iterator;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

import org.junit.Test;
import static org.junit.Assert.*;

public class CFGTest {
    @Test public void testFirstRec() {
        CFG<Character> cfg=new CFG();
        Nonterminal s=cfg.newNonterminal("S");
        cfg.appendRule(s,s,'a');
        cfg.appendRule(s);

        System.out.println("\nGrammar:");
        System.out.println(cfg);
        
        Map<Nonterminal, Set<List<Character>>> first=cfg.first(0);
        System.out.println("First(0):");
        System.out.println(first);
        assertEquals(new HashSet(Arrays.asList(Arrays.asList())), first.get(s));

        first=cfg.first(1);
        System.out.println("First(1):");
        System.out.println(first);
        assertEquals(new HashSet(Arrays.asList(Arrays.asList(),Arrays.asList('a'))), first.get(s));

        first=cfg.first(2);
        System.out.println("First(2):");
        System.out.println(first);
        assertEquals(new HashSet(
            Arrays.asList(Arrays.asList(),
            Arrays.asList('a'),
            Arrays.asList('a','a')
            )), first.get(s));
    };

    @Test public void testFirst() {
        CFG<Character> cfg=new CFG();
        Nonterminal s=cfg.newNonterminal("S");
        Nonterminal a=cfg.newNonterminal("A");
        cfg.appendRule(s,a,'a');
        cfg.appendRule(s);
        cfg.appendRule(a,'b');
        cfg.appendRule(a,a,'c');
        cfg.appendRule(a,'d',a);

        System.out.println("Grammar:");
        System.out.println(cfg);
        // <s>* ::= <a>a | <empty>
        // <a> ::= <a>c | d<a> | b

        // zero length prefixes
        Map<Nonterminal, Set<List<Character>>> first=cfg.first(0);
        System.out.println("First(0):");
        System.out.println(first);
        assertEquals(new HashSet(Arrays.asList(s,a)), first.keySet());
        assertEquals(new HashSet(Arrays.asList(Arrays.asList())), first.get(s));
        assertEquals(new HashSet(Arrays.asList(Arrays.asList())), first.get(a));

        // length one prefixes
        first=cfg.first(1);
        System.out.println("First(1):");
        System.out.println(first);
        assertEquals(new HashSet(Arrays.asList(s,a)), first.keySet());
        assertEquals(new HashSet(Arrays.asList(Arrays.asList('b'),Arrays.asList('d'))), first.get(a));
        assertEquals(new HashSet(Arrays.asList(Arrays.asList(),Arrays.asList('b'),Arrays.asList('d'))), 
            first.get(s));

        // length two prefixes
        first=cfg.first(2);
        System.out.println("First(2):");
        System.out.println(first);
        assertEquals(new HashSet(Arrays.asList(s,a)), first.keySet());
        assertEquals(new HashSet(Arrays.asList(Arrays.asList('b'),Arrays.asList('b','c'),Arrays.asList('d','b')
            ,Arrays.asList('d','d'))), first.get(a));
        assertEquals(new HashSet(Arrays.asList(
            Arrays.asList('b','c'),
            Arrays.asList('d','b'),
            Arrays.asList('d','d'),
            Arrays.asList(),
            Arrays.asList('b','a'))),  first.get(s));
    }

    public<T> Set<Rule<T>> match(IDFA<Lookahead<T>,Rule<T>> dfa, int depth, Iterable<T> string) {
        // Подготавливаем depth символов для предпросмотра
        List<T> lookahead=new ArrayList();
        Iterator<T> stream=string.iterator();
        for(int n=0; n<depth && stream.hasNext(); n++)
            lookahead.add(stream.next());
        // Запускаем автомат
        State state=dfa.initialState();
        // Пока не попадем в остановочное состояние, повторяем.
        while(true) {
            // Сдвигаем предпросмотр.
            if(stream.hasNext()) lookahead.add(stream.next());
            else {
                // Если символов для предпросмотра не хватает, 
                // то переходим к проверке маркеров.
                if(lookahead.size()<=depth) break;
            }
            // Извлекаем следующий символ 
            Term<T> symbol=new Term(lookahead.remove(0));
            // Делаем переход.
            Lookahead<T> arrow=new Lookahead(symbol,lookahead);
            System.out.print(String.format("From %s by %s",state,arrow));
            state=dfa.makeTransition(state, arrow);
            System.out.println(String.format(" to %s",state));
            // Если такого перехода нет, то строка не из языка.
            if(state==null) return null;
        };
        return dfa.getMarkers(state);
    }
    @Test public void testGenerateLR() {
        CFG<Character> cfg=new CFG();
        Nonterminal s=cfg.newNonterminal("s");
        Nonterminal a=cfg.newNonterminal("a");
        RHS<Character> s1=cfg.appendRule(s,a,'a');
        RHS<Character> s2=cfg.appendRule(s);
        RHS<Character> a1=cfg.appendRule(a,'b');
        RHS<Character> a2=cfg.appendRule(a,a,'c');
        RHS<Character> a3=cfg.appendRule(a,'d',a);

        System.out.println("Grammar:");
        System.out.println(cfg);
        // <s>* ::= <a>a | <empty>
        // <a> ::= <a>c | d<a> | b
        
        Map<Nonterminal, Set<List<Character>>> first=cfg.first(0);
        System.out.println("First(0):");
        System.out.println(first);

        FSA<Lookahead<Character>, Rule<Character>, Lookahead<Character>> fsa=cfg.generateLR(0);
        System.out.println("\nLR(0):");
        System.out.println(fsa);

        IDFA<Lookahead<Character>, Rule<Character>> dfa0=cfg.generateLRdet(0);
        System.out.println("LR(0):");
        System.out.println(dfa0);

        first=cfg.first(1);
        System.out.println("First(1):");
        System.out.println(first);

        fsa=cfg.generateLR(1);
        System.out.println("\nLR(1):");
        System.out.println(fsa);

        IDFA<Lookahead<Character>, Rule<Character>> dfa1=cfg.generateLRdet(1);
        System.out.println("LR(1):");
        System.out.println(dfa1);

        List<Character> string=Arrays.asList();
        Rule<Character> rule=new Rule(s2, Arrays.asList()); rule.position=rule.symbols.size();
        assertEquals(new HashSet(Arrays.asList(rule)), this.match(dfa0, 0, string));
        assertEquals(new HashSet(Arrays.asList(rule)), this.match(dfa1, 1, string));

        rule=new Rule(a1, Arrays.asList()); rule.position=rule.symbols.size();
        assertEquals(new HashSet(Arrays.asList(rule)), this.match(dfa0, 0, Arrays.asList('b')));
        rule=new Rule(a1, Arrays.asList('a')); rule.position=rule.symbols.size();
        assertEquals(new HashSet(Arrays.asList(rule)), this.match(dfa1, 1, Arrays.asList('b','a')));

        assertEquals(null, this.match(dfa0, 0, Arrays.asList('c')));
        assertEquals(null, this.match(dfa1, 1, Arrays.asList('c','a')));

        rule=new Rule(a1, Arrays.asList()); rule.position=rule.symbols.size();
        assertEquals(new HashSet(Arrays.asList(rule)), this.match(dfa0, 0, Arrays.asList('d','b')));
        rule=new Rule(a1, Arrays.asList('c')); rule.position=rule.symbols.size();
        assertEquals(new HashSet(Arrays.asList(rule)), this.match(dfa1, 1, Arrays.asList('d','b','c')));
    }
}