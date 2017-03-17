package wcn.fsa;

class SimpleFSA<K,F> extends FSA<K,F,K> {
    SimpleFSA() {
        super(new KeyPredicateMapFactory());
    }
}