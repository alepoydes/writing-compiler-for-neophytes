package wcn.lexer;

/**
 * Интерфейс конечного автомата.
 * Состояния автомата всегда кодируются целыми числами.
 * Переходы параметризуются типом T.
 * Остановочные состояния помечены метками типа F.
 */
public interface IFSA<T, F> {
    /**
     * Переводит автомат в стартовое состояние.
     */
    void reset();
    /**
     * Делает переход из текущего состояния по метке label, при успехе возвращает true.
     * Если такой переход не допускается, то возвращается false,
     * автомат остается в том же состоянии.
     */
    boolean makeTransition(T label);
    /**
     * Возвращает все маркеры остановочного состояния для текущего состояния.
     * Если ни одного маркера на возвращено, то состояние не остановочное.
     */
    Iterable<F> getMarkers();
    /**
     * Проверяет, удовлетворяет ли строка string автомату.
     */
    default boolean match(Iterable<T> string) {
        this.reset();
        for(T label: string) 
            if(!this.makeTransition(label)) return false;
        return this.getMarkers().iterator().hasNext();
    };
}
