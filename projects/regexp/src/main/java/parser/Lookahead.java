package wcn.parser;

import wcn.terminal.*;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;

/** 
 * Класс для представления переходов в LR автомате с предпросмотром.
 */
public class Lookahead<T> implements ICharSet<Term<T>,Lookahead<T>> {
    protected Term<T> symbol;
    protected List<T> lookahead;
    public Lookahead(Term<T> symbol, List<T> lookahead) {
        this.symbol=symbol;
        this.lookahead=lookahead;
    };
    public Lookahead(T symbol, List<T> lookahead) {
        this(new Term(symbol), lookahead);
    };
    public Lookahead(Nonterminal symbol, List<T> lookahead) {
        this(new Term(symbol), lookahead);
    };
    public Lookahead(Term<T> symbol, T... lookahead) {
        this(symbol, Arrays.asList(lookahead));
    };
    public Lookahead(T symbol, T... lookahead) {
        this(new Term(symbol), lookahead);
    };
    public Lookahead(Nonterminal symbol, T... lookahead) {
        this(new Term(symbol), lookahead);
    };

    // Интерфейс ICharSet
    public Lookahead<T> intersect(Lookahead<T> other) {
        return (this.equals(other))?this:null;
    }
    public Collection<Lookahead<T>> subtract(Lookahead<T> other) {
        Collection<Lookahead<T>> result=new ArrayList();
        if(!this.equals(other)) result.add(this);
        return result;
    }
    // методы Object
    @Override public String toString() { 
        StringBuilder sb=new StringBuilder();
        sb.append(this.symbol.toString());
        if(this.lookahead!=null && this.lookahead.size()>0) {
            sb.append("⌈");
            for(T t: this.lookahead) sb.append(t.toString());
            sb.append("⌉");
        };
        return sb.toString(); 
    };
    @Override public int hashCode() { 
        int hash=this.symbol.hashCode();
        for(T t: this.lookahead) hash+=t.hashCode();
        return hash; 
    }
    @Override public boolean equals(Object obj) { 
        if(obj==null) return false;
        if(this.getClass()!=obj.getClass()) return false;
        Lookahead<T> other=(Lookahead<T>)obj;
        if(this.lookahead.size()!=other.lookahead.size()) return false; 
        for(int n=0; n<this.lookahead.size(); n++) 
            if(!this.lookahead.get(n).equals(other.lookahead.get(n))) return false; 
        if(!this.symbol.equals(other.symbol)) return false;
        return true;
    } 
}