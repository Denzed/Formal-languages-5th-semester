package hw03.task1;

import automaton.Automaton;
import automaton.RegExp;

public class Main {
    public static void main(String[] args) {
        Automaton.setMinimization(Automaton.MINIMIZE_HOPCROFT);

        Automaton regexA = new RegExp("(a|b)*ab(a|b)*|(a|b)*a|b*").toAutomaton(true);
        Automaton regexB = new RegExp("(a|b)*(ab|ba)(a|b)*|a*|b*").toAutomaton(true);

        System.out.println(regexA.toDot());
        System.out.println(regexB.toDot());
    }
}
