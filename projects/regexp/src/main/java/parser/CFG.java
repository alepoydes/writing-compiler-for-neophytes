package wcn.parser;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.lang.StringBuilder;

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
    public Nonterminal newNonterminal(String name) {
        Nonterminal nt=new Nonterminal(name);
        this.rules.put(nt, new HashSet());
        return nt;
    }
    public void appendRule(Nonterminal left, Object... right) {
        this.rules.get(left).add(Arrays.asList(right));
    }
    @Override public String toString() { 
        StringBuilder sb=new StringBuilder();
        for(Map.Entry<Nonterminal, Set<List<Object>>> entry: this.rules.entrySet()) {
            sb.append(entry.getKey());
            sb.append(" ::= ");
            boolean first=true;
            for(List<Object> rhs: entry.getValue()) {
                if(first) first=false; else sb.append(" | ");
                for(Object item: rhs) sb.append(item.toString());
            };
            sb.append("\n");
        };
        return sb.toString(); 
    };
    /** Реализация */
    protected Map<Nonterminal, Set<List<Object>>> rules;
}