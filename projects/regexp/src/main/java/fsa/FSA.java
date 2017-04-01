package wcn.fsa;

import wcn.terminal.*;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Реализация недетерминированного конечного автомата.
 * Реализует интерфейс IFSA<T,F> для метод переходов типа T,
 * и маркерами остановочных состяний F.
 * Для переходов можно использовать множества значений,
 * хранимых в переменнй типа P.
 * Для хранения и поиска 
 */
public class FSA<T,F,P> implements IFSA<T,F> {
    // Реализация интерфейся IFSA
    public Set<State> initialState() {
        Set<State> activeStates=new HashSet();
        if(this.numberOfStates>0) activeStates.add(new State(0));
        this.doEpsilonTransition(activeStates);
        return activeStates;
    };
    /** 
     * Дополняет набор состояний states всеми состояниями, 
     * в которые можно попасть эпсилон-переходами.
     */
    protected void doEpsilonTransition(Set<State> states) {
        Set<State> suspects=states;
        while(!suspects.isEmpty()) {
            Set<State> nextSuspects=new HashSet();
            for(State from: suspects) 
                for(State to: this.transitions.get(from).get(null)) 
                    if(!states.contains(to)) nextSuspects.add(to);
            for(State state: nextSuspects) states.add(state);
            suspects=nextSuspects;
        };
    };
    public Set<State> makeTransition(Set<State> activeStates, T label) {
        HashSet<State> next=new HashSet(); // Следующий набор активных состояний
        // делаем переходы по символу
        for(State from: activeStates) 
            for(State to: this.transitions.get(from).get(label))
                next.add(to);
        // делаем эпсилон переходы
        this.doEpsilonTransition(next);
        // проверяем, был ли хоть один переход
        if(next.isEmpty()) return null;
        return next;
    };
    public Set<F> getMarkers(Set<State> activeStates) {
        HashSet<F> markers=new HashSet();
        for(State state: activeStates)
            for(F marker: this.markers.get(state)) 
                markers.add(marker);
        return markers;
    };
    // Оригинальные методы
    /**
     * Возвращает все переходы
     */
    public IPredicateMultiMap<P,T,State,?> getTransitions(Set<State> activeStates) {
        IPredicateMultiMap<P,T,State,?> result=this.factory.empty();
        for(State state: activeStates)
            result.mergeMap(this.transitions.get(state), (x) -> x);
        return result;
    }
    /**
     * Возвразает все остановчные состояния.
     */
    public Set<State> getMarked() {
        HashSet<State> result=new HashSet();
        for(Map.Entry<State,HashSet<F>> entry: this.markers.entrySet())
            if(!entry.getValue().isEmpty())
                result.add(entry.getKey());
        return result;
    };
    /**
     * Сбрасывает все пометки, делая все состояния не остановочными.
     */
    public void dropMarkers() {
        for(Map.Entry<State, HashSet<F>> entry: this.markers.entrySet())
            entry.getValue().clear();
    };
    /**
     * Конструктор, создающий автомат с единственным состоянием,
     * оно же стартовое, без переходов и без остановочных состояний.
     */
    public<M extends IPredicateMultiMap<P,T,State,M>> FSA(M factory) {
        this.factory=factory;
        this.transitions=new HashMap();
        this.markers=new HashMap();
        this.stateNames=new HashMap();
        numberOfStates=0;
    };
    /**
     * Создает новое состояние и возвращает его номер.
     */
    public State newState() {  
        return this.newState(String.format("%d",this.numberOfStates));
    };
    public State newState(String name) {  
        State state=new State(this.numberOfStates++);
        this.stateNames.put(state, name);
        this.transitions.put(state, this.factory.empty());
        this.markers.put(state, new HashSet());
        return state;
    };
    /**
     * Создает переход между состояниями по данному символу.
     * Метка label=null соответствует эпсилон переходу.
     * Добавление эпсилон перехода может изменить текущее состояние,
     * не забудьте вызвать doEpsilonTransitions после newTransition.
     */
    public void newTransition(State from, State to, P label) {
        this.transitions.get(from).put(label, to);
    };
    /**
     * Помечает состояние остановочным, маркируя его с помощью marker.
     */
    public void markState(State state, F mark) {
        this.markers.get(state).add(mark);
    };
    /**
     * Добавляет к автомату все состояния и переходы автомата automaton.
     * Возвращается состояние, в которое привратилось стартовое состояние
     * автомата automaton.
     */
    public State add(FSA<T,F,P> automaton) {
        // Если добавляемый автомат пустой, то ничего не делаем.
        if(automaton.numberOfStates==0) return null;
        // Добавляем все состояния
        State first=this.newState();
        for(int n=1; n<automaton.numberOfStates; n++) this.newState();
        // Добавляем переходы
        for(Map.Entry<State,IPredicateMultiMap<P,T,State,?>> entry: automaton.transitions.entrySet())
            this.transitions.get(new State(entry.getKey().getId()+first.getId()))
                .mergeMap(entry.getValue(), (state) -> new State(state.getId()+first.getId()));
        // Добавляем маркеры
        for(Map.Entry<State, HashSet<F>> entry: automaton.markers.entrySet()) {
            HashSet<F> target=this.markers.get(new State(entry.getKey().getId()+first.getId()));
            for(F marker: entry.getValue()) target.add(marker);
        };
        return first;
    };
    public IPredicateMultiMap<P,T,State,?> getFactory() {
        return this.factory;
    }
    /** Методы Object */
    @Override public String toString() { 
        StringBuilder result=new StringBuilder();
        for(State state: this.transitions.keySet()) {
            result.append(String.format("#%d(",state.getId()));
            result.append(this.stateNames.get(state));
            result.append(")");
            boolean isFirst=true;
            for(F marker: this.markers.get(state))
                if(isFirst) { result.append(String.format(":%s", marker)); isFirst=false; }
                else result.append(String.format(",%s", marker));
            result.append(":");
            for(Map.Entry<P,State> entry: this.transitions.get(state).entrySet()) {
                P symbol=entry.getKey();
                if(symbol!=null) result.append(String.format(" %s→%d", symbol, entry.getValue().getId()));
                else result.append(String.format(" ε→%d", entry.getValue().getId()));
            };
            result.append("\n");
        };
        return result.toString();
    };
    // Детали реализации
    /**
     * Число использованных состояний.
     * Автомат обязан иметь все состояния с номерами от 0 до numberOfStates-1.
     */
    protected int numberOfStates;
    /**
     * Хранилище всех переходов.
     */
    protected HashMap<State, IPredicateMultiMap<P,T,State,?>> transitions;
    /**
     * Хранилище маркеров остановочных состояний
     */
    protected HashMap<State, HashSet<F>> markers;
    /**
     * Конструктор хранилищ для transitions.
     */
    protected IPredicateMultiMap<P,T,State,?> factory;
    /** Названия состояний. Используется только для вывода. */
    protected Map<State,String> stateNames;
};