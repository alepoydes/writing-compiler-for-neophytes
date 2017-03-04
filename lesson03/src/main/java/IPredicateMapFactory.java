package wcn.lexer;

public interface IPredicateMapFactory<P,K,V> {
    IPredicateMap<P,K,V> make();
}