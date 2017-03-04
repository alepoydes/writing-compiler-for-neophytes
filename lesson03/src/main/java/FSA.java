package wcn.lexer;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
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
    public void reset() {
        this.activeStates.clear();
        if(this.numberOfStates>0) {
            this.activeStates.add(new State(0));
            this.doEpsilonTransition(this.activeStates);
        };
    };
    protected void doEpsilonTransition(HashSet<State> states) {
        HashSet<State> suspects=states;
        while(!suspects.isEmpty()) {
            HashSet<State> nextSuspects=new HashSet();
            for(State from: suspects) 
                for(State to: this.transitions.get(from).get(null)) 
                    if(!states.contains(to)) nextSuspects.add(to);
            for(State state: nextSuspects) states.add(state);
            suspects=nextSuspects;
        };
    };
    public boolean makeTransition(T label) {
        HashSet<State> next=new HashSet(); // Следующий набор активных состояний
        // делаем переходы по символу
        for(State from: this.activeStates) 
            for(State to: this.transitions.get(from).get(label))
                next.add(to);
        // делаем эпсилон переходы
        this.doEpsilonTransition(next);
        // проверяем, был ли хоть один переход
        if(next.isEmpty()) return false;
        this.activeStates=next;
        return true;
    };
    public Iterable<F> getMarkers() {
        HashSet<F> markers=new HashSet();
        for(State state: this.activeStates)
            for(F marker: this.markers.get(state)) 
                markers.add(marker);
        return markers;
    };
    // Оригинальные методы
    /**
     * Возвращает все достигнутые к настоящему моменту состояния.
     * Стартовое состояние всегда 0.
     */
    public Iterable<State> getActiveStates() {
        return this.activeStates;
    };
    /**
     * Возвразает все остановчные состояния.
     */
    public Iterable<State> getMarked() {
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
    public FSA(IPredicateMapFactory<P,T,State> factory) {
        this.factory=factory;
        this.activeStates=new HashSet();
        this.transitions=new HashMap();
        this.markers=new HashMap();
        numberOfStates=0;
        this.reset();
    };
    /**
     * Создает новое состояние и возвращает его номер.
     */
    public State newState() {  
        State state=new State(this.numberOfStates++);
        if(state.getId()==0) this.activeStates.add(state);
        this.transitions.put(state, this.factory.make());
        this.markers.put(state, new HashSet());
        return state;
    };
    /**
     * Создает переход между состояниями по данному символу.
     * Метка label=null соответствует эпсилон переходу.
     */
    public void newTransition(State from, State to, P label) {
        this.transitions.get(from).put(label, to);
        if(label==null) this.doEpsilonTransition(this.activeStates);
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
        // Добавляем активные состояния из присоединяемого автомата.
        for(State state: automaton.activeStates) 
            this.activeStates.add(new State(first.getId()+state.getId()));
        // Добавляем переходы
        for(Map.Entry<State,IPredicateMap<P,T,State>> entry: automaton.transitions.entrySet())
            this.transitions.get(new State(entry.getKey().getId()+first.getId()))
                .mergeMap(entry.getValue(), (state) -> new State(state.getId()+first.getId()));
        // Добавляем маркеры
        for(Map.Entry<State, HashSet<F>> entry: automaton.markers.entrySet()) {
            HashSet<F> target=this.markers.get(new State(entry.getKey().getId()+first.getId()));
            for(F marker: entry.getValue()) target.add(marker);
        };
        return first;
    };

    // Детали реализации
    /**
     * Перечень всех достигнутых к настоящему моменту состояний.
     */
    protected HashSet<State> activeStates; 
    /**
     * Число использованных состояний.
     * Автомат обязан иметь все состояния с номерами от 0 до numberOfStates-1.
     */
    protected int numberOfStates;
    /**
     * Хранилище всех переходов.
     */
    protected HashMap<State, IPredicateMap<P,T,State>> transitions;
    /**
     * Хранилище маркеров остановочных состояний
     */
    protected HashMap<State, HashSet<F>> markers;
    /**
     * Конструктор хранилищ для transitions.
     */
    protected IPredicateMapFactory<P,T,State> factory;
};