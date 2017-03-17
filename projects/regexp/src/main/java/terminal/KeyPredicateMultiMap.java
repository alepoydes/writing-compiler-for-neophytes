package wcn.terminal; 

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap;

/**
 * Реализация IPredicateMultiMap, добавляющая только один ключ
 * одновременно. В этом случае предикат есть равенство ключу.
 */
public class KeyPredicateMultiMap<K,V> implements IPredicateMultiMap<K,K,V,KeyPredicateMultiMap<K,V>> {
    // Внутреннее хранилище для массива
    private HashMap<K,HashSet<V>> map;
    // конструктор пустого массива
    public KeyPredicateMultiMap() {
        this.map=new HashMap();
    }
    public KeyPredicateMultiMap<K,V> empty() {
        return new KeyPredicateMultiMap();
    }
    // Реализация интерфейса
    public Iterable<V> get(K key) {
        HashSet<V> set=map.get(key);
        if(set==null) return new HashSet();
        return set;
    }
    public void put(K key, V value) {
        HashSet<V> set=map.get(key);
        if(set==null) {
            set=new HashSet();
            map.put(key, set);
        };
        set.add(value);
    }
    public Iterable<Map.Entry<K,V>> entrySet() {
        HashSet<Map.Entry<K,V>> result=new HashSet();
        for(Map.Entry<K,HashSet<V>> entry: this.map.entrySet())
            for(V value: entry.getValue())
                result.add(new AbstractMap.SimpleImmutableEntry(entry.getKey(), value));
        return result;
    }
}