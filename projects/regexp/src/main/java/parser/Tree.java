package wcn.parser;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

/** 
 * Хранит дерево, ветви которого нумеруются типом T. 
 * Метки ветвей уникальны.
 * */
public class Tree<T> {
    protected Map<T,Tree<T>> children;
    public Tree() {
        this.children=new HashMap();
    }
    /** Добавляет потомка с меткой label. */
    public Tree<T> add(T label) {
        Tree<T> child=this.children.get(label);
        if(child==null) {
            child=new Tree();
            this.children.put(label, child);
        };
        return child;
    }
    /** Добавляет ветвь с метками labels. */
    public void addBranch(List<T> labels) {
        Tree<T> root=this;
        for(T label: labels) root=root.add(label);
        root.add(null);
    }
    @Override public String toString() { 
        return this.children.toString(); 
    };
}