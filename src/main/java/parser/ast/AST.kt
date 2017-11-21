package parser.ast

data class AST(val root: ASTNode) {
    fun toPlantUML(): String {
        return PlantUMLConverter(root).result
    }

    override fun toString(): String {
        return root.toString()
    }
}

private val indentation = "  "

private fun indent(indent: Int): String = indentation.repeat(indent)

private class PlantUMLConverter(val root: ASTNode) {
    private var nodeCount = 0

    private val intermediateResult = mutableListOf<String>()

    val result = convert()

    private fun convert(): String {
        dfs(root, 0)
        return intermediateResult.joinToString(
                "\n",
                "@startuml\n",
                "\n@enduml"
        )
    }

    private fun addEdge(from: Int, to: Int, label: String, indent: Int) {
        intermediateResult.add("${indent(indent)}$from --> $to: $label")
    }

    private fun addVertex(
            id: Int,
            label: String,
            description: List<String>,
            body: List<ASTNode>,
            indent: Int
    ) {
        if (body.isNotEmpty()) {
            intermediateResult.add("${indent(indent)}state \"$label\" as $id {")
            dfs(body.first(), indent + 1)
            for (node in body.drop(1)) {
                intermediateResult.add("${indent(indent + 1)}||")
                dfs(node, indent + 1)
            }
            intermediateResult.add("${indent(indent)}}")
        } else {
            intermediateResult.add("${indent(indent)}state \"$label\" as $id")
        }
        description.mapTo(intermediateResult) { "${indent(indent)}$id: $it" }
    }

    private fun dfsBlock(block: Block, indent: Int): Int {
        val id = nodeCount++
        addVertex(
                id,
                "code block",
                listOf("position: ${block.position}"),
                block.statements,
                indent
        )
        return id
    }

    private fun dfsFunctionDefinition(definition: FunctionDefinition, indent: Int): Int {
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
                emptyList(),
                indent
        )
        addEdge(id, dfs(definition.body, indent), "body", indent)
        return id
    }

    private fun dfsVariableDefinition(definition: VariableDefinition, indent: Int): Int {
        val id = nodeCount++
        val name = "${definition.identifier}"
        val description = listOf(
                "name: \"$name\"",
                "position: ${definition.position}"
        )
        addVertex(id, "variable definition", description, emptyList(), indent)
        if (definition.value != null) {
            addEdge(id, dfs(definition.value, indent), "value", indent)
        }
        return id
    }

    private fun dfsReadStatement(statement: ReadStatement, indent: Int): Int {
        val id = nodeCount++
        val description = listOf(
                "position: ${statement.position}",
                "identifier: \"${statement.identifier}\""
        )
        addVertex(id, "read", description, emptyList(), indent)
        return id
    }

    private fun dfsWriteStatement(statement: WriteStatement, indent: Int): Int {
        val id = nodeCount++
        val description = listOf(
                "position: ${statement.position}"
        )
        addVertex(id, "write", description, emptyList(), indent)
        addEdge(id, dfs(statement.expression, indent), "value", indent)
        return id
    }

    private fun dfsWhileCycle(cycle: WhileCycle, indent: Int): Int {
        val id = nodeCount++
        addVertex(
                id,
                "while",
                listOf("position: ${cycle.position}"),
                emptyList(),
                indent
        )
        addEdge(id, dfs(cycle.condition, indent), "condition", indent)
        addEdge(id, dfs(cycle.body, indent), "body", indent)
        return id
    }

    private fun dfsIfClause(clause: IfClause, indent: Int): Int {
        val id = nodeCount++
        addVertex(
                id,
                "if",
                listOf("position: ${clause.position}"),
                emptyList(),
                indent
        )
        addEdge(id, dfs(clause.condition, indent), "condition", indent)
        addEdge(id, dfs(clause.thenBody, indent), "then", indent)
        if (clause.elseBody != null) {
            addEdge(id, dfs(clause.elseBody, indent), "else", indent)
        }
        return id
    }

    private fun dfsVariableAssignment(assignment: VariableAssignment, indent: Int): Int {
        val id = nodeCount++
        val name = "${assignment.identifier}"
        val description = listOf(
                "name: \"$name\"",
                "position: ${assignment.position}"
        )
        addVertex(id, "variable assignment", description, emptyList(), indent)
        addEdge(id, dfs(assignment.newValue, indent), "new value", indent)
        return id
    }

    private fun dfsFunctionCall(call: FunctionCall, indent: Int): Int {
        val id = nodeCount++
        val name = "${call.identifier}"
        val description = listOf(
                "name: \"$name\"",
                "position: ${call.position}"
        )
        addVertex(id, "function call", description, emptyList(), indent)
        call.parameters
                .map { dfs(it, indent) }
                .forEachIndexed {
                    index, parameterId -> addEdge(id, parameterId, "parameter #$index", indent)
                }
        return id
    }


    private fun dfsIdentifier(identifier: Identifier, indent: Int): Int {
        val id = nodeCount++
        val name = "$identifier"
        val description = listOf(
                "name: \"$name\"",
                "position: ${identifier.position}"
        )
        addVertex(id, "identifier reference", description, emptyList(), indent)
        return id
    }

    private fun dfsNumber(number: Number, indent: Int): Int {
        val id = nodeCount++
        val description = listOf(
                "position: ${number.position}",
                "value: ${number.value}"
        )
        addVertex(id, "number", description, emptyList(), indent)
        return id
    }

    private fun dfsBracedExpression(expression: BracedExpression, indent: Int): Int =
            dfs(expression.underlyingExpression, indent)

    private fun dfsBinaryExpression(expression: BinaryExpression, indent: Int): Int {
        val id = nodeCount++
        val description = listOf(
                "position: ${expression.position}",
                "operator: \"${expression.operator}\""
        )
        addVertex(id, "binary expression", description, emptyList(), indent)
        addEdge(id, dfs(expression.left, indent), "left", indent)
        addEdge(id, dfs(expression.right, indent), "right", indent)
        return id
    }

    private fun dfs(node: ASTNode, indent: Int): Int = when (node) {
        is Block -> dfsBlock(node, indent)
        is BracedBlock -> dfs(node.underlyingBlock, indent)
        is FunctionDefinition -> dfsFunctionDefinition(node, indent)
        is VariableDefinition -> dfsVariableDefinition(node, indent)
        is ReadStatement -> dfsReadStatement(node, indent)
        is WriteStatement -> dfsWriteStatement(node, indent)
        is WhileCycle -> dfsWhileCycle(node, indent)
        is IfClause -> dfsIfClause(node, indent)
        is VariableAssignment -> dfsVariableAssignment(node, indent)
        is FunctionCall -> dfsFunctionCall(node, indent)
        is Identifier -> dfsIdentifier(node, indent)
        is Number -> dfsNumber(node, indent)
        is BracedExpression -> dfsBracedExpression(node, indent)
        is BinaryExpression -> dfsBinaryExpression(node, indent)
        else -> error("Unknown AST node type at ${node.position}")
    }
}