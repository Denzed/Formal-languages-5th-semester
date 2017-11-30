package parser

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import parser.ast.AST
import parser.ast.ASTBuilder

data class MyToken(val name: String, val position: Pair<Int,Int>, val text: String) {
    override fun toString(): String {
        return "$name(${position.first - 1}, ${position.second}, ${text.length}, \"$text\")"
    }
}

fun tokensFromCode(code: String): List<MyToken> {
    val lexer = LLexer(CharStreams.fromString(code))
    return lexer.allTokens.map { token ->
        MyToken(
                lexer.vocabulary.getSymbolicName(token.type),
                Pair(token.line, token.charPositionInLine),
                token.text
        )
    }
}

fun astFromCode(code: String): AST {
    val lexer = LLexer(CharStreams.fromString(code))
    val parser = LParser(CommonTokenStream(lexer))
    parser.addErrorListener(ParseErrorListener)
    return AST(ASTBuilder.visit(parser.file()))
}