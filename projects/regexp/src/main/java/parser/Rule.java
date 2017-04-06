package wcn.parser;

import java.util.function.Function;
import java.util.List;

/**
 * Хранит правую часть грамматического правила вместе
 * с семантическим действием.
 */

public class Rule<T> extends RHS<T> {
    public Rule(Function<Object[], Object> action, Nonterminal lhs, List<Term<T>> symbols, 
    int position, List<T> lookahead) {
        super(action, lhs, symbols);
        this.lookahead=lookahead;
        this.position=position;
    }
    /** Конструктор создающий состояние без прочитанных символов. */
    public Rule(RHS rhs, List<T> lookahead) {
        this(rhs.action, rhs.lhs, rhs.symbols, 0, lookahead);
    }
    /** Конструктор станции, т.е. состояния без правой части правила. */
    public Rule(Nonterminal lhs, List<T> lookahead) {
        this(null, lhs, null, 0, lookahead);
    }
    @Override public String toString() { 
        StringBuilder sb=new StringBuilder();
        sb.append(this.lhs.toString());
        if(this.symbols!=null) {
            sb.append("↼");
            if(this.symbols.isEmpty()) sb.append("ε");
            else for(int i=0; i<this.symbols.size(); i++) {
                if(i==this.position) sb.append("★");
                sb.append(this.symbols.get(i));
            };
            if(this.position>=this.symbols.size()) sb.append("★");
        };
        if(this.lookahead.size()>0) {
            sb.append("⌈");
            for(Object item: this.lookahead) sb.append(item==null?"#":item.toString());
            sb.append("⌉");
        };
        //sb.append(String.format(" %d",this.hashCode()));
        return sb.toString(); 
    };
    @Override public int hashCode() { 
        int hash=this.lhs.hashCode();
        for(T item: this.lookahead) if(item!=null) hash=hash^item.hashCode();
        if(this.symbols!=null) {
            for(Term<T> item: this.symbols) hash=hash^item.hashCode();
            hash+=this.position; 
        };
        return hash;
    }
    @Override public boolean equals(Object obj) { 
        if(obj==null) return false;
        if(this.getClass()!=obj.getClass()) return false;
        Rule rule=(Rule)obj;
        if(this.lookahead.size()!=rule.lookahead.size()) { return false; };
        for(int n=0; n<this.lookahead.size(); n++) 
            if(!this.lookahead.get(n).equals(rule.lookahead.get(n))) { return false; };
        if(this.lhs!=rule.lhs) { return false; };
        if(this.symbols==null) { return rule.symbols==null; };
        if(this.symbols.size()!=rule.symbols.size()) { return false; };
        for(int n=0; n<this.symbols.size(); n++) 
            if(!this.symbols.get(n).equals(rule.symbols.get(n))) { return false; };
        if(this.position!=rule.position) return false;
        return true;
     };
    public List<T> lookahead;
    public int position;
}