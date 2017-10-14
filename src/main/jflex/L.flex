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
public static void main(String argv[]) {
    for (int i = 0; i < argv.length; i++) {
        try {
            System.out.println("Parsing ["+argv[i]+"]");
            LLexer l = new LLexer(new FileReader(argv[i]));
            StringBuilder sb = new StringBuilder();
            while (true) {
                Yytoken symb = l.yylex();
                if (symb == null) {
                    break;
                }
                sb = sb.append(symb).append("; ");
            }
            System.out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace(System.out);
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
%}

// spaces and lines
LineTerminator = \r|\n|\r\n
Whitespace = {LineTerminator} | [ \t\f]

// comments
NotLineTerminator = [^\r\n]
Comment = "//" {NotLineTerminator}* {LineTerminator}?

// identifiers
Identifier = [_a-z][_a-z0-9]*

// numeric literals
NumLiteral = {NumLiteral1} | {NumLiteral2} | {NumLiteral3}

NumLiteral1 = {Digits} \. {Digits}? {ExponentPart}?
NumLiteral2 = \. {Digits} {ExponentPart}?
NumLiteral3 = {Digits} {ExponentPart}

ExponentPart = [eE] [\+-]? {Digits}

Digits = {Digit} | {Digit} {DigitsAndUnderscores}? {Digit}
Digit = [0-9]
DigitsAndUnderscores = ({Digit} | _)+

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
    
  // boolean literals
  "true"                         { return literal("Bool", Boolean.toString(true)); }
  "false"                        { return literal("Bool", Boolean.toString(false)); }
  
  // separators
  "("                            { return separator("LParen"); }
  ")"                            { return separator("RParen"); }
  ";"                            { return separator("Colon"); }
  
  // operators
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

  // numeric literals
  {NumLiteral}                   { return literal("Num", Float.toString(new Float(yytext().replaceAll("_", "")))); }
  
  // comment
  {Comment}                      {}

  // whitespace
  {Whitespace}                   {}

  // identifiers
  {Identifier}                   { return identifier(yytext()); }
}

// errors
[^]                              { throw new Error(String.format("Illegal character <%s> at (%d, %d)", yytext(), yyline, yycolumn)); }