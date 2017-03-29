package wcn.fsa;

import wcn.terminal.*;

public class Combinators<T,F,P> {
    /** Конструктор хранилищ, передаваемый создаваемым автоматам. */
    protected IPredicateMultiMap<P,T,State,?> factory;
    /** Конструктор просто передает все параметры конструктору автомата. */
    public<M extends IPredicateMultiMap<P,T,State,M>> Combinators(M factory) {
        this.factory=factory;
    }
    /**
     * Конструирует автомат, принимающий только строку string.
     * Остановочное состояние маркируется как marker.
     */
    public FSA<T,F,P> literal(Iterable<P> string, F marker) {
        FSA<T,F,P> automaton=new FSA(this.factory);
        State state=automaton.newState();
        for(P label: string) {
            State next=automaton.newState();
            automaton.newTransition(state, next, label);
            state=next;
        };
        automaton.markState(state, marker);
        return automaton;
    }
    /**
     * Создает автомат, приниающих любой из перечисленных символов.
     */
    public FSA<T,F,P> anyOf(Iterable<P> labels, F marker) {
        FSA<T,F,P> automaton=new FSA(this.factory);
        State start=automaton.newState();
        State end=automaton.newState();
        automaton.markState(end, marker);
        for(P label: labels)
            automaton.newTransition(start, end, label);
        return automaton;
    }
    /**
     * Конструирует объединение автоматов automatа,
     * т.е. создает автомат, принимающий всякую строку, 
     * которую принимает хотя бы один автомат.
     */
    public FSA<T,F,P> union(Iterable<FSA<T,F,P>> automata) {
        FSA<T,F,P> automaton=new FSA(this.factory);
        State first=automaton.newState();
        for(FSA<T,F,P> a: automata) {
            State state=automaton.add(a);
            automaton.newTransition(first, state, null);
        };
        return automaton;
    };
    /**
     * Строит конкатенацию автоматов, т.е. каждый последующий автомат применяется,
     * только когда предыдущий попадает в остановочное состояние.
     * Все маркеры отбрасываются, кроме маркеров последнего в цепи автомата.
     */
    public FSA<T,F,P> concatenation(Iterable<FSA<T,F,P>> automata) {
        FSA<T,F,P> automaton=new FSA(this.factory);
        for(FSA<T,F,P> a: automata) {
            Iterable<State> ends=automaton.getMarked();
            automaton.dropMarkers();
            State start=automaton.add(a);
            if(start!=null)
                for(State end: ends) automaton.newTransition(end, start, null);
        };
        return automaton;
    };
    /**
     * Строит зведочку Клейна от автомата, т.е. автомат может применятся
     * ноль и более раз.
     * Маркер marker вернется, если автомат не был применен ни разу.
     */
    public FSA<T,F,P> star(FSA<T,F,P> automaton, F marker) {
        FSA<T,F,P> result=new FSA(this.factory);
        State start=result.add(automaton);
        Iterable<State> ends=result.getMarked();
        for(State end: ends) result.newTransition(end, start, null);
        State newend=result.newState();
        result.newTransition(start, newend, null);
        result.markState(newend, marker);
        return result;
    };
    /**
     * Строит новый автомат, который повторяет automaton один и более раз.
     */
    public FSA<T,F,P> repeat(FSA<T,F,P> automaton) {
        FSA<T,F,P> result=new FSA(this.factory);
        State start=result.add(automaton);
        Iterable<State> ends=result.getMarked();
        for(State end: ends) result.newTransition(end, start, null);
        return result;
    };
    /**
     * Строит новый автомат, который выполняет старый ноль или один раз.
     * Маркер marker вернется, если автомат не был применен ни разу.
     */
    public FSA<T,F,P> option(FSA<T,F,P> automaton, F marker) {
        FSA<T,F,P> result=new FSA(this.factory);
        State start=result.add(automaton);
        State newend=result.newState();
        result.newTransition(start, newend, null);
        result.markState(newend, marker);
        return result;
    };
}