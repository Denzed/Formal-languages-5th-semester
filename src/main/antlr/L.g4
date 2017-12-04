grammar L;

file
    :   block
        EOF
    ;

block
    :   (  statement
        )*
        statement
    |
    ;

bracedBlock
    :   CURLY_LPAREN block CURLY_RPAREN
    ;

CURLY_RPAREN
    :   '}'
    ;

CURLY_LPAREN
    :   '{'
    ;

statement
    :   functionDefinition
    |   functionCall
    |   variableDefinition
    |   whileCycle
    |   readStatement
    |   writeStatement
    |   ifClause
    |   variableAssignment
    ;

functionDefinition
    :   FUN_DEF identifier LPAREN parameterNames RPAREN bracedBlock
    ;

FUN_DEF
    :   'fun'
    ;

variableDefinition
    :   VAR_DEF identifier (ASSIGN expression)?
    ;

ASSIGN
    :   '='
    ;

VAR_DEF
    :   'var'
    ;

readStatement
    :   READ_KW identifier
    ;

READ_KW
    :   'read'
    ;

writeStatement
    :   WRITE_KW expression
    ;

WRITE_KW
    :   'write'
    ;

parameterNames
    :   ( identifier COMMA
        )*
        identifier
    |
    ;

COMMA
    :   ','
    ;

whileCycle
    :   WHILE_KW LPAREN expression RPAREN bracedBlock
    ;

WHILE_KW
    :   'while'
    ;

ifClause
    :   IF_KW LPAREN expression RPAREN bracedBlock
        ( ELSE_KW bracedBlock
        )?
    ;

IF_KW
    :   'if'
    ;

ELSE_KW
    :   'else'
    ;

variableAssignment
    :   identifier ASSIGN expression
    ;

expression
    :   binaryExpression
    |   atomicExpression
    ;

atomicExpression
    :   identifier
    |   literal
    |   bracedExpression
    ;

bracedExpression
    :   LPAREN expression RPAREN
    ;

functionCall
    :   identifier LPAREN parameters RPAREN
    ;

parameters
    :   ( expression COMMA
        )*
        expression
    |
    ;

identifier
    :   IDENTIFIER
    ;

IDENTIFIER
    :   ( LETTER
        | UNDERSCORE
        )
        ( LETTER
        | DIGIT
        | UNDERSCORE
        )*
    ;

literal
    :   number
    ;

number
    :   Number
    ;

binaryExpression
    :   binaryExpressionOfPrecedence14
    ;

binaryExpressionOfPrecedence5
    :   left = atomicExpression
        op = ( MUL | DIV | MOD )
        right = binaryExpressionOfPrecedence5   #op5Expr
    |   atomicExpression                        #atom5Expr
    ;

MOD
    :   '%'
    ;

DIV
    :   '/'
    ;

MUL
    :   '*'
    ;

binaryExpressionOfPrecedence6
    :   left = binaryExpressionOfPrecedence5
        op = ( ADD | SUB )
        right = binaryExpressionOfPrecedence6   #op6Expr
    |   binaryExpressionOfPrecedence5           #atom6Expr
    ;

SUB
    :   '-'
    ;

ADD
    :   '+'
    ;

binaryExpressionOfPrecedence8
    :   left = binaryExpressionOfPrecedence6
        op = ( LT | LE | GT | GE )
        right = binaryExpressionOfPrecedence8   #op8Expr
    |   binaryExpressionOfPrecedence6           #atom8Expr
    ;

GE
    :   '>='
    ;

GT
    :   '>'
    ;

LE
    :   '<='
    ;

LT
    :   '<'
    ;

binaryExpressionOfPrecedence9
    :   left = binaryExpressionOfPrecedence8
        op = ( EQ | NEQ )
        right = binaryExpressionOfPrecedence9   #op9Expr
    |   binaryExpressionOfPrecedence8           #atom9Expr
    ;

NEQ
    : '!='
    ;

EQ
    : '=='
    ;

binaryExpressionOfPrecedence13
    :   left = binaryExpressionOfPrecedence9
        op = LAND
        right = binaryExpressionOfPrecedence13  #op13Expr
    |   binaryExpressionOfPrecedence9           #atom13Expr
    ;

LAND
    :   '&&'
    ;

binaryExpressionOfPrecedence14
    :   left = binaryExpressionOfPrecedence13
        op = LOR
        right = binaryExpressionOfPrecedence14  #op14Expr
    |   binaryExpressionOfPrecedence13          #atom14Expr
    ;

LOR
    :   '||'
    ;

fragment UNDERSCORE
    : '_'
    ;

fragment DIGIT
    : '0'..'9'
    ;

fragment NON_ZERO_DIGIT
    : '1'..'9'
    ;

fragment LETTER
    :   ('a'..'z')
    |   ('A'..'Z')
    ;

RPAREN
    :   ')'
    ;

LPAREN
    :   '('
    ;

Number
    :   NON_ZERO_DIGIT DIGIT*
    ;

WS
    :   (' ' | '\t' | '\r'| '\n') -> channel(HIDDEN)
    ;

COMMENT
    :
    (
    LINE_COMMENT
    |
    MULTILINE_COMMENT
    ) -> channel(HIDDEN)
    ;

fragment LINE_COMMENT
    : '//' ~[\r\n]*
    ;

fragment MULTILINE_COMMENT
    : '/*'
    NOT_MULTILINE_COMMENT_END
    (
    MULTILINE_COMMENT
    NOT_MULTILINE_COMMENT_END
    )*
    '*/'
    ;

fragment NOT_MULTILINE_COMMENT_END
    : ( ~'*' | ( '*'+ ~[/*]) )* '*'*
    ;