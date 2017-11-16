package parser.ast

data class AST(val root: ASTNode) {
    fun toPlantUML(): String {
        return PlantUMLConverter(root).result
    }

    override fun toString(): String {
        return root.toString()
    }
}

private class PlantUMLConverter(val root: ASTNode) {
    private var nodeCount = 0

    private val intermediateResult = mutableListOf<String>()

    val result = convert()

    private fun convert(): String {
        dfs(root)
        return intermediateResult.joinToString(
                "\n",
                "@startuml\n",
                "\n@enduml"
        )
    }

    private fun addEdge(from: Int, to: Int, label: String) {
        intermediateResult.add("$from --> $to: $label")
    }

    private fun addVertex(
            id: Int,
            label: String,
            description: List<String>,
            body: List<ASTNode>
    ) {
        if (body.isNotEmpty()) {
            intermediateResult.add("state \"$label\" as $id {")
            dfs(body.first())
            for (node in body.drop(1)) {
                intermediateResult.add("||")
                dfs(node)
            }
            intermediateResult.add("}")
        } else {
            intermediateResult.add("state \"$label\" as $id")
        }
        description.mapTo(intermediateResult) { "$id: $it" }
    }

    private fun dfsBlock(block: Block): Int {
        val id = nodeCount++
        addVertex(
                id,
                "code block",
                listOf("position: ${block.position}"),
                block.statements
        )
        return id
    }

    private fun dfsFunctionDefinition(definition: FunctionDefinition): Int {
        val id = nodeCount++
        val name = "${definition.identifier}"
        val description = listOf(
                "name: \"$name\"",
                "position: ${definition.position}"
        ) + definition.parameters.map { identifier -> identifier.text }
        addVertex(
                id,
                "function definition",
                description,
                emptyList()
        )
        addEdge(id, dfs(definition.body), "body")
        return id
    }

    private fun dfsVariableDefinition(definition: VariableDefinition): Int {
        val id = nodeCount++
        val name = "${definition.identifier}"
        val description = listOf(
                "name: \"$name\"",
                "position: ${definition.position}"
        )
        addVertex(id, "variable definition", description, emptyList())
        if (definition.value != null) {
            addEdge(id, dfs(definition.value), "value")
        }
        return id
    }

    private fun dfsReadStatement(statement: ReadStatement): Int {
        val id = nodeCount++
        val description = listOf(
                "position: ${statement.position}",
                "identifier: \"${statement.identifier}\""
        )
        addVertex(id, "read", description, emptyList())
        return id
    }

    private fun dfsWriteStatement(statement: WriteStatement): Int {
        val id = nodeCount++
        val description = listOf(
                "position: ${statement.position}"
        )
        addVertex(id, "write", description, emptyList())
        addEdge(id, dfs(statement.expression), "value")
        return id
    }

    private fun dfsWhileCycle(cycle: WhileCycle): Int {
        val id = nodeCount++
        addVertex(
                id,
                "while",
                listOf("position: ${cycle.position}"),
                emptyList()
        )
        addEdge(id, dfs(cycle.condition), "condition")
        addEdge(id, dfs(cycle.body), "body")
        return id
    }

    private fun dfsIfClause(clause: IfClause): Int {
        val id = nodeCount++
        addVertex(
                id,
                "if",
                listOf("position: ${clause.position}"),
                emptyList()
        )
        addEdge(id, dfs(clause.condition), "condition")
        addEdge(id, dfs(clause.thenBody), "then")
        if (clause.elseBody != null) {
            addEdge(id, dfs(clause.elseBody), "else")
        }
        return id
    }

    private fun dfsVariableAssignment(assignment: VariableAssignment): Int {
        val id = nodeCount++
        val name = "${assignment.identifier}"
        val description = listOf(
                "name: \"$name\"",
                "position: ${assignment.position}"
        )
        addVertex(id, "variable assignment", description, emptyList())
        addEdge(id, dfs(assignment.newValue), "new value")
        return id
    }

    private fun dfsFunctionCall(call: FunctionCall): Int {
        val id = nodeCount++
        val name = "${call.identifier}"
        val description = listOf(
                "name: \"$name\"",
                "position: ${call.position}"
        )
        addVertex(id, "function call", description, emptyList())
        call.parameters
                .map { dfs(it) }
                .forEachIndexed { index, parameterId -> addEdge(id, parameterId, "parameter #$index") }
        return id
    }


    private fun dfsIdentifier(identifier: Identifier): Int {
        val id = nodeCount++
        val name = "$identifier"
        val description = listOf(
                "name: \"$name\"",
                "position: ${identifier.position}"
        )
        addVertex(id, "identifier reference", description, emptyList())
        return id
    }

    private fun dfsNumber(number: Number): Int {
        val id = nodeCount++
        val description = listOf(
                "position: ${number.position}",
                "value: ${number.value}"
        )
        addVertex(id, "number", description, emptyList())
        return id
    }

    private fun dfsBracedExpression(expression: BracedExpression): Int {
        return dfs(expression.underlyingExpression)
    }

    private fun dfsBinaryExpression(expression: BinaryExpression): Int {
        val id = nodeCount++
        val description = listOf(
                "position: ${expression.position}",
                "operator: \"${expression.operator}\""
        )
        addVertex(id, "binary expression", description, emptyList())
        addEdge(id, dfs(expression.left), "left")
        addEdge(id, dfs(expression.right), "right")
        return id
    }

    private fun dfs(node: ASTNode): Int {
        return when (node) {
            is Block -> dfsBlock(node)
            is BracedBlock -> dfs(node.underlyingBlock)
            is FunctionDefinition -> dfsFunctionDefinition(node)
            is VariableDefinition -> dfsVariableDefinition(node)
            is ReadStatement -> dfsReadStatement(node)
            is WriteStatement -> dfsWriteStatement(node)
            is WhileCycle -> dfsWhileCycle(node)
            is IfClause -> dfsIfClause(node)
            is VariableAssignment -> dfsVariableAssignment(node)
            is FunctionCall -> dfsFunctionCall(node)
            is Identifier -> dfsIdentifier(node)
            is Number -> dfsNumber(node)
            is BracedExpression -> dfsBracedExpression(node)
            is BinaryExpression -> dfsBinaryExpression(node)
            else -> error("Unknown AST node type at ${node.position}")
        }
    }
}