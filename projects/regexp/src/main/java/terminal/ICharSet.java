package wcn.terminal;

public interface ICharSet<SELF extends ICharSet<SELF>> {
    /**
     * Возвращает пересечение множеств символов.
     * Если пересечение пусто, то возращается null.
     */
    SELF intersection(SELF other);
}