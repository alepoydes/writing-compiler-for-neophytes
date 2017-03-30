package wcn.fsa;

import org.junit.Test;

import wcn.terminal.*;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Arrays;

public class DFATest {
    
    @Test public void testNewState() {
        DFA<UChar,Integer,UChar> automaton = new DFA(new KeyPredicateMap());
        assertEquals("Initial state is not zero", 0, automaton.numberOfStates);
        State state=automaton.newState();
        assertEquals("Number of states do not increase", 1, automaton.numberOfStates);
        assertEquals("Wrong state ID", 0, state.getId());
        state=automaton.newState();
        assertEquals("Number of states do not increase", 2, automaton.numberOfStates);
        assertEquals("Wrong state ID", 1, state.getId());
    }
    @Test public void testActiveStatesInitialization() {
        DFA<UChar,Integer,UChar> automaton = new DFA(new KeyPredicateMap());
        State state=automaton.initialState();
        assertEquals("automaton without states has to have 0 active state.", 0, state.getId());
        state=automaton.newState();
        assertEquals("Initial active state is not the first state created.", 0, state.getId());
    };
    @Test public void testTransitions() {
        DFA<UChar,Integer,UChar> automaton = new DFA(new KeyPredicateMap());
        State start=automaton.newState();
        State first=automaton.newState();
        UChar a=new UChar('a');
        automaton.newTransition(start, first, a);
        State state=automaton.initialState();
        assertEquals("Wrong initial state.", start, state);
        state=automaton.makeTransition(state, a);
        assertNotNull("Transition is skipped.", state);
        assertEquals("Wrong transition.", first, state);
        assertNull("Forbidden transition.", automaton.makeTransition(state, a));
    };
    @Test public void testTerminalMarks() {
        DFA<UChar,Integer,UChar> automaton = new DFA(new KeyPredicateMap());
        State start=automaton.newState();
        State first=automaton.newState();
        State second=automaton.newState();
        UChar a=new UChar('a');
        automaton.newTransition(start, first, a);
        automaton.newTransition(first, second, a);
        automaton.markState(start, 0);
        automaton.markState(second, 1);
        State state=automaton.initialState();
        assertEquals("Wrong markers.", new Integer(0), automaton.getMarker(state));
        state=automaton.makeTransition(state, a);
        assertNotNull(state);
        assertEquals("Wrong markers.", null, automaton.getMarker(state));
        assertEquals("Wrong markers.", new HashSet(), automaton.getMarkers(state));
        state=automaton.makeTransition(state, a);
        assertNotNull(state);
        assertEquals("Wrong markers.", new Integer(1), automaton.getMarker(state));
        assertEquals("Wrong markers.", new HashSet(Arrays.asList(new Integer(1))), automaton.getMarkers(state));
    };
    @Test public void testFromFSA() {
        FSA<UChar,Integer,UChar> automaton = new FSA(new KeyPredicateMultiMap());
        State start=automaton.newState();
        State first=automaton.newState();
        State second=automaton.newState();
        UChar a=new UChar('a');
        UChar b=new UChar('b');
        automaton.newTransition(start,start,null);
        automaton.newTransition(start,first,null);
        automaton.newTransition(second,first,null); 
        automaton.newTransition(first,second,a);
        automaton.newTransition(start,second,b);        
        automaton.newTransition(second,start,a);
        automaton.newTransition(second,first,b);
        automaton.markState(start,0);
        System.out.println("Indeterminate automaton:"); System.out.println(automaton);
        /*
        Indeterminate automaton:
        #0:0: eps>0 'b'>2 eps>1 // start
        #1: 'a'>2             // first
        #2: eps>1 'a'>0 'b'>1 // second
        */
        DFA<UChar,Integer,UChar> dfa=new DFA(new KeyPredicateMap(), automaton);
        System.out.println("Determinate automaton:"); System.out.println(dfa);
        /*
        Determinate automaton:
        #0(0,1):0: 'a'>1 'b'>1   // s0
        #1(1,2): 'a'>2 'b'>3   // s1
        #2(0,1,2):0: 'a'>2 'b'>1 // s2
        #3(1): 'a'>1           // s3
        */
        // initial: start,first
        State s0=dfa.initialState();
        assertEquals((Integer)0,dfa.getMarker(s0));
        // after transition: second,first
        State s1=dfa.makeTransition(s0,a);
        assertNotNull(s1);
        assertEquals(null,dfa.getMarker(s1));
        assertTrue(s0!=s1);
        // start,first,second
        State s2=dfa.makeTransition(s1,a);
        assertEquals((Integer)0,dfa.getMarker(s2));
        assertTrue(s0!=s2); 
        assertTrue(s1!=s2);
        // start,first,second
        assertEquals(s2, dfa.makeTransition(s2, a));
        // second,first
        assertEquals(s1, dfa.makeTransition(s2, b));
        // first        
        State s3=dfa.makeTransition(s1, b);
        assertNotNull(s3);
        assertNull(dfa.getMarker(s3));
        assertTrue(s0!=s3);
        assertTrue(s1!=s3);
        assertTrue(s2!=s3);
        assertNull(dfa.makeTransition(s3,b));
        // second,first
        assertEquals(s1, dfa.makeTransition(s3,a));
    };
}
