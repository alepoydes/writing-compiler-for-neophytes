package wcn.parser;

/**
 * Хранит символ расширенного алфавита, т.е. либо терминал T,
 * либо нетерминал Nonterminal.
 */
public class Term<T> {
    protected Object data;
    public Term(Nonterminal data) {
        this.data=data;
    }
    public Term(T data) {
        this.data=data;
    }
    public boolean isTerminal() {
        return !(this.data instanceof Nonterminal);
    }
    public T getTerminal() {
        return (T)this.data;
    }
    public Nonterminal getNonterminal() {
        return (Nonterminal)this.data;
    }
    @Override public String toString() { return this.data.toString(); };
    @Override public int hashCode() { return this.data.hashCode(); }
    @Override public boolean equals(Object obj) { 
        if(obj==null) return false; 
        if(this.getClass()!=obj.getClass()) return false; 
        Term<T> term=(Term<T>)obj;
        if(!this.data.equals(term.data)) return false; 
        return true;
    } 
}