package wcn.parser;

import java.util.function.Function;
import java.util.List;

/**
 * Хранит правую часть грамматического правила вместе
 * с семантическим действием.
 */

public class RHS<T> {
    public RHS(Function<Object[], Object> action, Nonterminal lhs, List<Term<T>> symbols) {
        this.action=action;
        this.symbols=symbols;
        this.lhs=lhs;
    }
    @Override public String toString() { 
        StringBuilder sb=new StringBuilder();
        sb.append(lhs.toString());
        sb.append("↼");
        if(this.symbols.isEmpty()) sb.append("ε");
        else for(Term<T> item: this.symbols) sb.append(item.toString());
        return sb.toString(); 
    };
    public Function<Object[], Object> action;
    public List<Term<T>> symbols;
    public Nonterminal lhs;
}