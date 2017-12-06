package parser

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.junit.Test


class NoParseTest {
    @Test(expected = ParseCancellationException::class)
    fun functionWithoutBodyDefinition() {
        astFromCode("fun kek()")
    }

    @Test(expected = ParseCancellationException::class)
    fun functionWithoutArgumentsDefinition() {
        astFromCode("fun kek { write lol }")
    }

    @Test(expected = ParseCancellationException::class)
    fun variableDefinitionWithAStatement() {
        astFromCode("var kek = (var lol)")
    }

    @Test(expected = ParseCancellationException::class)
    fun variableDefinitionWithNotIdentifierName() {
        astFromCode("var кек = 1")
    }

    @Test(expected = ParseCancellationException::class)
    fun readWithoutArgument() {
        astFromCode("read")
    }

    @Test(expected = ParseCancellationException::class)
    fun readWithNotAnIdentifierArgument() {
        astFromCode("read 1")
    }

    @Test(expected = ParseCancellationException::class)
    fun writeWithoutArgument() {
        astFromCode("write")
    }

    @Test(expected = ParseCancellationException::class)
    fun functionCallWithColonSeparatedArguments() {
        astFromCode("kek(1; 7; 9)")
    }

    @Test(expected = ParseCancellationException::class)
    fun whileCycleWithoutCondition() {
        astFromCode("while {}")
    }

    @Test(expected = ParseCancellationException::class)
    fun whileCycleWithEmptyCondition() {
        astFromCode("while () {}")
    }

    @Test(expected = ParseCancellationException::class)
    fun ifClauseWithDoubleElseBlock() {
        astFromCode("if (1) {} else {} else {}")
    }

    @Test(expected = ParseCancellationException::class)
    fun mismatchedParenthesesExpression() {
        astFromCode("kek = 1 + ((0)")
    }

    @Test(expected = ParseCancellationException::class)
    fun unknownOperator() {
        astFromCode("1 ? 2")
    }
}