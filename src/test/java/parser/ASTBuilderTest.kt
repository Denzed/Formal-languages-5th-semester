package parser
import org.junit.After
import org.junit.Before
import org.junit.Test
import parser.ast.*
import parser.ast.BinaryExpression.Companion.Op
import parser.ast.Number
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals


private object PositionIndependentASTConverter {
    val blankPosition = Pair(-1, -1)

    fun convertAST(ast: AST): AST = AST(dfs(ast.root))

    private fun dfs(node: ASTNode): ASTNode = when (node) {
        is Block -> dfsBlock(node)
        is BracedBlock -> dfsBracedBlock(node)
        else -> error("Unknown AST node type at ${node.position}")
    }

    private fun dfsBlock(node: Block): Block = Block(
            blankPosition,
            node.statements.map(this::dfsStatement)
    )

    private fun dfsBracedBlock(node: BracedBlock): BracedBlock = BracedBlock(
            blankPosition,
            dfsBlock(node.underlyingBlock)
    )

    private fun dfsStatement(node: Statement): Statement {
        return when (node) {
            is FunctionDefinition -> dfsFunctionDefinition(node)
            is VariableDefinition -> dfsVariableDefinition(node)
            is ReadStatement -> dfsReadStatement(node)
            is WriteStatement -> dfsWriteStatement(node)
            is WhileCycle -> dfsWhileCycle(node)
            is IfClause -> dfsIfClause(node)
            is VariableAssignment -> dfsVariableAssignment(node)
            is FunctionCall -> dfsFunctionCall(node)
            else -> error("Unknown AST node type at ${node.position}")
        }
    }

    private fun dfsExpression(node: Expression): Expression {
        return when (node) {
            is Identifier -> dfsIdentifier(node)
            is Number -> dfsNumber(node)
            is BracedExpression -> dfsBracedExpression(node)
            is BinaryExpression -> dfsBinaryExpression(node)
            else -> error("Unknown AST node type at ${node.position}")
        }
    }

    private fun dfsFunctionDefinition(definition: FunctionDefinition): FunctionDefinition =
            FunctionDefinition(
                    blankPosition,
                    dfsIdentifier(definition.identifier),
                    definition.parameters.map(this::dfsIdentifier),
                    dfsBracedBlock(definition.body)
            )

    private fun dfsVariableDefinition(definition: VariableDefinition): Statement =
            VariableDefinition(
                    blankPosition,
                    dfsIdentifier(definition.identifier),
                    definition.value?.let(this::dfsExpression)
            )

    private fun dfsReadStatement(statement: ReadStatement): Statement =
            ReadStatement(
                    blankPosition,
                    dfsIdentifier(statement.identifier)
            )

    private fun dfsWriteStatement(statement: WriteStatement): Statement =
            WriteStatement(
                    blankPosition,
                    dfsExpression(statement.expression)
            )

    private fun dfsWhileCycle(cycle: WhileCycle): Statement =
            WhileCycle(
                    blankPosition,
                    dfsExpression(cycle.condition),
                    dfsBracedBlock(cycle.body)
            )

    private fun dfsIfClause(clause: IfClause): Statement =
            IfClause(
                    blankPosition,
                    dfsExpression(clause.condition),
                    dfsBracedBlock(clause.thenBody),
                    clause.elseBody?.let(this::dfsBracedBlock)
            )

    private fun dfsVariableAssignment(assignment: VariableAssignment): Statement =
            VariableAssignment(
                    blankPosition,
                    dfsIdentifier(assignment.identifier),
                    dfsExpression(assignment.newValue)
            )

    private fun dfsFunctionCall(call: FunctionCall): Statement =
            FunctionCall(
                    blankPosition,
                    dfsIdentifier(call.identifier),
                    call.parameters.map(this::dfsExpression)
            )


    private fun dfsIdentifier(identifier: Identifier): Identifier =
            identifier.copy(blankPosition)

    private fun dfsNumber(number: Number): Expression = number.copy(blankPosition)


    private fun dfsBracedExpression(expression: BracedExpression): Expression =
            BracedExpression(blankPosition, dfsExpression(expression.underlyingExpression))

    private fun dfsBinaryExpression(expression: BinaryExpression): Expression =
            expression.copy(
                    blankPosition,
                    dfsExpression(expression.left),
                    right = dfsExpression(expression.right)
            )
}

class ASTBuilderTest {
    private val errContent = ByteArrayOutputStream()

    @Before
    fun setUpStreams() {
        System.setErr(PrintStream(errContent))
    }

    @After
    fun cleanUpStreams() {
        assertEquals(0, errContent.size(), errContent.toString())
        errContent.reset()
        System.setErr(null)
    }

