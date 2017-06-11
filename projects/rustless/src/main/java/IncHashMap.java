package rustless;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Partial implementation of Map collection 
 * that can create its editable copies in constant time.
 */
public class IncHashMap<K,V> {
    public IncHashMap() {
        this(null);
    }
    // Create copy of parent. 
    // The copy can be modified, keys can be appended and removed.
    // The content of the map is not cloned, 
    // hence modification of the values modifies all copies of the map.
    public IncHashMap(IncHashMap<K,V> parent) {
        this.parent=parent;
        this.container=new HashMap();
    }
    // All methods searching for keys are O(depth),
    // where depth is number of parents.
    public boolean containsKey(Object key) {
        if(this.container.containsKey(key)) return true;
        if(this.parent==null) return false;
        return this.parent.containsKey(key);
    }
    public V get(Object key) {
        return this.getOrDefault(key, null);
    }
    public V getOrDefault(Object key, V defaultValue) {
        V result=this.container.get(key);
        if(result!=null) return result;
        if(this.parent==null) return defaultValue;
        return this.parent.getOrDefault(key, defaultValue);
    }
    public boolean isEmpty() {
        if(!this.container.isEmpty()) return false;
        if(this.parent==null) return true;
        return this.parent.isEmpty();
    }
    public Set<K> keySet() {
        if(this.parent==null) return this.container.keySet();
        Set<K> result=this.parent.keySet();
        result.addAll(this.container.keySet());
        return result;
    }
    public V put(K key, V value) {
        V result=this.get(key);
        this.container.put(key, value);
        return result;
    }
    // putAll is faster than put, since it does not check parents.
    public void putAll(Map<? extends K,? extends V> m) {
        this.container.putAll(m);
    }
    public V putIfAbsent(K key, V value) {
        V result=this.get(key);
        if(result!=null) return result;
        return this.container.put(key, value);
    }
    public V remove(K key) {
        V result=this.get(key);
        this.container.put(key, null);
        return result;
    }
    protected IncHashMap<K,V> parent;
    protected HashMap<K,V> container;
}