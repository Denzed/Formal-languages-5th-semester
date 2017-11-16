package parser

import org.junit.Test
import kotlin.test.assertEquals

class LexerTest {
    @Test
    fun testLexer() {
        val code =
"""// some random COMMENT
 	// different whitespaces \s \t
 /* moar comments!
 */
_lfanfkan_341341 // identifier
var = fun if then else while read write // keywords
179 // literals
+ - * / % == != > >= < <= && || // operators
(){} // delimiters"""
        val tokenString = tokensFromCode(code).joinToString()
        val expected = """COMMENT(0, 0, 22, "// some random COMMENT"), COMMENT(1, 2, 30, "// different whitespaces \s \t"), COMMENT(2, 1, 21, "/* moar comments!
 */"), IDENTIFIER(4, 0, 16, "_lfanfkan_341341"), COMMENT(4, 17, 13, "// identifier"), VAR_DEF(5, 0, 3, "var"), ASSIGN(5, 4, 1, "="), FUN_DEF(5, 6, 3, "fun"), IF_KW(5, 10, 2, "if"), IDENTIFIER(5, 13, 4, "then"), ELSE_KW(5, 18, 4, "else"), WHILE_KW(5, 23, 5, "while"), READ_KW(5, 29, 4, "read"), WRITE_KW(5, 34, 5, "write"), IDENTIFIER(5, 40, 5, "begin"), COMMENT(5, 46, 11, "// keywords"), Number(6, 0, 3, "179"), COMMENT(6, 4, 11, "// literals"), ADD(7, 0, 1, "+"), SUB(7, 2, 1, "-"), MUL(7, 4, 1, "*"), DIV(7, 6, 1, "/"), MOD(7, 8, 1, "%"), EQ(7, 10, 2, "=="), NEQ(7, 13, 2, "!="), GT(7, 16, 1, ">"), GE(7, 18, 2, ">="), LT(7, 21, 1, "<"), LE(7, 23, 2, "<="), LAND(7, 26, 2, "&&"), LOR(7, 29, 2, "||"), COMMENT(7, 32, 12, "// operators"), LPAREN(8, 0, 1, "("), RPAREN(8, 1, 1, ")"), CURLY_LPAREN(8, 2, 1, "{"), CURLY_RPAREN(8, 3, 1, "}"), COMMENT(8, 5, 13, "// delimiters")"""
        assertEquals(expected, tokenString)
    }
}