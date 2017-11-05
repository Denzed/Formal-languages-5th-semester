package hw06.formal

import java.io.BufferedWriter

class Symbol private constructor(val label: String) {
    val isTerminal = label[0].isLowerCase()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Symbol

        if (label != other.label) return false
        if (isTerminal != other.isTerminal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + isTerminal.hashCode()
        return result
    }

    companion object {
        val EPS_TEXT = "eps"
        val EPS = Symbol(EPS_TEXT)

        fun makeSymbol(label: String): Symbol {
            if (label == EPS_TEXT) {
                return EPS
            }
            return Symbol(label)
        }
    }
}


class Production(val symbol: Symbol, val products: List<Symbol>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Production

        if (symbol != other.symbol) return false
        if (products != other.products) return false

        return true
    }

    override fun hashCode(): Int {
        var result = symbol.hashCode()
        result = 31 * result + products.hashCode()
        return result
    }
}

class CFGrammar(
        var initial: Symbol,
        val productions: MutableMap<Symbol, MutableSet<Production>> = mutableMapOf()) {

    internal fun addProduction(production: Production) {
        productions.withDefault { _ -> mutableSetOf() }[production.symbol]!!.add(production)
    }

    fun toChomskyNormalForm(): CFGrammar {
        val shortGrammar = removeLongProductions()
        val epsFreeGrammar = shortGrammar.removeEpsProductions()
        val chainFreeGrammar = epsFreeGrammar.removeChainProductions()
        return chainFreeGrammar.removeNonTerminalsInLongProductions()
    }

    private fun removeLongProductions(): CFGrammar {
        val shortGrammar = CFGrammar(initial)
        productions.forEach({ symbol, symbolProductions ->
            var counter = 0
            for (production in symbolProductions) {
                val products = production.products
                val productsSize = products.size
                if (productsSize <= 2) {
                    shortGrammar.addProduction(production)
                    continue
                }
                val addedSymbols = mutableListOf<Symbol>()
                for (index in 0 until productsSize - 2) {
                    addedSymbols.add(Symbol.makeSymbol(symbol.label + counter++))
                }
                shortGrammar.addProduction(
                        Production(symbol, listOf(products[0], addedSymbols[0])))
                for (index in 0 until productsSize - 2) {
                    val newProduct =
                            if (index == productsSize - 3)
                                products[index + 2]
                            else
                                addedSymbols[index + 1]
                    val newProduction =
                            Production(addedSymbols[index],
                                    listOf(products[index + 1], newProduct))
                    shortGrammar.addProduction(newProduction)
                }
            }
        })
        return shortGrammar
    }

    private fun removeEpsProductions(): CFGrammar {
        val isEpsGenerating = mutableMapOf<Symbol,Boolean>()
        isEpsGenerating.put(Symbol.EPS, true)
        productions.forEach({ symbol, _ ->
            computeEpsGenerating(symbol, isEpsGenerating)
        })

        val epsFreeGrammar =
            if (isEpsGenerating[initial]!!) {
                val initialWithEps = Symbol.makeSymbol(initial.label + "'")
                val result = CFGrammar(initialWithEps)
                result.addProduction(Production(initialWithEps, listOf(Symbol.EPS)))
                result.addProduction(Production(initialWithEps, listOf(initial)))
                result
            } else {
                CFGrammar(initial)
            }
        productions.forEach({ _, symbolProductions ->
            val products = mutableListOf<Symbol>()
            for (production in symbolProductions) {
                generateNonEpsProductions(
                        products,
                        0,
                        production,
                        isEpsGenerating,
                        epsFreeGrammar)
            }
        })
        return epsFreeGrammar
    }

    private fun computeEpsGenerating(
            symbol: Symbol,
            isEpsGenerating: MutableMap<Symbol, Boolean>
    ): Boolean {
        val computedResult = isEpsGenerating[symbol]
        if (computedResult != null) {
            return computedResult
        }
        val symbolProductions = productions[symbol]
        val epsGenerating = symbolProductions?.any {
                production -> production.products.all {
                    productSymbol -> computeEpsGenerating(productSymbol, isEpsGenerating)
                }
            } ?: false
        isEpsGenerating.put(symbol, epsGenerating)
        return epsGenerating
    }

    private fun generateNonEpsProductions(
            takenProducts: MutableList<Symbol>,
            index: Int,
            production: Production,
            isEpsGenerating: Map<Symbol, Boolean>,
            epsFreeGrammar: CFGrammar) {
        val products = production.products
        if (index == products.size) {
            if (takenProducts.isNotEmpty() && takenProducts[0] != Symbol.EPS) {
                epsFreeGrammar.addProduction(
                        Production(production.symbol, takenProducts.toMutableList()))
            }
            return
        }
        val currentSymbol = products[index]
        if (isEpsGenerating[currentSymbol]!!) {
            generateNonEpsProductions(
                    takenProducts, index + 1, production, isEpsGenerating, epsFreeGrammar)
        }
        takenProducts.add(currentSymbol)
        generateNonEpsProductions(
                takenProducts, index + 1, production, isEpsGenerating, epsFreeGrammar)
        takenProducts.removeAt(takenProducts.size - 1)
    }

    private fun removeChainProductions(): CFGrammar {
        val chainFreeGrammar = CFGrammar(initial)
        val meaningfulSymbols = productions.keys.toList()
        val n = meaningfulSymbols.size
        val chainProducible = Array(n, { BooleanArray(n) })
        productions.forEach({ symbol, symbolProductions ->
            val symbolIndex = meaningfulSymbols.indexOf(symbol)
            for (production in symbolProductions) {
                val products = production.products
                if (products.size != 1) {
                    chainFreeGrammar.addProduction(production)
                    continue
                }
                val product = products[0]
                val productIndex = meaningfulSymbols.indexOf(product)
                if (productIndex == -1) {
                    chainFreeGrammar.addProduction(production)
                    continue
                }
                chainProducible[symbolIndex][productIndex] = true
            }
        })
        calculateTransitiveClosure(chainProducible)
        productions.forEach({ symbol, _ ->
            val symbolIndex = meaningfulSymbols.indexOf(symbol)
            (0 until n)
                    .filter { chainProducible[symbolIndex][it] }
                    .map { meaningfulSymbols[it] }
                    .forEach { product ->
                        productions[product]
                                ?.map { it.products }
                                ?.filterNot { it.size == 1 && !it[0].isTerminal }
                                ?.forEach { chainFreeGrammar.addProduction(Production(symbol, it)) }
                    }
        })
        return chainFreeGrammar
    }

    private fun removeNonTerminalsInLongProductions(): CFGrammar {
        val resultGrammar = CFGrammar(initial)
        val introducedSymbols = mutableSetOf<Symbol>()
        productions.forEach({ symbol, symbolProductions ->
            for (production in symbolProductions) {
                val products = production.products
                if (products.size == 1) {
                    resultGrammar.addProduction(production)
                    continue
                }
                val transformedProducts =
                    products.map { product ->
                        if (product.isTerminal) {
                            val newSymbol = Symbol.makeSymbol(product.label.toUpperCase() + "L")
                            if (newSymbol !in introducedSymbols) {
                                introducedSymbols.add(newSymbol)
                                resultGrammar.addProduction(
                                        Production(newSymbol, listOf(product)))
                            }
                            newSymbol
                        } else {
                            product
                        }
                    }
                resultGrammar.addProduction(Production(symbol, transformedProducts))
            }
        })
        return resultGrammar
    }

    private fun calculateTransitiveClosure(g: Array<BooleanArray>) {
        val n = g.size
        for (k in 0 until n) {
            for (i in 0 until n) {
                for (j in 0 until n) {
                    g[i][j] = g[i][j] or (g[i][k] and g[k][j])
                }
            }
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CFGrammar

        if (productions != other.productions) return false
        if (initial != other.initial) return false

        return true
    }

    override fun hashCode(): Int {
        var result = productions.hashCode()
        result = 31 * result + initial.hashCode()
        return result
    }

    fun printTo(bufferedWriter: BufferedWriter) {
        val initialProductions = productions[initial]
        if (initialProductions == null || initialProductions.isEmpty()) {
            bufferedWriter.appendln("${initial.label}: ${initial.label}")
        } else {
            printProductions(bufferedWriter, initialProductions)
        }
        productions.forEach({ symbol, symbolProductions ->
            if (symbol != initial) {
                printProductions(bufferedWriter, symbolProductions)
            }
        })
    }

    private fun printProductions(bufferedWriter: BufferedWriter, productions: Set<Production>) {
        for (production in productions) {
            bufferedWriter.appendln(production.products.joinToString(
                    " ",
                    production.symbol.label + ": "))
        }
    }

    companion object {
        private val WS: String = "[\t ]+"

        fun fromText(definition: String): CFGrammar {
            val productions = definition
                    .split("\n")
                    .map {
                        val (symbolString, result) = it.split(":")
                        assert(symbolString.length == 1 || symbolString == Symbol.EPS_TEXT,
                                { "Expected symbol of length 1, but got ${symbolString.length}" })
                        val symbol = Symbol.makeSymbol(symbolString)
                        return@map Pair(
                                symbol,
                                Production(symbol, result
                                        .split(WS.toRegex())
                                        .map { Symbol.makeSymbol(it) }))
                    }.groupBy { it.first }
                    .mapValues { it.value.map { pair -> pair.second }.toMutableSet() }
                    .toMutableMap()
            val start = definition.takeWhile { it != ':' }
            return CFGrammar(Symbol.makeSymbol(start), productions)
        }
    }
}