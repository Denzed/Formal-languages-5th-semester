package hw02.task3;

import automaton.Automaton;
import automaton.BasicAutomata;

public class Main {
    private static final String alphabet = "_0123456789qwertyuiopasdfghjklzxcvbnm";
    private static final String alphabetWithoutDigits = "_qwertyuiopasdfghjklzxcvbnm";
    private static final String[] keywords = {"if", "then", "else", "let", "in", "true", "false"};

    public static final Automaton identifiersAutomaton = BasicAutomata.makeCharSet(alphabetWithoutDigits).concatenate(
            BasicAutomata.makeCharSet(alphabet).repeat());

    private static final Automaton keywordsAutomaton = BasicAutomata.makeStringUnion(keywords);

    public static void main(String[] args) {
        Automaton.setMinimization(Automaton.MINIMIZE_HOPCROFT);

        Automaton identifiersWithoutKeywordsAutomaton = identifiersAutomaton.minus(keywordsAutomaton);
        identifiersWithoutKeywordsAutomaton.minimize();

        System.out.println(identifiersAutomaton.toDot());
        System.out.println(keywordsAutomaton.toDot());
        System.out.println(identifiersWithoutKeywordsAutomaton.toDot());
    }
}
