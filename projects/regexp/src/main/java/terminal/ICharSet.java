package wcn.terminal;

import java.util.ArrayList;
import java.util.Collection;

public interface ICharSet<T, SELF extends ICharSet<T, SELF>> {
    /**
     * Возвращает пересечение множеств символов.
     * Если пересечение пусто, то возращается null.
     */
    default SELF intersect(SELF other) {
        if(this.equals(other)) return (SELF)this; else return null;
    };
    /**
     * Возвращает разность множеств this-other, которая может состоять
     * из нескольких кусков.
     */
    default Collection<SELF> subtract(SELF other) {
        ArrayList<SELF> result=new ArrayList();
        if(!this.equals(other)) result.add((SELF)this);
        return result;
    };
}