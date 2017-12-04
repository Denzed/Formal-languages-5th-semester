package lr0

import grammar.*

data class LR0Item(val production: Production, val pointer: Int) {
    override fun toString(): String = buildString {
        val pref = production.to.take(pointer)
        val suf = production.to.drop(pointer)
        append(
                production.from,
                " -> ",
                pref.joinToString(""),
                ".",
                suf.joinToString("")
        )
    }
}

data class State(val items: Set<LR0Item>) {
    override fun toString(): String =
            buildString {
                append("{", items.joinToString(), "}")
            }
}

data class Edge(val from: State, val symbol: Symbol, val to: State)

class LR0Automaton(cfGrammar: CFGrammar) {
    private fun augmentGrammar(cfGrammar: CFGrammar): CFGrammar {
        val newStart = NonTerminal(cfGrammar.start.string + "_aug")
        val newProductions = cfGrammar.productions + Production(newStart, listOf(cfGrammar.start, EOF))
        return CFGrammar(newStart, newProductions)
    }

    private val augmentedGrammar = augmentGrammar(cfGrammar)
    private val groupedProductions = augmentedGrammar.productions.groupBy { it.from }
    private val symbols = augmentedGrammar.productions.flatMap { it -> it.to + it.from }.toSet()

    private fun closure(items: Set<LR0Item>): Set<LR0Item> {
        val result = items.toMutableSet()
        var full = false
        while (!full) {
            full = true
            for (item in items) {
                val to = item.production.to
                if (item.pointer < to.size && to[item.pointer] is NonTerminal) {
                    groupedProductions[to[item.pointer]]?.forEach {
                        production -> full = !result.add(LR0Item(production, 0))
                    }
                }
            }
        }
        return result.toSet()
    }

    private fun goto(items: Set<LR0Item>, symbol: Symbol): Set<LR0Item> = closure(
            items
                    .filter {
                        item -> item.pointer < item.production.to.size
                            && item.production.to[item.pointer] == symbol
                    }
                    .map { item -> item.copy(pointer = item.pointer + 1) }
                    .toSet()
    )

    val states = mutableSetOf<State>()
    val edges = mutableSetOf<Edge>()
    val start = build()

    private fun build(): State {
        fun dfs(u: State) {
            for (symbol in symbols) {
                val v = State(goto(u.items, symbol))
                if (v !in states) {
                    states.add(v)
                    dfs(v)
                }
                val edge = Edge(u, symbol, v)
                if (edge !in edges) {
                    edges.add(edge)
                }
            }
        }

        val start = State(
                closure(
                        setOf(
                                LR0Item(
                                        groupedProductions[augmentedGrammar.start]!!.first(),
                                        0
                                )
                        )
                )
        )
        states.add(start)

        dfs(start)

        return start
    }

    override fun toString(): String {
        val mapping = states
                .filter { state -> state.items.isNotEmpty() }
                .mapIndexed { index, state -> state to index }.toMap()
        val mappedEdges : Map<State, Map<Symbol, List<State>>> = edges
                .groupBy { it.from }
                .mapValues {
                    entry -> entry.value
                        .groupBy { it.symbol }
                        .mapValues { innerEntry -> innerEntry.value.map { edge -> edge.to } }
                }
        return buildString {
            appendln("LR0Automaton with start at ${mapping[start]!!}:")
            mapping.forEach { state, index -> appendln("$index: $state") }
            appendln(symbols.joinToString("|", "-|"))
            appendln(symbols.joinToString("-|-", "-|") { "" })
            for ((state, index) in mapping.toList().sortedBy { it.second }) {
                val reduceActions = state.items
                        .filter { it.pointer == it.production.to.size }
                        .map { augmentedGrammar.productions.indexOf(it.production) }
                if (reduceActions.size > 1) {
                    throw LR0ConflictFound("reduce-reduce")
                }
                appendln(symbols
                        .map { symbol ->
                            val to = mappedEdges[state]?.get(symbol)
                            assert(to == null || to.size < 2, { "Detected ambiguity" })
                            val result = to
                                    ?.first()
                                    ?.let { mapping[it]?.toString() }
                                    ?.let {
                                        when {
                                            symbol is Terminal && reduceActions.isEmpty() ->
                                                if (symbol == EOF) "acc" else "s($it)"
                                            symbol is Terminal ->
                                                throw LR0ConflictFound("shift-reduce")
                                            reduceActions.isNotEmpty() ->
                                                "r(${reduceActions.first()})"
                                            else -> it
                                        }
                                    }
                            return@map result ?: ""
                        }
                        .joinToString(" | ", "$index |")
                )
            }
        }
    }
}