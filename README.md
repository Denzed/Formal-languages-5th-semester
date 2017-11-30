# L
## Syntax
block = statement\*

statement = functionDefinition | functionCall | variableDefinition | whileCycle | readStatement | writeStatement | ifClause | variableAssignment

functionDefinition = 'fun' identifier '(' parameterNames ')' '{' block '}'

variableDefinition = 'var' identifier ('=' expression)?

readStatement = 'read' identifier

writeStatement = 'write' expression

parameterNames = identifier {','}

whileCycle = 'while' (expression) '{' block '}'

ifClause = 'if' (expression) '{' block '}' ('else' '{' block '}')?

variableAssignment = identifier '=' expression

expression = binaryExpression | atomicExpression

atomicExpression = identifier | literal | '(' expression ')'

functionCall = identifier '(' parameters ')'

parameters = expression {','}

identifier = \[a-zA-Z_\]\[a-zA-Z0-9_\]\*

literal = number

number = \[1-9\]\[0-9\]*

binaryExpression(prec) = binaryExpression(prec - 1) op(prec) binaryExpression(prec) | binaryExpression(prec - 1)
binaryExpression(minPrec) = atomicExpression op(minPrec) binaryExpression(minPrec)

Operators have precedences like those in C

op = AR_OP | LOG_OP

AR_OP = '+' | '-' | '\*' | '/' | '%'

LOG_OP = '==' | '!=' | '>' | '>=' | '<' | '<=' | '&&' | '||'

comment = line_comment | multiline_comment

line_comment = '//' ~[\r\n]*

    multiline_comment = '/\*' ^'\*' '\*/'


## Parser

Parses input file with L code to AST and writes it to output file in PlantUML format.
PlantUML was preferred over DOT because of easier node attribute definition.
Code block code is inside the corresponding node because we can preserve order of statements in that case. It also sounds pretty natural :)

### Building
run `./gradlew jar`
Resulting JAR-file can be found at `./build/libs/hw07-1.0-SNAPSHOT.jar`

### Running
Usage: `java -jar hw07-1.0-SNAPSHOT.jar command inputFile [outputFile={inputFile}.out]`
Commands:
    lex -- splits given code to tokens
    ast -- builds AST from given code and outputs it as an SVG image
       if output file's extension is ".svg" or a PlantUML diagram instead.
    help -- outputs this text

### Example output

input
```Kotlin
fun y(z) {
    var x = 1
    read x
    write z + x
}
```
will yield
```PlantUML
@startuml
state "code block" as 0 {
  state "function definition" as 1
  1: name: "y"
  1: position: (0, 0)
  1: z
  state "code block" as 2 {
    state "variable definition" as 3
    3: name: "x"
    3: position: (1, 4)
    state "number" as 4
    4: position: (1, 12)
    4: value: 1
    3 --> 4: value
    ||
    state "read" as 5
    5: position: (2, 4)
    5: identifier: "x"
    ||
    state "write" as 6
    6: position: (3, 4)
    state "binary expression" as 7
    7: position: (3, 10)
    7: operator: "+"
    state "identifier reference" as 8
    8: name: "z"
    8: position: (3, 10)
    7 --> 8: left
    state "identifier reference" as 9
    9: name: "x"
    9: position: (3, 14)
    7 --> 9: right
    6 --> 7: value
  }
  2: position: (1, 4)
  1 --> 2: body
}
0: position: (0, 0)
@enduml
```
which encodes the following diagram
![](http://www.gravizo.com/svg?@startuml;state%20%22code%20block%22%20as%200%20{;state%20%22function%20definition%22%20as%201;1:%20name:%20%22y%22;1:%20position:%20%280,%200%29;1:%20z;state%20%22code%20block%22%20as%202%20{;state%20%22variable%20definition%22%20as%203;3:%20name:%20%22x%22;3:%20position:%20%281,%204%29;state%20%22number%22%20as%204;4:%20position:%20%281,%2012%29;4:%20value:%201;3%20--%3E%204:%20value;||;state%20%22read%22%20as%205;5:%20position:%20%282,%204%29;5:%20identifier:%20%22x%22;||;state%20%22write%22%20as%206;6:%20position:%20%283,%204%29;state%20%22binary%20expression%22%20as%207;7:%20position:%20%283,%2010%29;7:%20operator:%20%22+%22;state%20%22identifier%20reference%22%20as%208;8:%20name:%20%22z%22;8:%20position:%20%283,%2010%29;7%20--%3E%208:%20left;state%20%22identifier%20reference%22%20as%209;9:%20name:%20%22x%22;9:%20position:%20%283,%2014%29;7%20--%3E%209:%20right;6%20--%3E%207:%20value;};2:%20position:%20%281,%204%29;1%20--%3E%202:%20body;};0:%20position:%20%280,%200%29;@enduml)