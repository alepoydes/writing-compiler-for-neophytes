package wcn.fsa;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Arrays;

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
        assertEquals("automaton without states has to have no active states.", new HashSet(), automaton.getActiveStates());
        State start=automaton.newState();
        assertEquals("Initial active state is not the first state created.", new HashSet(Arrays.asList(start)), automaton.getActiveStates());
    };
    @Test public void testTransitions() {
        SimpleFSA<Integer,Integer> automaton = new SimpleFSA();
        State start=automaton.newState();
        State second=automaton.newState();
        automaton.newTransition(start, second, 0);
        assertEquals("Wrong initial state.", new HashSet(Arrays.asList(start)), automaton.getActiveStates());
        assertTrue("Transition is skipped.", automaton.makeTransition(0));
        assertEquals("Wrong transition.", new HashSet(Arrays.asList(second)), automaton.getActiveStates());
        assertFalse("Forbidden transition.", automaton.makeTransition(0));
        assertEquals("State is changed without transition.", new HashSet(Arrays.asList(second)), automaton.getActiveStates());
    };
    @Test public void testNonDeterministicTransitions() {
        SimpleFSA<Integer,Integer> automaton = new SimpleFSA();
        State start=automaton.newState();
        State second=automaton.newState();
        automaton.newTransition(start, second, 0);
        automaton.newTransition(start, start, 0);
        automaton.newTransition(second, start, 1);
        assertEquals("Wrong initial state.", new HashSet(Arrays.asList(start)), automaton.getActiveStates());
        assertTrue("Transition is skipped.", automaton.makeTransition(0));
        assertEquals("Wrong transition.", new HashSet(Arrays.asList(start, second)), automaton.getActiveStates());
        assertTrue("Transition is skipped.", automaton.makeTransition(0));
        assertEquals("Wrong transition.", new HashSet(Arrays.asList(start, second)), automaton.getActiveStates());
        assertTrue("Transition is skipped.", automaton.makeTransition(1));
        assertEquals("Wrong transition.", new HashSet(Arrays.asList(start)), automaton.getActiveStates());
        assertFalse("forbidden transition.", automaton.makeTransition(1));
        assertEquals("State is changed without transition.", new HashSet(Arrays.asList(start)), automaton.getActiveStates());        
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
        assertEquals("Wrong markers.", new HashSet(Arrays.asList(0)), automaton.getMarkers());
        assertTrue(automaton.makeTransition(0));
        assertEquals("Wrong markers.", new HashSet(Arrays.asList()), automaton.getMarkers());
        assertTrue(automaton.makeTransition(0));
        assertEquals("Wrong markers.", new HashSet(Arrays.asList(1,2)), automaton.getMarkers());
    };
    @Test public void testEpsilonTransitions() {
        SimpleFSA<Integer,Integer> automaton = new SimpleFSA();
        State start=automaton.newState();
        automaton.newTransition(start, start, null); // проаерка на бесконечный цикл
        assertEquals("Wrong initial active states", new HashSet(Arrays.asList(start)), automaton.getActiveStates());
        State second=automaton.newState();
        assertEquals(new HashSet(Arrays.asList(start)), automaton.getActiveStates());
        automaton.newTransition(start, second, null);
        assertEquals("No epsilon-transitions on newTransition", new HashSet(Arrays.asList(start, second)), automaton.getActiveStates());
        State third=automaton.newState();
        automaton.newTransition(second, third, 0);
        assertEquals(new HashSet(Arrays.asList(start, second)), automaton.getActiveStates());
        assertTrue(automaton.makeTransition(0));
        assertEquals(new HashSet(Arrays.asList(third)), automaton.getActiveStates());
        automaton.newTransition(third,start,0);
        automaton.newTransition(second,third,null);
        assertTrue(automaton.makeTransition(0));
        assertEquals(new HashSet(Arrays.asList(start, second, third)), automaton.getActiveStates());
    };
}
