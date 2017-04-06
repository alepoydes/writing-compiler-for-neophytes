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
    /** Возвращает число символов в правой части правила. */
    public int size() { return this.symbols.size(); }
    /** 
     * Применяет семантическое действие. 
     * Если действие равно null, то применяется действие по умолчанию,
     * которое конструирует текстовое представление дерева разбора.
    */
    public Object apply(List<Object> data) {
        if(this.action!=null) 
            return this.action.apply(data.toArray());
        StringBuilder sb=new StringBuilder();
        boolean first=true;
        for(int n=0; n<this.size(); n++) {
            if(first) first=false;
            else sb.append(",");
            String term=this.symbols.get(n).toString();
            String value=data.get(n).toString();
            if(!term.equals(value)) sb.append(String.format("%s(%s)",term,value));
            else sb.append(term);
        };
        return sb.toString();
    };
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