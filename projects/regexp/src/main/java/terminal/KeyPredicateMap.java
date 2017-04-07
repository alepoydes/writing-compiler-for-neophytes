package wcn.terminal; 

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap;

/**
 * Реализация IPredicateMap, добавляющая только один ключ одновременно. 
 * В этом случае предикат есть равенство ключу.
 */
public class KeyPredicateMap<K extends ICharSet<K,K>,V> implements IPredicateMap<K,K,V,KeyPredicateMap<K,V>> {
    // Внутреннее хранилище для массива
    private HashMap<K,V> map;
    // конструктор пустого массива
    public KeyPredicateMap() {
        this.map=new HashMap();
    }
    public KeyPredicateMap<K,V> empty() {
        return new KeyPredicateMap();
    }
    // Реализация интерфейса
    public V get(K key) {
        return this.map.get(key);
    }
    public void put(K key, V value) throws IndexOutOfBoundsException {
        if(this.map.put(key, value)!=null) 
            throw new IndexOutOfBoundsException(String.format("Duplicate key '%s'", key));
    }
    public Iterable<Map.Entry<K,V>> entrySet() {
        return this.map.entrySet();
    }
    @Override public String toString() { 
        return this.map.toString();
    }
}