package wcn.parser;

import wcn.fsa.*;

import java.util.List;
import java.util.Set;
import java.util.Iterator;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Парсер LR(k) с предпросмотром на k символов.
 */
public class LR<T> {
    protected IDFA<Lookahead<T>, Rule<T>> automaton; // LR автомат
    protected int depth; // глубина предпросмотра
    protected List<Term<T>> stack; // стек уже обработанных терминалов
    protected List<Object> results; // результаты семинтических действий для символов их stack
    protected List<State> states; // Текущее состояние автомата
    protected List<T> buffer; // Буфер символов для предпросмотра
    protected List<Object> resultBuffer; // Данные для buffer
    protected Nonterminal startSymbol; // стартовый символ = цель сверток

    /** Копирует состояние автомата. */
    public LR(LR<T> other) {
        this.automaton=other.automaton;
        this.depth=other.depth;
        this.startSymbol=other.startSymbol;
        this.stack=new ArrayList(other.stack);
        this.results=new ArrayList(other.results);
        this.states=new ArrayList(other.states);
        this.buffer=new ArrayList(other.buffer);
        this.resultBuffer=new ArrayList(other.resultBuffer);
    };
    /** Создает автомат по данной грамматике. */
    public LR(CFG<T> grammar, int depth) {
        this(grammar, depth, false);
    }
    public LR(CFG<T> grammar, int depth, boolean debug) {
        this.automaton=grammar.generateLRdet(depth, debug);
        if(debug) {
            System.err.println("LR automaton:");
            System.err.println(this.automaton);
        };
        this.depth=depth;
        this.startSymbol=grammar.startSymbol;
        this.reset();
    };
    /** Перезапускает автомат */
    public void reset() {
        this.stack=new ArrayList();
        this.buffer=new ArrayList();
        this.resultBuffer=new ArrayList();
        this.results=new ArrayList();
        this.states=new ArrayList();
        this.states.add(this.automaton.initialState());
    }
    /*
     * Возвращает true, если в парсер требуется передать новые символы
     * из строки с помощью feed.
     * Если возвращает false, то безопасно вызывать shift.
     * Если достигнут конец строки, то hungry не нужно вызывать, можно 
     * вызывать сразу shift.
     */
    public boolean isHungry() {
        return this.stack.size()+this.buffer.size()-this.states.size()-this.depth<0;
    }
    /** 
     * Добавляет символ в буфер.
     * Должно вызываться перед вызовом shift.
     */
    public void feed(T symbol) {
        this.buffer.add(symbol);
        this.resultBuffer.add(symbol);
    };
    public void feed(Pair<T,Object> symbol) {
        this.feed(symbol.key, symbol.value);
    }
    public void feed(T symbol, Object result) {
        this.buffer.add(symbol);
        this.resultBuffer.add(result);
    };
    /**
     * Возвращает true, если достигнут конец строки, больше сдвигов делать нельзя.
     * Вызывать нужно после feed.
     */
    public boolean isEOL() {
        return this.buffer.size()+this.stack.size()-this.states.size()<0;
    };
    /** 
     * Возвращает символ с предпросмотром, по которому можно сделать сдвиг. 
     * Если сдвиг невозможен, то возвращает null.
    */
    public Lookahead<T> canShift() throws IllegalStateException  {
        int delta=this.stack.size()-this.states.size();
        if(delta>0) throw new IllegalStateException("Unprocessed nonterminals on stack");
        else if(delta==0) {
            // Нужно сделать сдвиг на нетерминал со стека
            Lookahead<T> arrow=new Lookahead(this.stack.get(this.stack.size()-1), new ArrayList(this.buffer.subList(0,Math.min(this.buffer.size(),depth))));
            if(this.automaton.makeTransition(this.states.get(this.states.size()-1), arrow)!=null) return arrow; 
            else return null;
        } else if(delta==-1) {
            // Пора сдвигаться на новый терминал.
            if(this.buffer.size()<1) return null;
            Lookahead<T> arrow=new Lookahead(new Term(this.buffer.get(0)), new ArrayList(this.buffer.subList(1,Math.min(this.buffer.size(),depth+1))));
            if(this.automaton.makeTransition(this.states.get(this.states.size()-1), arrow)!=null) return arrow; 
            else return null;
        } else throw new IllegalStateException("Unbinded state");
    }
    /** 
     * Делает сдвиг на один символ. 
     * Символы должны быть предоставлены заранее с помощью feed.
     * Если сдвиг невозможен, то будет выброшено исключение.
     * Проверить возможность сдвига можно с помощью canShift.
     */
    public void shift(Lookahead<T> arrow) throws ParserError {
        if(this.stack.size()<this.states.size()) {
            if(this.buffer.size()<1) throw new ParserError("Empty stack");
            T term=this.buffer.remove(0);
            this.stack.add(new Term(term));
            this.results.add(this.resultBuffer.remove(0));
        };
        State newState=this.automaton.makeTransition(this.states.get(this.states.size()-1), arrow);
        if(newState==null) throw new ParserError("Wrong transition");
        this.states.add(newState);
    };
    /** Возвращает перечень всех возможных на этом шаге сверток. */
    public Set<Rule<T>> listReductions() {
        Set<Rule<T>> all=this.automaton.getMarkers(this.states.get(this.states.size()-1));
        Set<Rule<T>> filtered=new HashSet();
        outerloop: for(Rule<T> rule: all) {
            if(rule.lookahead.size()!=Math.min(this.buffer.size(),this.depth)) continue;
            for(int n=0; n<rule.lookahead.size(); n++) 
                if(!rule.lookahead.get(n).equals(this.buffer.get(n))) continue outerloop; 
            filtered.add(rule);
        };
        return filtered;
    };
    /** 
     * Выполняет указанную свертку, при успехе возвращает true.
     * Если на стеке нет нужных для свертки аргументов, то возвращает false
     * и не изменяет стек.
     */
    public boolean reduce(Rule<T> rule) {
        // Проверяем совпадение правой части правила с данными на стеке
        // Если rule было получено из listReduction, то это избыточно.
        if(rule.size()>this.stack.size()) return false;
        for(int n=0; n<rule.size(); n++) 
            if(!rule.symbols.get(n).equals(this.stack.get(this.stack.size()-rule.size()+n)))
                return false;
        // Вычисляем семантическое действие
        int newSize=this.results.size()-rule.size();
        List<Object> args=new ArrayList();
        for(int n=0; n<rule.size(); n++) {
            args.add(this.results.remove(newSize));
            this.stack.remove(newSize);
            this.states.remove(newSize+1);
        };
        Object result=rule.apply(args);
        // Помещаем на стек результат свертки.
        this.stack.add(new Term(rule.lhs));
        this.results.add(result);
        return true;
    };
    /**
     * Проверяет находимся ли мы на вершине стека, т.е. сдвинуты ли все нетерминалы.
     */
    public boolean isOnTop() {
        return this.stack.size()==this.states.size()-1;
    };
    /** 
     * Проверяет, удалось ли свернуть весь ввод в стартовый символ.
     * При успехе возвращает результат семантических действий, иначе null.     * 
     * Должно вызываться в конце строки, когда isEOL возвращает true.
     * Если вызвано не в конце строки, то выбрасывается исключение. 
     */
    public Object finish() throws ParserError {
        if(this.buffer.size()>0) throw new ParserError("Unexpected symbol");
        if(this.stack.size()!=1) throw new ParserError("Unexpected EOL");
        Term<T> value=this.stack.get(0);
        if(value.isTerminal()) throw new ParserError("Unexpected EOL");
        if(!value.getNonterminal().equals(this.startSymbol)) throw new ParserError("Unexpected EOL");
        return this.results.get(0);
    };
    /**
     * Метод для парсинга строки string.
     * Возвращает результат семантического действия или
     * выбрасывает ParserError при синтаксической ошибке.
     */
    public Object parse(Iterator<Pair<T,Object>> string) throws ParserError {
        return this.parse(string, false);
    }
    public Object parse(Iterator<Pair<T,Object>> string, boolean debug) throws ParserError {
        // Перезапускаем автомат
        this.reset();
        // Первоначально наполняем буфер
        while(this.isHungry() && string.hasNext()) this.feed(string.next());
        // Пытаемся сделать сдвиги/свертки, пока вся строка не будет прочитана.
        while(true) {
            // Проверяем, можно ли сделать сдвиг и свертки
            Lookahead<T> arrow=this.canShift();
            Set<Rule<T>> reductions=this.isOnTop()?this.listReductions():new HashSet();
            // Проверяем конфликты и выполняем действия
            if(arrow!=null) {
                if(debug && !reductions.isEmpty()) {
                    System.err.println(this);
                    System.err.println(String.format("Shift-reduce conflict: shift to %s, reduce %s"
                        ,arrow,reductions));
                };
                this.shift(arrow);
            } else {
                if(debug) System.err.println(this);
                if(reductions.isEmpty()) {
                    // Дальше ничего сделать нельзя.
                    // Проверяем, достигнут ли конец строки
                    if(string.hasNext()) {
                        // Раз нет, то строка не лежит в языке
                        System.err.println(String.format("Unexpected symbol"));
                        throw new ParserError(String.format("Unexpected symbol"));
                    };
                    // Если символов больше нет, то пытаемся завершить работу
                    Object result=this.finish();
                    if(debug) System.err.println(String.format("Parsed:%s: %s",this.stack.get(0),result));
                    return result;
                };
                if(debug && reductions.size()>1) 
                    System.err.println(String.format("Reduce-reduce conflict: %s",reductions));
                this.reduce(reductions.iterator().next());
            };
            // Пополняем буфер, сколько необходимо
            while(this.isHungry() && string.hasNext()) this.feed(string.next());
        }
    }

    @Override public String toString() { 
        StringBuilder sb=new StringBuilder();
        for(int n=0; n<Math.max(this.stack.size(),this.states.size()); n++) {
            if(n<this.states.size()) sb.append(String.format("@%s ",this.states.get(n)));
            //if(n==this.states.size()-1) sb.append("† ");
            if(n<this.stack.size()) {
                String term=this.stack.get(n).toString();
                String result=this.results.get(n).toString();
                if(!term.equals(result)) sb.append(String.format("%s[%s] ",term,result));
                else sb.append(String.format("%s ",term));
            };
        };
        sb.append("† ");
        for(T t: this.buffer) sb.append(String.format("%s ",t));
        return sb.toString();
    }
}