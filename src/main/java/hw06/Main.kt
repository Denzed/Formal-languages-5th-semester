package hw06

import guru.nidi.graphviz.parse.Parser
import hw06.formal.CFGrammar
import hw06.formal.IntersectionCFGrammar
import java.io.File

fun main(arguments: Array<String>) {
    if (arguments.size != 3) {
        System.out.println(
                "Invalid number of arguments: expected 3, got ${arguments.size}\n" +
                "Usage: [] automatonPath grammarPath outputPath")
        return
    }
    val automatonPath = arguments[0]
    val grammarPath = arguments[1]
    val outputPath = arguments[2]
    try {
        val automaton = Parser.read(File(automatonPath))
        val grammar = CFGrammar.fromText(
                File(grammarPath).bufferedReader().use { it.readText() })
        val grammarInCNF = grammar.toChomskyNormalForm()

        IntersectionCFGrammar(grammarInCNF, automaton)
                .get()
                .printTo(File(outputPath).bufferedWriter())
    } catch (error: Error) {
        System.out.println("An error occurred: ${error.message}")
    }
}