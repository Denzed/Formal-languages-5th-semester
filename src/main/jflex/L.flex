import java.util.*;
import java.io.*;
import symbols.*;

%%

%public
%class LLexer
%unicode
%line
%column
%eofclose

%{
StringBuilder comment = new StringBuilder();

public static void main(String argv[]) {
    for (int i = 0; i < argv.length; i++) {
        try {
            System.err.println("Parsing ["+argv[i]+"]");
            LLexer l = new LLexer(new FileReader(argv[i]));
            StringBuilder sb = new StringBuilder();
            while (true) {
                Yytoken symb = l.yylex();
                if (symb == null) {
                    break;
                }
                sb = sb.append(symb).append("; ");
            }
            System.err.println("Finished successfully");
            System.out.println(sb.toString());
        } catch (Throwable e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}

public Keyword keyword(String type) {
    return new Keyword(type, yyline, yycolumn, yycolumn + yytext().length() - 1);
}

public Literal literal(String type, String value) {
    return new Literal(type, yyline, yycolumn, yycolumn + yytext().length() - 1, value);
}

public Operator operator(String type) {
    return new Operator(type, yyline, yycolumn, yycolumn + yytext().length() - 1);
}

public Separator separator(String type) {
    return new Separator(type, yyline, yycolumn, yycolumn + yytext().length() - 1);
}

public Identifier identifier(String name) {
    return new Identifier(name, yyline, yycolumn, yycolumn + yytext().length() - 1);
}

public Comment comment(String text) {
    return new Comment(text.trim(), yyline, yycolumn, yycolumn + yytext().length() - 1);
}
%}

// spaces and lines
LineTerminator = \r|\n|\r\n
Whitespace = {LineTerminator} | [ \t\f]

// comments
NotLineTerminator = [^\r\n]
CommentStart = "//"
CommentBody = {NotLineTerminator}*
CommentEnd = {LineTerminator}?

// identifiers
Identifier = [_a-z][_a-z0-9]*

// numeric literals
NumLiteral = ({Integer} | {NumLiteral1} | {NumLiteral2} | {NumLiteral3})

NumLiteral1 = {Digits} \. {Digits}? {ExponentPart}?
NumLiteral2 = \. {Digits} {ExponentPart}?
NumLiteral3 = {Digits} {ExponentPart}

ExponentPart = [eE] [\+-]? {Digits}

Sign = [+-]
Integer = 0 | [1-9] ({Digits}? | _+ {Digits})
Digits = {Digit} | {Digit} {DigitsAndUnderscores}? {Digit}
Digit = [0-9]
DigitsAndUnderscores = ({Digit} | _)+

UnexpectedCharacter = .

%x COMMENT
%%

<YYINITIAL> {
  // keywords
  "if"                           { return keyword("If"); }
  "then"                         { return keyword("Then"); }
  "else"                         { return keyword("Else"); }
  "while"                        { return keyword("While"); }
  "do"                           { return keyword("Do"); }
  "read"                         { return keyword("Read"); }
  "write"                        { return keyword("Write"); }
  "begin"                        { return keyword("Begin"); }
  "end"                          { return keyword("End"); }
    
  // operators
  ":="                           { return operator("Assign"); }
  "+"                            { return operator("Plus"); }
  "-"                            { return operator("Minus"); }
  "*"                            { return operator("Mult"); }
  "/"                            { return operator("Div"); }
  "%"                            { return operator("Mod"); }
  "=="                           { return operator("Eq"); }
  "!="                           { return operator("NotEq"); }
  ">"                            { return operator("GT"); }
  ">="                           { return operator("GTEq"); }
  "<"                            { return operator("LT"); }
  "<="                           { return operator("LTEq"); }
  "&&"                           { return operator("LAnd"); }
  "||"                           { return operator("LOr"); }

  // boolean literals
  "true"                         { return literal("Bool", Boolean.toString(true)); }
  "false"                        { return literal("Bool", Boolean.toString(false)); }

  // numeric literals
  {NumLiteral}                   { return literal("Num", Float.toString(new Float(yytext().replaceAll("_", "")))); }
  
  // identifiers
  {Identifier}                   { return identifier(yytext()); }

  // comment
  {CommentStart}                 { yybegin(COMMENT); }
  
  // separators
  "("                            { return separator("LParen"); }
  ")"                            { return separator("RParen"); }
  ";"                            { return separator("Colon"); }

  // whitespaces
  {Whitespace}                   { }

  // errors
  {UnexpectedCharacter}          { throw new Error(String.format("Unexpected character <%s> at (%d, %d)", yytext(), yyline, yycolumn)); }
}

<COMMENT> {
  {CommentBody}                  { return comment(yytext()); }
  {CommentEnd}                   { yybegin(YYINITIAL); }

  // errors
  {UnexpectedCharacter}          { throw new Error(String.format("Unexpected character <%s> at (%d, %d)", yytext(), yyline, yycolumn)); }
}