package wcn.fsa;

import java.util.Set;

/**
 * Интерфейс конечного автомата.
 * Состояния автомата всегда кодируются целыми числами.
 * Переходы параметризуются типом T.
 * Остановочные состояния помечены метками типа F.
 */
public interface IFSA<T, F> {
    /**
     * Возвращает начальное состояние автомата
     */
    Set<State> initialState();
    /**
     * Делает переход из состояния activeState по метке label, 
     * при успехе возвращает новое состояние.
     * Если такой переход не допускается, то возвращается null.
     */
    Set<State> makeTransition(Set<State> activeState, T label);
    /**
     * Возвращает все маркеры остановочного состояния для текущего состояния.
     * Если ни одного маркера на возвращено, то состояние не остановочное.
     */
    Set<F> getMarkers(Set<State> activeState);
    /**
     * Проверяет, удовлетворяет ли строка string автомату.
     */
    default boolean match(Iterable<T> string) {
        Set<State> state=this.initialState();
        for(T label: string) {
            state=this.makeTransition(state, label);
            if(state==null) return false;
        };
        return !this.getMarkers(state).isEmpty();
    };
}
