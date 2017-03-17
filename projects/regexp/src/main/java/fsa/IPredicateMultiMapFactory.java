package wcn.fsa;

public interface IPredicateMultiMapFactory<P,K,V> {
    IPredicateMultiMap<P,K,V> make();
}