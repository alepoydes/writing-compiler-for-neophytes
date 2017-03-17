package wcn.fsa; 

/**
 * Конструктор объектов типа KeyPreducateMap.
 */
public class KeyPredicateMultiMapFactory<K,V> implements IPredicateMultiMapFactory<K,K,V> {
    public KeyPredicateMultiMap<K,V> make() {
        return new KeyPredicateMultiMap();
    }
}