    private fun removePositions(ast: AST): AST = PositionIndependentASTConverter.convertAST(ast)

    @Test
    fun testVariableDefinition() {
        val code = "var _s179 = 179"
        val expectedTree = AST(
                Block(blankPosition,
                        mutableListOf(VariableDefinition(
                                blankPosition,
                                Identifier(blankPosition, "_s179"),
                                Number(blankPosition, 179)))))
        val actualAST = removePositions(astFromCode(code))

        assertEquals(expectedTree, actualAST)
    }

    @Test
    fun testVariableAssignment() {
        val code = "_s179 = 179"
        val expectedTree = AST(
                Block(blankPosition,
                        mutableListOf(VariableAssignment(
                                blankPosition,
                                Identifier(blankPosition,"_s179"),
                                Number(blankPosition, 179)))))
        val actualAST = removePositions(astFromCode(code))

        assertEquals(expectedTree, actualAST)
    }

    @Test
    fun testBinaryOperation() {
        val code = "_s179 = 1 + 178"

        val expectedTree = AST(
                Block(blankPosition,
                        mutableListOf(VariableAssignment(
                                blankPosition,
                                Identifier(blankPosition,"_s179"),
                                BinaryExpression(
                                        blankPosition,
                                        Number(blankPosition, 1),
                                        Op.ADD,
                                        Number(blankPosition, 178))))))
        val actualAST = removePositions(astFromCode(code))

        assertEquals(expectedTree, actualAST)
    }

    @Test
    fun testFunctionDefinition() {
        val code = "fun sum(a, b) {}"

        val expectedFunctionBody = BracedBlock(blankPosition,
                Block(blankPosition, mutableListOf()))

        val expectedTree = AST(
                Block(blankPosition,
                        mutableListOf(FunctionDefinition(
                                blankPosition,
                                Identifier(blankPosition, "sum"),
                                mutableListOf(
                                        Identifier(blankPosition, "a"),
                                        Identifier(blankPosition,"b")),
                                expectedFunctionBody))))
        val actualAST = removePositions(astFromCode(code))

        assertEquals(expectedTree, actualAST)
    }

    @Test
    fun testFunctionCall() {
        val code = "sum(178, 1)"

        val expectedTree = AST(
                Block(blankPosition,
                        mutableListOf(FunctionCall(
                                blankPosition,
                                Identifier(blankPosition, "sum"),
                                mutableListOf(
                                        Number(blankPosition, 178),
                                        Number(blankPosition, 1)
                                )))))
        val actualAST = removePositions(astFromCode(code))

        assertEquals(expectedTree, actualAST)
    }

    @Test
    fun testWhileCycle() {
        val code = "while (1) {}"

        val expectedTree = AST(
                Block(blankPosition,
                        mutableListOf(WhileCycle(
                                blankPosition,
                                Number(blankPosition,1),
                                BracedBlock(blankPosition,
                                        Block(blankPosition, mutableListOf()))))))
        val actualAST = removePositions(astFromCode(code))

        assertEquals(expectedTree, actualAST)
    }

    @Test
    fun testIfClause() {
        val code = "if (1) {}"

        val expectedTree = AST(
                Block(blankPosition,
                        mutableListOf(IfClause(
                                blankPosition,
                                Number(blankPosition,1),
                                BracedBlock(blankPosition,
                                        Block(blankPosition, mutableListOf())),
                                null))))
        val actualAST = removePositions(astFromCode(code))

        assertEquals(expectedTree, actualAST)
    }

    @Test
    fun testIfElseClause() {
        val code = "if (1) {} else {}"

        val expectedTree = AST(
                Block(blankPosition,
                        mutableListOf(IfClause(
                                blankPosition,
                                Number(blankPosition,1),
                                BracedBlock(blankPosition,
                                        Block(blankPosition, mutableListOf())),
                                BracedBlock(blankPosition,
                                        Block(blankPosition, mutableListOf()))))))
        val actualAST = removePositions(astFromCode(code))

        assertEquals(expectedTree, actualAST)
    }

    @Test
    fun testBracedExpression() {
        val code = "_s179 = (1)"

        val expectedTree = AST(
                Block(blankPosition,
                        mutableListOf(VariableAssignment(
                                blankPosition,
                                Identifier(blankPosition, "_s179"),
                                BracedExpression(
                                        blankPosition,
                                        Number(blankPosition,1))))))
        val actualAST = removePositions(astFromCode(code))

        assertEquals(expectedTree, actualAST)
    }

    private companion object {
        val blankPosition = Pair(-1, -1)
    }
}