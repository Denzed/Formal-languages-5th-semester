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
}

class IntersectionCFGrammar(
        private val grammar: CFGrammar,
        private val automaton: MutableGraph) {

    private val states = mutableSetOf<State>()
    private val stateQueue = mutableListOf<State>()

    private var intersectionGrammar: CFGrammar = build()

    fun get(): CFGrammar = intersectionGrammar

    private fun build(): CFGrammar {
        intersectionGrammar = initGrammar()
        generateSingleLetterProductions()
        val productionsForProduct = buildProductionsForProduct()
        val nodes = automaton.nodes()
        while (stateQueue.isNotEmpty()) {
            val state = stateQueue.removeAt(0)
            productionsForProduct[state.symbol]?.forEach { production ->
                val products = production.products
                if (products[0] == state.symbol) {
                    nodes
                            .map { Pair(it, State.of(state.nodeT, products[1], it)) }
                            .filter { it.second in states }
                            .forEach { reachStateFrom(
                                    State.of(state.nodeS, production.symbol, it.first),
                                    state,
                                    it.second) }
                } else {
                    nodes
                            .map { Pair(it, State.of(it, products[0], state.nodeS)) }
                            .filter { it.second in states }
                            .forEach { reachStateFrom(
                                    State.of(it.first, production.symbol, state.nodeT),
                                    it.second,
                                    state) }
                }
            }
        }
        return intersectionGrammar
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
                            productionsForProduct
                                    .withDefault { mutableSetOf() }[product]!!
                                    .add(production)
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
                    intersectionGrammar.addProduction(Production(
                            Symbol.makeSymbol(state.toString()),
                            listOf(Symbol.makeSymbol(label))))
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
                    .forEach { terminals.withDefault { mutableSetOf() }[it.label]!!.add(symbol) }
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
        val startNodes = findNodesShaped(START_SHAPE_LABEL)
        if (startNodes.isEmpty()) {
            error("No start nodes.")
        }
        if (startNodes.size > 1) {
            error("More than one start node.")
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

    companion object {
        private val START_SHAPE_LABEL = "plaintext"
        private val TERMINAL_SHAPE_LABEL = "doublecircle"

        private fun nodeGetShape(node: MutableNode): String? {
            return getAttrValue(node.attrs(), "shape")
        }

        private fun linkGetLabel(link: Link): String {
            return getAttrValue(link.attrs(), "symbol") ?:
                    error("Link without a symbol!")
        }

        private fun <T> getAttrValue(attrs: MutableAttributed<T>, label: String): String? {
            return attrs.filter { it.key == label }[0].value.toString()
        }

        private fun linkFromNode(link: Link): MutableNode {
            return (link.from() as MutableNodePoint).node()
        }

        private fun linkToNode(link: Link): MutableNode {
            return (link.to() as MutableNodePoint).node()
        }
    }
}