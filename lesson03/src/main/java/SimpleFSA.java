package wcn.lexer;

class SimpleFSA<K,F> extends FSA<K,F,K> {
    SimpleFSA() {
        super(new KeyPredicateMapFactory());
    }
}