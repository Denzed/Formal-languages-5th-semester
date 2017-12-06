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
    lexer.removeErrorListeners()
    lexer.addErrorListener(ParseErrorListener)
    return lexer
            .allTokens
            .filter { token -> token.type != LLexer.WS }
            .map { token -> MyToken(
                        lexer.vocabulary.getSymbolicName(token.type),
                        Pair(token.line, token.charPositionInLine),
                        token.text
                    )
            }
}

fun astFromCode(code: String): AST {
    val lexer = LLexer(CharStreams.fromString(code))
    lexer.removeErrorListeners()
    lexer.addErrorListener(ParseErrorListener)
    val parser = LParser(CommonTokenStream(lexer))
    parser.removeErrorListeners()
    parser.addErrorListener(ParseErrorListener)
    return AST(ASTBuilder.visit(parser.file()))
}