import grammar.readGrammar
import lr0.LR0Automaton

fun analyze(string: String) {
    val cfGrammar = readGrammar(string)

    val lr0automaton = LR0Automaton(cfGrammar)

    println(lr0automaton)
}

fun main(args: Array<String>) {
    analyze("""
        S: S ( S )
        S: ε
    """.trimIndent())
//
//    analyze("""
//        S: ( S )
//        S: S S
//        S: ε
//    """.trimIndent())
//
//    analyze("""
//        S: M
//        S: S - M
//        M: N
//        M: N * M
//        N: 1 N'
//        N: 2 N'
//        N: 3 N'
//        N: 4 N'
//        N: 5 N'
//        N: 6 N'
//        N: 7 N'
//        N: 8 N'
//        N: 9 N'
//        N': ε
//        N': 0 N'
//        N': 1 N'
//        N': 2 N'
//        N': 3 N'
//        N': 4 N'
//        N': 5 N'
//        N': 6 N'
//        N': 7 N'
//        N': 8 N'
//        N': 9 N'
//    """.trimIndent())
//
    analyze("""
        S: a B c
        B: a b c
        B: b
        S: a D c c
        D: a D c c
        D: d
    """.trimIndent())
}