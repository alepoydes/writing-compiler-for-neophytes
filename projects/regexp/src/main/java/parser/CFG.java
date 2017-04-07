package wcn.parser;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.lang.StringBuilder;
import java.util.function.Function;

import wcn.fsa.*;
import wcn.terminal.*;

/**
 * Класс CFG<T> содержит описание контекстно-свободной грамматики.
 * В качестве терминалов используются символы T.
 * Нетерминалы можно создать только методом newNonterminal,
 * тип нетерминала Nonterminal.
 */
public class CFG<T> {
    public CFG() {
        this.rules=new HashMap();
    }
    /** Добавляет в алфавит новый нетерминал. Имя name используется только для отладки. */
    public Nonterminal newNonterminal(String name) {
        Nonterminal nt=new Nonterminal(name);
        if(this.startSymbol==null) this.startSymbol=nt;
        this.rules.put(nt, new HashSet());
        return nt;
    }
    /** 
     * Добавляет в грамматику правило вида:
     * <left> ::= <right[0]><right[1]>... { action }
     */
    public RHS appendRule(Function<Object[],Object> action, Nonterminal left, Object... right) {
        List<Term<T>> args=new ArrayList();
        // Проверяем корректность аргументов.
        for(Object item: right) {
            if(item instanceof Nonterminal) {
                if(!this.rules.containsKey((Nonterminal)item)) 
                    throw(new IllegalArgumentException(String.format("Undefined non-terminal '%s'",item)));
                args.add(new Term((Nonterminal)item));
            } else if(item instanceof Term) {
                args.add((Term<T>)item);
            } else args.add(new Term((T)item));
        };
        // Добавляем правило.
        RHS<T> rule=new RHS(action, left, args);
        this.rules.get(left).add(rule);
        return rule;
    }
    public RHS<T> appendRule(Nonterminal left, Object... right) {
        return this.appendRule(null, left, right);
    }
    public Forest<Term<T>> forest() {
        // Строим лес, где деревьями будут нетерминалы
        Forest<Term<T>> forest=new Forest();
        for(Map.Entry<Nonterminal,Set<RHS<T>>> entry: this.rules.entrySet()) {
            // Создаем дерево для нетерминала
            Tree<Term<T>> tree=new Tree();
            // и добавляем в него все способы вывести нетерминал.
            for(RHS<T> rhs: entry.getValue()) tree.addBranch(rhs.symbols);
            // Само дерево сохраняем в лесу
            forest.put(new Term(entry.getKey()), tree);
        };
        return forest;
    }
    /** 
     * Вычисляет префиксы длины depth для правой части rhs правила,
     * опираясь на ранее найденные префиксы first меньшей длины.
     * Символ конца строки записывается как null.
     */
    public static<T> Set<List<T>> getPrefixes(RHS<T> rhs, Map<Nonterminal, Set<List<T>>> first, int depth) {
        Set<List<T>> result=new HashSet();
        Set<List<T>> expandable=new HashSet(); expandable.add(new ArrayList());
        for(Term<T> symbol: rhs.symbols) {
            Set<List<T>> newExpandable=new HashSet();
            for(List<T> word: expandable) {
                if(symbol.isTerminal()) {
                    // Терминал просто добавляется в конец всех префиксов
                    word.add(symbol.getTerminal());
                    newExpandable.add(word);
                } else {
                    // Для нетерминала перебираем все префиксы и пытаемся их присоединить
                    for(List<T> prefix: first.get(symbol.getNonterminal())) {
                        List<T> newPrefix=new ArrayList(word);
                        newPrefix.addAll(prefix);
                        if(prefix.size()<1 || prefix.get(prefix.size()-1)!=null) {
                            // Префиск известен только частично, поэтому
                            // дальше расширять слово не удастся, переносим его в result.
                            result.add(newPrefix.subList(0,depth));
                        } else {
                            // Добавляем префикс, будем его продолжать далее. Символ null не храним.
                            newPrefix.remove(newPrefix.size()-1);
                            newExpandable.add(newPrefix);
                        };
                    };
                };
            };
            // Длинные префиксы копируем в результат, остальные в expandable
            expandable=new HashSet();
            for(List<T> prefix: newExpandable) {
                if(prefix.size()>=depth) result.add(prefix.subList(0,depth));
                else expandable.add(prefix);
            };
        };
        // Переносим все незавершенные префиксы в результат, добавляя конец строки в конец.
        for(List<T> word: expandable) {
            word.add(null);
            result.add(word);
        };
        return result;
    };
    /** Возвращает все цепочки из depth первых символов каждого нетерминала. */
    public Map<Nonterminal, Set<List<T>>> first(int depth) {
        // Для каждого дерева/нетерминала создаем перечень префиксов.
        // null в хранимых словах будет обозначать конец строки.
        Map<Nonterminal, Set<List<T>>> prefixes=new HashMap();
        // Первоначально инциализируем его префиксами нулевой длины
        for(Nonterminal nt: this.rules.keySet()) 
            prefixes.put(nt, new HashSet(Arrays.asList()));
        int modified;
        do {
            modified=0;
            // Вычисляем все префиксы для всех правил, пользуясь уже найденными правилами.
            for(Map.Entry<Nonterminal, Set<RHS<T>>> entry: this.rules.entrySet()) {
                Set<List<T>> set=prefixes.get(entry.getKey());
                for(RHS<T> rhs: entry.getValue()) {
                    // Считаем все префиксы для правой части
                    Set<List<T>> prefs=CFG.getPrefixes(rhs, prefixes, depth);
                    // Сохраняем префиксы
                    for(List<T> prefix: prefs) 
                        if(set.add(prefix)) modified++;
                };
            };
        } while(modified>0);
        // Убираем из префиксов не полные префиксы слова и символы null.
        Map<Nonterminal, Set<List<T>>> result=new HashMap();
        for(Map.Entry<Nonterminal, Set<List<T>>> entry: prefixes.entrySet()) {
            Set<List<T>> set=new HashSet();
            for(List<T> word: entry.getValue()) {
                if(word.size()>0 && word.get(word.size()-1)==null) {
                    word.remove(word.size()-1);
                } else if(word.size()!=depth) continue;
                set.add(word);
            };
            result.put(entry.getKey(), set);
        };
        return result;        
    }
    /** 
     * Возвращает префикс длины не больше depth для цепочки string из терминалов.
     * first должно содержать первые depth символов для всех нетерминалов.
     */
    public static<T> Set<List<T>> expandTerms(List<Term<T>> string, Map<Nonterminal, Set<List<T>>> first, int depth) {
        // Массив с префиксами требуемой длины.
        HashSet<List<T>> result=new HashSet();
        // Массив с префиксами, которые еще нужно растить.
        Set<List<T>> active=new HashSet();
        if(depth>0) active.add(new ArrayList());
        else result.add(new ArrayList());
        // Последовательно добавляем символы из строки
        for(Term<T> symbol: string) {
            Set<List<T>> next=new HashSet();
            if(symbol.isTerminal()) {
                // терминал просто добавляем ко всем префиксам
                T t=symbol.getTerminal();
                for(List<T> a: active) {
                    a.add(t);
                    if(a.size()<depth) next.add(a); else result.add(a);
                };
            } else {
                // перебираем все префиксы для нетерминала
                for(List<T> f: first.get(symbol.getNonterminal())) {
                    // и добавляем их к уже накопленным префиксам
                    for(List<T> a: active) {
                        List<T> p=new ArrayList(a);
                        if(p.size()+f.size()<depth) {
                            // Если префикс недостаточно длинный, оставляем его в работе.
                            p.addAll(f);
                            next.add(p);
                        } else {
                            // Если префикс нужной длины, то сохраняем его в результат.
                            p.addAll(f.subList(0,depth-p.size()));
                            result.add(p);
                        };
                    };
                };
            };
            active=next;
            // Нет нужды продолжать, если префикс имеет нужную длину
            if(active.isEmpty()) break;
        };
        result.addAll(active);
        return result;
    };
    /** Возвращает автомат LR(depth), где depth - число символов для предпросмотра. */
    public IDFA<Lookahead<T>, Rule<T>> generateLRdet(int depth) {
        return this.generateLRdet(depth, false);
    }
    public IDFA<Lookahead<T>, Rule<T>> generateLRdet(int depth, boolean debug) {
        FSA<Lookahead<T>, Rule<T>, Lookahead<T>> fsa=generateLR(depth, debug);
        if(debug) {
            System.err.println("Nondeterminate automaton:");
            System.err.println(fsa);
        };
        return new DFA(new KeyPredicateMap(), fsa, debug);
    }
    public FSA<Lookahead<T>, Rule<T>, Lookahead<T>> generateLR(int depth) {
        return this.generateLR(depth, false);
    }
    public FSA<Lookahead<T>, Rule<T>, Lookahead<T>> generateLR(int depth, boolean debug) {
        if(this.startSymbol==null) return null;
        FSA<Lookahead<T>,Rule<T>,Lookahead<T>> fsa=new FSA(new KeyPredicateMultiMap());
        // Перечень станций = всех нетерминалов с предпросмотрами (правая часть правила отброшена = null)
        Map<Rule<T>, State> stations=new HashMap();
        // Перечень всех необработанных станций
        Deque<Rule<T>> active=new ArrayDeque();
        // Стартовое состояние: стартовый символ, после стартового симаола идет конец строки.
        List<T> eol=new ArrayList();
        Rule<T> start_rule=new Rule(this.startSymbol, eol);
        State start_state=fsa.newState(start_rule.toString());
        stations.put(start_rule, start_state);
        active.add(start_rule);
        // Создаем перечень стартовых симоволов
        Map<Nonterminal, Set<List<T>>> first=this.first(depth);
        if(debug) {
            System.err.println("FIRST:");
            for(Map.Entry<Nonterminal, Set<List<T>>> entry: first.entrySet()) {
                System.err.print(String.format("%s:",entry.getKey()));
                for(List<T> string: entry.getValue()) {
                    System.err.print(" ");
                    for(T c: string) System.err.print(c);
                };
                System.err.println();
            };
        };
        // Обходим все станции, пока они не закончатся. В процессе обхода могут добавляться новые станции.
        Rule<T> station;
        while((station=active.poll())!=null) {
            State station_state=stations.get(station);
            // Добавляем цепочки переходов для каждого правила из грамматики с
            // правой частью, соответствующей станции.
            for(RHS<T> rhs: this.rules.get(station.lhs)) {
                // Первое правило цепочки
                Rule<T> head=new Rule(rhs, station.lookahead);
                State head_state=fsa.newState(head.toString());
                // Добавляем переход из станции в цепочку
                fsa.newTransition(station_state, head_state, null);
                // Продвигаемся по цепочке
                while(head.position<head.symbols.size()) {
                    // Ищем, что идет после текущего символа
                    List<Term<T>> tail=new ArrayList(head.symbols.subList(head.position+1,head.symbols.size()));
                    for(T t: head.lookahead) tail.add(new Term(t));
                    Set<List<T>> following=CFG.expandTerms(tail, first, depth);
                    // Делаем переход на станцию (предсказание), если далее идет нетерминал
                    Term<T> current=head.symbols.get(head.position);
                    if(!current.isTerminal()) {
                        Nonterminal lhs=current.getNonterminal();
                        // Добавляем переходы в станции
                        for(List<T> lookahead: following) {
                            // Ищем, встречалась ли уже станция.
                            Rule<T> next_station=new Rule(lhs, lookahead);
                            State next_state=stations.get(next_station);
                            if(next_state==null) {
                                next_state=fsa.newState(next_station.toString());
                                stations.put(next_station, next_state);
                                active.add(next_station);
                            };
                            // Добавляем переход
                            fsa.newTransition(head_state, next_state, null); 
                        };
                    };
                    // Сдвигаем текущее положение
                    head.position++;
                    State next_state=fsa.newState(head.toString());
                    // Добавляем переход далее по цепочке
                    for(List<T> lookahead: following) {
                        Lookahead<T> arrow=new Lookahead(current, lookahead);
                        fsa.newTransition(head_state, next_state, arrow);
                    };
                    head_state=next_state;
                };
                // Последнее состояния является остановочным, добавляем маркер
                fsa.markState(head_state, head);
            };
        };
        return fsa;
    }
    /** Возвращает грамматику в БНФ. Порядок произвольный. Действия не выводятся. */
    @Override public String toString() { 
        StringBuilder sb=new StringBuilder();
        for(Map.Entry<Nonterminal, Set<RHS<T>>> entry: this.rules.entrySet()) {
            for(RHS rhs: entry.getValue()) {
                if(entry.getKey()==this.startSymbol) sb.append("*");
                sb.append(rhs);
                sb.append("\n");
            };
        };
        return sb.toString(); 
    };
    /** Реализация */
    protected Nonterminal startSymbol;
    protected Map<Nonterminal, Set<RHS<T>>> rules;
}