package wcn.terminal;

import java.util.Collection;

public interface ICharSet<T, SELF extends ICharSet<T, SELF>> {
    /**
     * Возвращает пересечение множеств символов.
     * Если пересечение пусто, то возращается null.
     */
    SELF intersect(SELF other);
    /**
     * Возвращает разность множеств this-other, которая может состоять
     * из нескольких кусков.
     */
    Collection<SELF> subtract(SELF other);
}