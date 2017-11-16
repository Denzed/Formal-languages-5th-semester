package parser.ast

import org.antlr.v4.runtime.ParserRuleContext
import parser.LBaseVisitor
import parser.LParser

private fun getStartPosition(context: ParserRuleContext): Pair<Int,Int> {
    return Pair(context.start.line - 1, context.start.charPositionInLine)
}

object ASTBuilder : LBaseVisitor<ASTNode>() {
    override fun visitBlock(context: LParser.BlockContext): Block =
            Block(
                    getStartPosition(context),
                    context.statement().map(StatementBuilder::visitStatement)
            )

    override fun visitBracedBlock(context: LParser.BracedBlockContext): BracedBlock =
            BracedBlock(
                    getStartPosition(context),
                    visitBlock(context.block())
            )
}

private object StatementBuilder : LBaseVisitor<Statement>() {
    private fun makeExpression(context: LParser.ExpressionContext): Expression {
        return ExpressionBuilder.visitExpression(context)
    }

    private fun makeIdentifier(context: LParser.IdentifierContext): Identifier {
        return ExpressionBuilder.visitIdentifier(context)
    }

    override fun visitFunctionDefinition(
            context: LParser.FunctionDefinitionContext
    ): FunctionDefinition {
        val identifier = makeIdentifier(context.identifier())
        val arguments = context
                .parameterNames()
                .identifier()
                .map(this::makeIdentifier)
        return FunctionDefinition(
                getStartPosition(context),
                identifier,
                arguments,
                ASTBuilder.visitBracedBlock(context.bracedBlock()))
    }

    override fun visitVariableDefinition(
            context: LParser.VariableDefinitionContext
    ): VariableDefinition {
        val identifier = makeIdentifier(context.identifier())
        val expression = context.expression()?.let { makeExpression(it) }
        return VariableDefinition(
                getStartPosition(context),
                identifier,
                expression
        )
    }

    override fun visitWhileCycle(context: LParser.WhileCycleContext): WhileCycle {
        return WhileCycle(
                getStartPosition(context),
                makeExpression(context.expression()),
                ASTBuilder.visitBracedBlock(context.bracedBlock()))
    }

    override fun visitIfClause(context: LParser.IfClauseContext): IfClause {
        val condition = makeExpression(context.expression())
        val thenBlock = ASTBuilder.visitBracedBlock(context.bracedBlock(0))
        val elseBlock = if (context.bracedBlock(1) != null) {
            ASTBuilder.visitBracedBlock(context.bracedBlock(1))
        } else {
            null
        }
        return IfClause(
                getStartPosition(context),
                condition,
                thenBlock,
                elseBlock
        )
    }

    override fun visitVariableAssignment(
            context: LParser.VariableAssignmentContext
    ): VariableAssignment {
        val identifier = makeIdentifier(context.identifier())
        val expression = makeExpression(context.expression())

        return VariableAssignment(
                getStartPosition(context),
                identifier,
                expression)
    }

    override fun visitReadStatement(
            context: LParser.ReadStatementContext
    ): ReadStatement {
        return ReadStatement(
                getStartPosition(context),
                makeIdentifier(context.identifier())
        )
    }

    override fun visitWriteStatement(
            context: LParser.WriteStatementContext
    ): WriteStatement {
        return WriteStatement(
                getStartPosition(context),
                makeExpression(context.expression())
        )
    }

    override fun visitFunctionCall(context: LParser.FunctionCallContext): FunctionCall {
        val identifier = makeIdentifier(context.identifier())
        val arguments = context
                .parameters()
                .expression()
                .map { subContext -> makeExpression(subContext) }

        return FunctionCall(getStartPosition(context), identifier, arguments)
    }
}

private object ExpressionBuilder : LBaseVisitor<Expression>() {
    private fun makeBinaryExpression(
            position: Pair<Int,Int>,
            left: Expression,
            opString: String,
            right: Expression
    ): BinaryExpression =
            BinaryExpression(
                    position,
                    left,
                    BinaryExpression.ops[opString] ?:
                            error("Unknown operator: \"$opString\""),
                    right
            )

    override fun visitBracedExpression(context: LParser.BracedExpressionContext): BracedExpression {
        return BracedExpression(getStartPosition(context), visitExpression(context.expression()))
    }

    override fun visitLiteral(context: LParser.LiteralContext): Literal {
        return Number(getStartPosition(context), context.number().text.toInt())
    }

    override fun visitIdentifier(context: LParser.IdentifierContext): Identifier {
        return Identifier(getStartPosition(context), context.text)
    }

    override fun visitOp14Expr(context: LParser.Op14ExprContext): BinaryExpression =
            makeBinaryExpression(
                    getStartPosition(context),
                    visit(context.left),
                    context.op.text,
                    visit(context.right)
            )

    override fun visitOp13Expr(context: LParser.Op13ExprContext): BinaryExpression =
            makeBinaryExpression(
                    getStartPosition(context),
                    visit(context.left),
                    context.op.text,
                    visit(context.right)
            )

    override fun visitOp9Expr(context: LParser.Op9ExprContext): BinaryExpression =
            makeBinaryExpression(
                    getStartPosition(context),
                    visit(context.left),
                    context.op.text,
                    visit(context.right)
            )

    override fun visitOp8Expr(context: LParser.Op8ExprContext): BinaryExpression =
            makeBinaryExpression(
                    getStartPosition(context),
                    visit(context.left),
                    context.op.text,
                    visit(context.right)
            )

    override fun visitOp6Expr(context: LParser.Op6ExprContext): BinaryExpression =
            makeBinaryExpression(
                    getStartPosition(context),
                    visit(context.left),
                    context.op.text,
                    visit(context.right)
            )

    override fun visitOp5Expr(context: LParser.Op5ExprContext): BinaryExpression =
            makeBinaryExpression(
                    getStartPosition(context),
                    visit(context.left),
                    context.op.text,
                    visit(context.right)
            )
}