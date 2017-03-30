package wcn.fsa;

import java.util.Set;

/**
 * Интерфейс детерминированного конечного автомата.
 * Состояния автомата всегда кодируются целыми числами.
 * Переходы параметризуются типом T.
 * Остановочные состояния помечены метками типа F.
 */
public interface IDFA<T, F> {
    /**
     * Возвращает начальное состояние автомата
     */
    State initialState();
    /**
     * Делает переход из состояния activeState по метке label, 
     * при успехе возвращает новое состояние.
     * Если такой переход не допускается, то возвращается null.
     */
    State makeTransition(State activeState, T label);
    /**
     * Возвращает все маркеры остановочного состояния для текущего состояния.
     * Если ни одного маркера на возвращено, то состояние не остановочное.
     */
    Set<F> getMarkers(State activeState);
    /**
     * Проверяет, удовлетворяет ли строка string автомату.
     */
    default boolean match(Iterable<T> string) {
        State state=this.initialState();
        for(T label: string) {
            state=this.makeTransition(state, label);
            if(state==null) return false;
        };
        return !this.getMarkers(state).isEmpty();
    };
    /**
     * Возвращает маркер остановочного состояния для текущего состояния.
     * Если состояние не остановочное, то возвращает null.
     */
    default F getMarker(State activeState) {
        Set<F> markers=this.getMarkers(activeState);
        if(markers.isEmpty()) return null;
        return markers.iterator().next();
    };
}
