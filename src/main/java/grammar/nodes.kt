package grammar

abstract class Symbol {
    abstract val string: String

    companion object {
        fun makeSymbol(string: String): Symbol =
                when {
                    string.first().isUpperCase() -> NonTerminal(string)
                    else -> Terminal(string)
                }
    }
}

data class Terminal(override val string: String) : Symbol() {
    override fun toString(): String = string
}

val EOF = Terminal("$")

data class NonTerminal(override val string: String) : Symbol() {
    override fun toString(): String = string
}

data class Production(val from: NonTerminal, val to: List<Symbol>)

data class CFGrammar(val start: NonTerminal, val productions: List<Production>)