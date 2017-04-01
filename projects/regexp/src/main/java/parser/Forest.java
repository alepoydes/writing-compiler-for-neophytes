package wcn.parser;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;

public class Forest<T> {
    public Map<T,Tree<T>> forest;
    public Forest() {
        this.forest=new HashMap();
    }
    public void put(T label, Tree<T> tree) {
        this.forest.put(label, tree);
    }
    /** 
     * Возвращает depth первых уровней всех ветвей дерева tree.
     * Если дерево содержит метки, добавленные через Forest.put,
     * то они заменяются обходом дерева, добавленного в лес.
     */
    public Set<List<T>> listBranches(Tree<T> tree, int depth, T current) {
        return this.listBranches(tree, depth, current, new HashSet());
    }
    public Set<List<T>> listBranches(Tree<T> tree, int depth, T current, Set<T> visited) {
        //System.out.println(String.format("Depth: %d, Tree: %s, Visited: %s",depth,tree,visited));
        Set<List<T>> result=new HashSet();
        // Если достигнута максимальная глубина, либо листья, 
        // то возвращаем множество из единственной пустой ветви.
        if(depth==0 || tree==null || tree.children.isEmpty()) {
            result.add(new ArrayList());
            //System.out.println(String.format("Depth: %d, Tree: %s, Branches: %s",depth,tree,result));
            return result;
        };
        Set<T> newVisited=new HashSet(visited);
        if(current!=null) newVisited.add(current);
        // Перебираем всех потомков.
        for(Map.Entry<T,Tree<T>> entry: tree.children.entrySet()) {
            // Если такой переход уже делался, то игнорируем его, 
            // чтобы не зациклиться
            T key=entry.getKey();
            if(visited.contains(key)) continue;
            // Проверяем, есть ли метка перехода в перечне деревьев.
            Tree<T> substitution=this.forest.get(key);
            Set<List<T>> prefixes;
            // Если есть, то все ветки дерева из леса используем как префиксы
            // для ветвей дерева tree
            if(substitution==null) {
                prefixes=new HashSet();
                if(key!=null) prefixes.add(Arrays.asList(key));
                else prefixes.add(Arrays.asList());
            } else {
                prefixes=this.listBranches(substitution, depth, key, newVisited);
            };
            // Вызываем реккурентно генерацию списка ветвей для потомка
            Set<List<T>> sublist=this.listBranches(entry.getValue(), depth-1, null, new HashSet());
            // Присоединяем все префиксы 
            for(List<T> prefix: prefixes) {
                // ко всем ветвея потомка
                for(List<T> subbranch: sublist) {
                    List<T> branch=new ArrayList(prefix);
                    for(int n=0; n<depth-prefix.size() && n<subbranch.size(); n++)
                        branch.add(subbranch.get(n));
                    //System.out.println(String.format("New branch: %s",branch));            
                    result.add(branch);
                };
            };
        };
        //System.out.println(String.format("Depth: %d, Tree: %s, Branches: %s",depth,tree,result));
        return result;
    }
    @Override public String toString() { 
        return this.forest.toString(); 
    };
}