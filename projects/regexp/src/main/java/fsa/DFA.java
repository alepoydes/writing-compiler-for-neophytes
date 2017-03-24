package wcn.fsa;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;
import java.lang.StringBuilder;

import wcn.terminal.*;

/**
 * Интерфейс детерминированного конечного автомата.
 * Состояния автомата всегда кодируются целыми числами.
 * Переходы параметризуются типом T.
 * Остановочные состояния помечены метками типа F.
 */ 
public class DFA<T,F,P extends ICharSet<T,P>> implements IDFA<T, F> {
    /** Конструктор пустого автомата*/
    public<M extends IPredicateMap<P,T,State,M>> DFA(M factory) {
        this.factory=factory;
        this.transitions=new ArrayList<IPredicateMap<P,T,State,?>>();
        this.markers=new ArrayList<F>();
        this.numberOfStates=0;
        this.reset();
    };
    /** Конструктор, строящий детерминированный автомат из недетерминированного.
     */
    public<M extends IPredicateMap<P,T,State,M>> DFA(M factory, FSA<T,F,P> automaton) {
        this(factory);
        // Сохраняем состояние автомата, чтобы потом его восстановить
        Collection<State> automaton_pre=new HashSet(automaton.getActiveStates());
        // Перезапускаем автомат и сохраняем стартовое состояние
        automaton.reset();
        Set<State> old_initial=new HashSet(automaton.getActiveStates());
        // Создаем отображение старых состояний на новые
        Map<Set<State>,State> old2new=new HashMap();
        State new_initial=this.newState(old_initial);
        old2new.put(old_initial,new_initial);
        // Создаем массив состояний, которые мы еще не рассмотрели
        Deque<Set<State>> unprocessed=new ArrayDeque();
        unprocessed.addLast(old_initial);        
        // Перебираем все состояния 
        Set<State> old_states;
        while((old_states=unprocessed.poll())!=null) {
            State active=old2new.get(old_states);
            // и делаем из состояния active все переходы.
            // Получаем все возможные символы для перехода.
            automaton.setActiveStates(old_states);
            Map<P,Set<State>> old_transitions=DFA.purifyTransitions(automaton.getActiveTransitions());
            // Маркируем остановочные состояния
            Iterator<F> markers=automaton.getMarkers().iterator();
            if(markers.hasNext()) {
                F mark=markers.next();
                this.markState(active, mark);
                if(markers.hasNext()) {
                    System.out.print(String.format("Conflicting markers %s,%s,.. for states", mark, markers.next()));
                    for(State state: old_states) System.out.print(String.format(" %s", state.getId()));
                    System.out.println(".");
                };
            };
            // Для каждого символа перехода
            for(Map.Entry<P,Set<State>> entry: old_transitions.entrySet()) {
                // Ищем куда ведет переход
                Set<State> old_targets=entry.getValue();
                automaton.doEpsilonTransition(old_targets);
                State target;
                if(old2new.containsKey(old_targets)) target=old2new.get(old_targets);
                else {
                    target=this.newState(old_targets);
                    old2new.put(old_targets,target);
                    // Если состояние новое, то добавляем его в список непроанализированных состояний
                    unprocessed.addLast(old_targets);
                };
                // Создаем переход в новом автомате
                this.newTransition(active,target,entry.getKey());
            };
        };
        // Восстанавливаем начальное состояние
        automaton.setActiveStates(automaton_pre);
    }
    /** 
     * Составляет карту переходов, такую что ключи не имеют пересечений.
     */
    private static<P extends ICharSet<?,P>> Map<P,Set<State>> purifyTransitions(IPredicateMultiMap<P,?,State,?> transitions) {
        // Таблица уже добавленных переходов
        Map<P,Set<State>> map=new HashMap();
        // Добавляем переходы по одному
        for(Map.Entry<P,State> entry: transitions.entrySet()) {
            // Игнорируем эпсилон переходы, так как они уже учтены
            if(entry.getKey()==null) continue;
            // Создаем коллекцию для переходов, которые не учтены в карте map.
            Collection<P> new_keys=new HashSet(Arrays.asList(entry.getKey()));
            // Новая таблица переходов, содержащая map и переход entry.getValue
            Map<P,Set<State>> new_map=new HashMap();
            // Добавляем все переходы из старой карты
            for(Map.Entry<P,Set<State>> old_entry: map.entrySet()) {
                P intersection=entry.getKey().intersect(old_entry.getKey());
                // Если добавляемый ключ не пересекается со старым, то копируем старый
                if(intersection==null) {
                    new_map.put(old_entry.getKey(), old_entry.getValue());
                    continue;
                };
                // Копируем часть старого ключа, не пересекающуюся с новым
                for(P part: old_entry.getKey().subtract(intersection)) 
                    new_map.put(part, old_entry.getValue());
                // В пересечение ключей добавляем состояния из нового ключа
                Set<State> states=new HashSet(old_entry.getValue());
                states.add(entry.getValue());
                new_map.put(intersection, states);
                // Удаляем из недобавленной части перехода старый переход, 
                // так как мы ее только что добавили.
                Collection<P> new_new_keys=new HashSet();
                for(P key: new_keys) 
                    for(P remaining_part_of_key: key.subtract(old_entry.getKey()))
                        new_new_keys.add(remaining_part_of_key);
                new_keys=new_new_keys;
            };
            // Добавляем осток ключа, отсутствовавший в map
            Set<State> new_target=new HashSet();
            new_target.add(entry.getValue());
            for(P key: new_keys) new_map.put(key, new_target);
            // Составленную карту делаем активной
            map=new_map;
        };
        return map;
    }
    /** По данной таблице переходов строит эквивалентную, в которой символы переходов не пересекаются */
    /** Реализация интерфейся IDFA */
    public void reset() {
        this.activeState=0;
    };
    public boolean makeTransition(T label) {
        if(this.numberOfStates==0) return false;
        State state=this.transitions.get(this.activeState).get(label);
        if(state==null) return false;
        this.activeState=state.getId();
        return true;
    };
    public F getMarker() {
        if(this.numberOfStates==0) return null;
        return this.markers.get(this.activeState);
    };
    public Iterable<F> getMarkers() {
        F marker=this.getMarker();
        List<F> result=new ArrayList();
        if(marker!=null) result.add(marker);
        return result;
    }
    /** Методы доступа к внутреннему состоянию */
    public State getActiveState() { return new State(this.activeState); }
    /** Методы конструирования автомата */
    public State newState() {  
        State state=new State(this.numberOfStates++);
        this.markers.add(null);
        this.transitions.add(this.factory.empty());
        return state;
    };
    /** Создает новое состояние, сохраняя для отладки информацию о предках */
    public State newState(Collection<State> states) {  
        State state=this.newState();
        if(this.parentStates==null) this.parentStates=new ArrayList();
        while(this.parentStates.size()<state.getId()) this.parentStates.add(new HashSet());
        this.parentStates.add(new HashSet(states));
        return state;
    };
    public void newTransition(State from, State to, P label) throws IllegalArgumentException {
        if(label==null) throw new IllegalArgumentException("Epsilon(null) transition");
        this.transitions.get(from.getId()).put(label, to);
    };
    public void markState(State state, F mark) {
        this.markers.set(state.getId(),mark);
    };
    /** Методы Object */
    @Override public String toString() { 
        StringBuilder result=new StringBuilder();
        for(int n=0; n<this.transitions.size(); n++) {
            result.append(String.format("#%d", n));
            if(this.parentStates!=null) {
                result.append("(");
                boolean first=true;
                for(State state: this.parentStates.get(n))
                    if(first) { first=false; result.append(String.format("%d",state.getId())); }
                    else result.append(String.format(",%d",state.getId()));
                result.append(")");
            };
            F marker=this.markers.get(n);
            if(marker!=null) result.append(String.format(":%s", marker));
            result.append(":");
            for(Map.Entry<P,State> entry: this.transitions.get(n).entrySet()) {
                P symbol=entry.getKey();
                if(symbol!=null) result.append(String.format(" '%s'>%d", symbol, entry.getValue().getId()));
                else result.append(String.format(" eps>%d", entry.getValue().getId()));
            };
            result.append("\n");
        };
        return result.toString();
    };
    /** Реализиция */
    protected int activeState;
    protected int numberOfStates;
    protected List<IPredicateMap<P,T,State,?>> transitions;
    protected List<F> markers;
    protected List<Collection<State>> parentStates;
    protected IPredicateMap<P,T,State,?> factory;
}

