package wcn.fsa;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;

public class FSATest {
    
    @Test public void testNewState() {
        SimpleFSA<Integer,Integer> automaton = new SimpleFSA();
        assertEquals("Initial state is not zero", 0, automaton.numberOfStates);
        State state=automaton.newState();
        assertEquals("Number of states do not increase", 1, automaton.numberOfStates);
        assertEquals("Wrong state ID", 0, state.getId());
        state=automaton.newState();
        assertEquals("Number of states do not increase", 2, automaton.numberOfStates);
        assertEquals("Wrong state ID", 1, state.getId());
    }
    @Test public void testActiveStatesInitialization() {
        SimpleFSA<Integer,Integer> automaton = new SimpleFSA();
        Set<State> state=automaton.initialState();
        assertEquals("automaton without states has to have no active states.", new HashSet(), state);
        State start=automaton.newState();
        state=automaton.initialState();
        assertEquals("Initial active state is not the first state created.", new HashSet(Arrays.asList(start)), state);
    };
    @Test public void testTransitions() {
        SimpleFSA<Integer,Integer> automaton = new SimpleFSA();
        State start=automaton.newState();
        State second=automaton.newState();
        automaton.newTransition(start, second, 0);
        Set<State> state=automaton.initialState();
        assertEquals("Wrong initial state.", new HashSet(Arrays.asList(start)), state);
        state=automaton.makeTransition(state,0);
        assertNotNull("Transition is skipped.", state);
        assertEquals("Wrong transition.", new HashSet(Arrays.asList(second)), state);
        assertNull("Forbidden transition.", automaton.makeTransition(state,0));
    };
    @Test public void testNonDeterministicTransitions() {
        SimpleFSA<Integer,Integer> automaton = new SimpleFSA();
        State start=automaton.newState();
        State second=automaton.newState();
        automaton.newTransition(start, second, 0);
        automaton.newTransition(start, start, 0);
        automaton.newTransition(second, start, 1);
        Set<State> state=automaton.initialState();
        assertEquals("Wrong initial state.", new HashSet(Arrays.asList(start)), state);
        state=automaton.makeTransition(state,0);
        assertNotNull("Transition is skipped.", state);
        assertEquals("Wrong transition.", new HashSet(Arrays.asList(start, second)), state);
        state=automaton.makeTransition(state,0);
        assertNotNull("Transition is skipped.", state);
        assertEquals("Wrong transition.", new HashSet(Arrays.asList(start, second)), state);
        state=automaton.makeTransition(state,1);
        assertNotNull("Transition is skipped.", state);
        assertEquals("Wrong transition.", new HashSet(Arrays.asList(start)), state);
        assertNull("forbidden transition.", automaton.makeTransition(state,1));
    };
    @Test public void testTerminalMarks() {
        SimpleFSA<Integer,Integer> automaton = new SimpleFSA();
        State start=automaton.newState();
        State second=automaton.newState();
        State third=automaton.newState();
        automaton.newTransition(start, second, 0);
        automaton.newTransition(second, third, 0);
        automaton.markState(start, 0);
        automaton.markState(third, 1);
        automaton.markState(third, 2);
        Set<State> state=automaton.initialState();
        assertEquals("Wrong markers.", new HashSet(Arrays.asList(0)), automaton.getMarkers(state));
        state=automaton.makeTransition(state,0);
        assertNotNull(state);
        assertEquals("Wrong markers.", new HashSet(Arrays.asList()), automaton.getMarkers(state));
        state=automaton.makeTransition(state,0);
        assertNotNull(state);
        assertEquals("Wrong markers.", new HashSet(Arrays.asList(1,2)), automaton.getMarkers(state));
    };
    @Test public void testEpsilonTransitions() {
        SimpleFSA<Integer,Integer> automaton = new SimpleFSA();
        State start=automaton.newState();
        automaton.newTransition(start, start, null); // проаерка на бесконечный цикл
        assertEquals("Wrong initial active states", new HashSet(Arrays.asList(start)), automaton.initialState());
        State second=automaton.newState();
        assertEquals(new HashSet(Arrays.asList(start)), automaton.initialState());
        automaton.newTransition(start, second, null);
        assertEquals("No epsilon-transitions on newTransition", new HashSet(Arrays.asList(start, second)), automaton.initialState());
        State third=automaton.newState();
        automaton.newTransition(second, third, 0);
        Set<State> state=automaton.initialState();
        assertEquals(new HashSet(Arrays.asList(start, second)), state);
        state=automaton.makeTransition(state,0);
        assertNotNull(state);
        assertEquals(new HashSet(Arrays.asList(third)), state);
        automaton.newTransition(third,start,0);
        automaton.newTransition(second,third,null);
        state=automaton.makeTransition(state,0);
        assertNotNull(state);
        assertEquals(new HashSet(Arrays.asList(start, second, third)), state);
    };
}
