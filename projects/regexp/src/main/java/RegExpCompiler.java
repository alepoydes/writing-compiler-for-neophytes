package wcn;

import wcn.fsa.*;
import wcn.lexer.*;
import wcn.terminal.*;
import wcn.regexp.*;
import wcn.parser.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;

public class RegExpCompiler {
    public static void main(String[] args) throws IOException {
        BufferedReader input=new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String str;
        RexExp regexp=null;
        do {
            System.out.println("Enter regexp:");
            str=input.readLine();
            try {
                regexp=new RexExp(str);
            } catch(ParserError err) {
                System.out.println("Malformed regexp. Try again.");
            };
        } while(regexp==null);
        System.out.println("Finite state automaton for the regexp:");
        System.out.println(regexp.automaton);
        outer: while(true) {
            System.out.println("Enter word to match the regexp:");
            str=input.readLine();
            if(str==null) break;
            List<UChar> list=UChar.asList(str);
            State state=regexp.automaton.initialState();
            for(UChar label: list) {
                System.out.printf("  State %s. Can stop: %s\n",state,regexp.automaton.getMarkers(state).isEmpty()?"no":"yes");
                System.out.printf("  Transition by symbol %s.\n", label);
                state=regexp.automaton.makeTransition(state, label);
                if(state==null) {
                    System.out.printf("  There is no appropriate transition.\n");
                    System.out.printf("The word does not match the regexp.\n");
                    continue outer;
                };
            };
            System.out.printf("  State %s. Can stop: %s\n",state,regexp.automaton.getMarkers(state).isEmpty()?"no":"yes");
            System.out.printf("  End of line.\n");
            if(regexp.automaton.getMarkers(state).isEmpty()) {
                System.out.printf("  The state is not marked as stop state.\n");
                System.out.printf("The word does not match the regexp.\n");
            } else 
                System.out.printf("The word mathes the regexp.\n");
        };
    }
}
