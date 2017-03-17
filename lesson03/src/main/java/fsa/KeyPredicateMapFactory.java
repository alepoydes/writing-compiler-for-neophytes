package wcn.fsa; 

/**
 * Конструктор объектов типа KeyPreducateMap.
 */
public class KeyPredicateMapFactory<K,V> implements IPredicateMapFactory<K,K,V> {
    public KeyPredicateMap<K,V> make() {
        return new KeyPredicateMap();
    }
}