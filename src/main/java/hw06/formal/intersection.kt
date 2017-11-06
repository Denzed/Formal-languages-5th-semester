package hw06.formal

import guru.nidi.graphviz.attribute.MutableAttributed
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.model.MutableNode
import guru.nidi.graphviz.model.MutableNodePoint


internal class State private constructor(
        val nodeS: MutableNode,
        val symbol: Symbol,
        val nodeT: MutableNode) {

    companion object {
        fun of(nodeS: MutableNode, symbol: Symbol, nodeT: MutableNode): State {
            return State(nodeS, symbol, nodeT)
        }
    }

    override fun toString(): String {
        return "[${nodeS.label()},${symbol.label},${nodeT.label()}]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as State

        if (nodeS != other.nodeS) return false
        if (symbol != other.symbol) return false
        if (nodeT != other.nodeT) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nodeS.hashCode()
        result = 31 * result + symbol.hashCode()
        result = 31 * result + nodeT.hashCode()
        return result
    }
}

class IntersectionCFGrammar(
        private val grammar: CFGrammar,
        private val automaton: MutableGraph) {

    private val states = mutableSetOf<State>()
    private val stateQueue = mutableListOf<State>()

    private val intersectionGrammar: CFGrammar = initGrammar()
    private var built = false

    fun get(): CFGrammar = when {
        built -> intersectionGrammar
        else -> {
            build()
            get()
        }
    }

    private fun build() {
        if (built) {
            return
        }
        generateSingleLetterProductions()
        val productionsForProduct = buildProductionsForProduct()
        val nodes = automaton.nodes()
        while (stateQueue.isNotEmpty()) {
            val state = stateQueue.removeAt(0)
            val symbol = state.symbol
            val nodeS = state.nodeS
            val nodeT = state.nodeT
            productionsForProduct[symbol]?.forEach { production ->
                val products = production.products
                val firstProduct = products[0]
                val secondProduct = products[1]
                val isSymbolFirst = firstProduct == symbol
                nodes.forEach { complementingNode ->
                    val complementingState = if (isSymbolFirst)
                        State.of(nodeT, secondProduct, complementingNode)
                    else
                        State.of(complementingNode, firstProduct, nodeS)
                    if (complementingState in states) {
                        val unionState = if (isSymbolFirst)
                            State.of(nodeS, production.symbol, complementingNode)
                        else
                            State.of(complementingNode, production.symbol, nodeT)
                        if (isSymbolFirst) {
                            reachStateFrom(unionState, state, complementingState)
                        } else {
                            reachStateFrom(unionState, complementingState, state)
                        }
                    }
                }
            }
        }
        built = true
    }

    private fun buildProductionsForProduct(): MutableMap<Symbol, MutableSet<Production>> {
        val productionsForProduct = mutableMapOf<Symbol,MutableSet<Production>>()
        grammar.productions.forEach({ _, symbolProductions ->
            symbolProductions
                    .filter { production ->
                !production.products[0].isTerminal && production.products[0] != Symbol.EPS
                    }
                    .forEach { production ->
                        production.products.forEach { product ->
                            if (product !in productionsForProduct) {
                                productionsForProduct[product] = mutableSetOf()
                            }
                            productionsForProduct[product]!!.add(production)
                        }
                    }
        })
        return productionsForProduct
    }

    private fun generateSingleLetterProductions() {
        val terminals = computeTerminals()
        automaton.nodes().forEach { node ->
            for (link in node.links()) {
                val label = linkGetLabel(link)
                val nodeS = linkFromNode(link)
                val nodeF = linkToNode(link)
                terminals[label]?.forEach { terminal ->
                    val state = State.of(nodeS, terminal, nodeF)

                    reachStateFrom(state, null, null)
                    val newProduction = Production(
                            Symbol.makeSymbol(state.toString()),
                            listOf(Symbol.makeSymbol(label)))
                    intersectionGrammar.addProduction(newProduction)
                }
            }
        }
    }

    private fun computeTerminals(): MutableMap<String,MutableSet<Symbol>> {
        val terminals = mutableMapOf<String,MutableSet<Symbol>>()
        grammar.productions.forEach({ symbol, symbolProductions ->
            symbolProductions
                    .filter { it.products.size == 1 }
                    .map { it.products[0] }
                    .filter { it.isTerminal }
                    .forEach {
                        if (it.label !in terminals) {
                            terminals[it.label] = mutableSetOf()
                        }
                        terminals[it.label]!!.add(symbol)
                    }
        })
        return terminals
    }

    private fun reachStateFrom(state: State, leftState: State?, rightState: State?) {
        if (leftState != null && rightState != null) {
            intersectionGrammar.addProduction(
                    Production(Symbol.makeSymbol(state.toString()),
                            listOf(Symbol.makeSymbol(leftState.toString()),
                                    Symbol.makeSymbol(rightState.toString()))))
        }
        if (state !in states) {
            states.add(state)
            stateQueue.add(state)
        }
    }

    private fun initGrammar(): CFGrammar {
        val intersectionInitialNode = Symbol.makeSymbol("S")
        val intersectionGrammar = CFGrammar(intersectionInitialNode)

        val initialNode = grammar.initial
        val grammarContainsEpsRule = grammar.productions[initialNode]?.any {
            production -> production.products.size == 1 &&
                          production.products[0] == Symbol.EPS
        } ?: false

        val automatonStartNode = findStartNode()
        val terminalNodes = findTerminalNodes()
        for (automatonTerminalNode in terminalNodes) {
            val state = State.of(automatonStartNode, initialNode, automatonTerminalNode)
            val stateSymbol = Symbol.makeSymbol(state.toString())
            intersectionGrammar.addProduction(
                    Production(intersectionInitialNode, listOf(stateSymbol)))
            if (grammarContainsEpsRule &&
                    automatonStartNode == automatonTerminalNode) {
                intersectionGrammar.addProduction(
                        Production(stateSymbol, listOf(Symbol.EPS)))
            }
        }
        return intersectionGrammar
    }

    private fun findStartNode(): MutableNode {
        val startNodes = findNodesColored(START_COLOR_LABEL)
        when {
            startNodes.isEmpty() -> error("No start nodes.")
            startNodes.size > 1  -> error("More than one start node.")
        }
        return startNodes[0]
    }

    private fun findTerminalNodes(): List<MutableNode> {
        val terminalNodes = findNodesShaped(TERMINAL_SHAPE_LABEL)
        if (terminalNodes.isEmpty()) {
            error("No terminal nodes.")
        }
        return terminalNodes
    }

    private fun findNodesShaped(shapeLabel: String): List<MutableNode> {
        return automaton.nodes()
                .filter({ node -> shapeLabel == nodeGetShape(node) })
    }

    private fun findNodesColored(colorLabel: String): List<MutableNode> {
        return automaton.nodes()
                .filter({ node -> colorLabel == nodeGetColor(node) })
    }

    companion object {
        private val START_COLOR_LABEL = "red"
        private val TERMINAL_SHAPE_LABEL = "doublecircle"

        private fun nodeGetShape(node: MutableNode): String? {
            return getAttrValue(node.attrs(), "shape")
        }

        private fun nodeGetColor(node: MutableNode): String? {
            return getAttrValue(node.attrs(), "color")
        }

        private fun linkGetLabel(link: Link): String {
            return getAttrValue(link.attrs(), "label") ?:
                    error("Link without a symbol!")
        }

        private fun <T> getAttrValue(attrs: MutableAttributed<T>, label: String): String? {
            return attrs.firstOrNull { it.key == label }?.value.toString()
        }

        private fun linkFromNode(link: Link): MutableNode {
            return (link.from() as MutableNodePoint).node()
        }

        private fun linkToNode(link: Link): MutableNode {
            return (link.to() as MutableNodePoint).node()
        }
    }
}