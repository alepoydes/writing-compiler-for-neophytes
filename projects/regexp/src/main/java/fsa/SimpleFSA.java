package wcn.fsa;

import wcn.terminal.*;

class SimpleFSA<K,F> extends FSA<K,F,K> {
    SimpleFSA() {
        super(new KeyPredicateMultiMap());
    }
}