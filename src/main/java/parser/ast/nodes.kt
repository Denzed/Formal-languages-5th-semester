package parser.ast

abstract class ASTNode {
    abstract val position: Pair<Int,Int>
}

data class Block(
        override val position: Pair<Int,Int>,
        val statements: List<Statement>
) : ASTNode() {
    override fun toString(): String {
        return statements.joinToString("\n")
    }
}

data class BracedBlock(
        override val position: Pair<Int,Int>,
        val underlyingBlock: Block
) : ASTNode() {
    override fun toString(): String {
        val indentedBody =  "\n$underlyingBlock"
                .replace("\n", "\n\t")
        return "{$indentedBody\n}"
    }
}

abstract class Statement: ASTNode()

data class FunctionDefinition(
        override val position: Pair<Int,Int>,
        val identifier: Identifier,
        val parameters: List<Identifier>,
        val body: BracedBlock
) : Statement() {
    override fun toString(): String {
        return "fun $identifier(${parameters.joinToString()}) $body"
    }
}

data class VariableDefinition(
        override val position: Pair<Int,Int>,
        val identifier: Identifier,
        val value: Expression?
) : Statement() {
    override fun toString(): String {
        return buildString {
            append("val $identifier")
            if (value != null) {
                append(" = $value")
            }
        }
    }
}

data class ReadStatement(
        override val position: Pair<Int,Int>,
        val identifier: Identifier
) : Statement() {
    override fun toString(): String {
        return "read $identifier"
    }
}

data class WriteStatement(
        override val position: Pair<Int,Int>,
        val expression: Expression
) : Statement() {
    override fun toString(): String {
        return "write $expression"
    }
}


abstract class Expression: ASTNode()

data class WhileCycle(
        override val position: Pair<Int,Int>,
        val condition: Expression,
        val body: BracedBlock
) : Statement() {
    override fun toString(): String {
        return "while ($condition) $body"
    }
}

data class IfClause(
        override val position: Pair<Int,Int>,
        val condition: Expression,
        val thenBody: BracedBlock,
        val elseBody: BracedBlock?
) : Statement() {
    override fun toString(): String {
        val elseBodyString = if (elseBody != null) "else $elseBody" else ""
        return "if ($condition) $thenBody $elseBodyString"
    }
}

data class VariableAssignment(
        override val position: Pair<Int,Int>,
        val identifier: Identifier,
        val newValue: Expression
) : Statement() {
    override fun toString(): String {
        return "$identifier = $newValue"
    }
}

data class FunctionCall(
        override val position: Pair<Int,Int>,
        val identifier: Identifier,
        val parameters: List<Expression>
) : Statement() {
    override fun toString(): String {
        return "$identifier(${parameters.joinToString()})"
    }
}

abstract class AtomicExpression: Expression()

data class Identifier(
        override val position: Pair<Int,Int>,
        val text: String
) : AtomicExpression() {
    override fun toString(): String {
        return text
    }
}

abstract class Literal : AtomicExpression()

data class Number(
        override val position: Pair<Int,Int>,
        val value: Int
) : Literal() {
    override fun toString(): String {
        return value.toString()
    }
}

data class BracedExpression(
        override val position: Pair<Int,Int>,
        val underlyingExpression: Expression
) : AtomicExpression() {
    override fun toString(): String {
        return "($underlyingExpression)"
    }
}

data class BinaryExpression(
        override val position: Pair<Int,Int>,
        val left: Expression,
        val operator: Op,
        val right: Expression
) : Expression() {
    override fun toString(): String {
        return "$left $operator $right"
    }

    companion object {
        val ops = Op.values().map { op -> Pair(op.string, op) }.toMap()

        enum class Op(val string: String) {
            ADD("+"),
            SUB("-"),
            MUL("*"),
            DIV("/"),
            MOD("%"),
            EQ("=="),
            NEQ("!="),
            LE("<="),
            LT("<"),
            GE(">="),
            GT(">"),
            LAND("&&"),
            LOR("||");

            override fun toString(): String {
                return string
            }
        }
    }
}