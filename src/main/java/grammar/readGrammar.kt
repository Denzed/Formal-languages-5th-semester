package grammar

private val WS: String = "\t\n "

private fun isSymbolToken(token: String) = token.length > 1 && token.endsWith(':')

fun readGrammar(definition: String): CFGrammar {
    val tokens = definition.split("[$WS]+".toRegex()).filter { it.isNotEmpty() }

    val initial = Symbol.makeSymbol(definition.takeWhile { it != ':' })

    if (initial !is NonTerminal) {
        throw InvalidRuleException(initial.string)
    }

    val productions = mutableListOf<Production>()

    var pos = 0
    while (pos < tokens.size) {
        val token = tokens[pos]
        assert(isSymbolToken(token),
                { "Expected a rule definition, but have not found one" })
        val symbol = Symbol.makeSymbol(token.removeSuffix(":"))

        if (symbol !is NonTerminal) {
            throw InvalidRuleException(symbol.string)
        }

        pos++
        val products = mutableListOf<Symbol>()

        while (pos < tokens.size && !isSymbolToken(tokens[pos])) {
            products.add(Symbol.makeSymbol(tokens[pos]))
            pos++
        }
        assert(products.isNotEmpty(),
                { "Expected rule productions, but have not found one" })
        productions.add(Production(symbol, products))
    }

    return CFGrammar(initial, productions)
}