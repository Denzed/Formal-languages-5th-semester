package hw06.formal

import guru.nidi.graphviz.parse.Parser
import org.junit.Test
import java.io.File

class IntersectionTest {
    @Test
    fun testIntersectionGrammar() {
        val automaton = Parser.read(File("src/test/resources/automaton.dot"))
        val grammar = CFGrammar.fromText(File("src/test/resources/grammar.txt").readText())
        val normalizedGrammar = grammar.toChomskyNormalForm()

        val intersectionGrammar = IntersectionCFGrammar(normalizedGrammar, automaton).get()
        val expectedGrammar = CFGrammar.fromText(File("src/test/resources/expected.txt").readText())

        assert(intersectionGrammar == expectedGrammar)
    }
}