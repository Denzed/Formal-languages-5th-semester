package hw06

import guru.nidi.graphviz.parse.Parser
import hw06.formal.CFGrammar
import hw06.formal.IntersectionCFGrammar
import java.io.File

fun main(arguments: Array<String>) {
    if (arguments.size < 2 || arguments.size > 3) {
        System.out.println(
                "Invalid number of arguments: expected 2-3, got ${arguments.size}\n" +
                "Usage: java -jar hw06-1.0-SNAPSHOT.jar automatonPath grammarPath [outputPath]\n" +
                "If no output file specified output will be commenced to standard out")
        return
    }
    val automatonPath = arguments[0]
    val grammarPath = arguments[1]

    try {
        val automaton = Parser.read(File(automatonPath))
        val grammar = CFGrammar.fromText(
                File(grammarPath).bufferedReader().use { it.readText() })
        val grammarInCNF = grammar.toChomskyNormalForm()

        val resultGrammar = IntersectionCFGrammar(grammarInCNF, automaton).get()

        resultGrammar.printTo(
                if (arguments.size == 3)
                    File(arguments[2]).bufferedWriter()
                else
                    System.out.bufferedWriter()
        )
    } catch (exception: Exception) {
        System.out.println("An exception occurred: ${exception.message}")
    } catch (error: Error) {
        System.out.println("An error occurred: ${error.message}")
    }
}