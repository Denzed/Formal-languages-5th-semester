package hw03.task2;

import automaton.Automaton;
import automaton.RegExp;

import java.util.Arrays;

public class Main {
    private static final Automaton S = Automaton.makeChar(' ').repeat();
    private static final Automaton Z = new RegExp("[+-]{0,1}(0|[1-9][0-9]*)").toAutomaton();
    private static final Automaton SZ = S.concatenate(Z);
    private static final Automaton List = Automaton
            .makeChar('[')
            .concatenate(SZ.concatenate(S)
                           .concatenate(Automaton.makeChar(';'))
                           .repeat()
                           .concatenate(SZ)
                           .union(S))
            .concatenate(S)
            .concatenate(Automaton.makeChar(']'));

    private static final Automaton TupleElem;
    private static final Automaton STupleElem;
    private static final Automaton Tuple;

    static {
        Automaton.setMinimization(Automaton.MINIMIZE_HOPCROFT);

        List.minimize();

        TupleElem = Automaton
                .union(Arrays.asList(
                        hw02.task3.Main.identifiersAutomaton,
                        Z,
                        List));
        TupleElem.minimize();

        STupleElem = S.concatenate(TupleElem);
        STupleElem.minimize();

        Tuple = Automaton
                .makeChar('(')
                .concatenate(STupleElem.concatenate(S)
                        .concatenate(Automaton.makeChar(','))
                        .repeat()
                        .concatenate(STupleElem)
                        .union(S))
                .concatenate(S)
                .concatenate(Automaton.makeChar(')'));
        Tuple.minimize();
    }

    public static void main(String[] args) {
        System.out.println(List
                .toDot()
                .replace("\";\"", "\"semicolon\"")
                .replace("\\u0020", "\\\\s")
                .replace("\"[\"", "\"\\[\"")
                .replace("\"(\"", "\"\\(\"")
                .replace("\"]\"", "\"\\]\"")
                .replace("\")\"", "\"\\)\""));

        System.out.println(Tuple
                .toDot()
                .replace("\";\"", "\"semicolon\"")
                .replace("\\u0020", "\\\\s")
                .replace("\"[\"", "\"\\[\"")
                .replace("\"(\"", "\"\\(\"")
                .replace("\"]\"", "\"\\]\"")
                .replace("\")\"", "\"\\)\""));
    }
}
