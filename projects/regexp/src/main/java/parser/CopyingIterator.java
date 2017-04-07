package wcn.parser;

import java.util.Iterator;
import java.lang.Iterable;

public class CopyingIterator<T> implements Iterator<Pair<T,Object>> {
    public Iterator<T> iterator;
    public CopyingIterator(Iterable<T> obj) {
        this(obj.iterator());
    }
    public CopyingIterator(Iterator<T> iterator) {
        this.iterator=iterator;
    };
    public boolean hasNext() { 
        return this.iterator.hasNext();
    }
    public Pair<T,Object> next() {
        T value=this.iterator.next();
        return new Pair(value, value);
    }
}