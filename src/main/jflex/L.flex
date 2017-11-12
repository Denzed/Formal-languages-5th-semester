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
NumLiteral = {Sign}? ({Integer} | {NumLiteral1} | {NumLiteral2} | {NumLiteral3})

NumLiteral1 = {Digits} \. {Digits}? {ExponentPart}?
NumLiteral2 = \. {Digits} {ExponentPart}?
NumLiteral3 = {Digits} {ExponentPart}

ExponentPart = [eE] [\+-]? {Digits}

Sign = [+-]
Integer = 0 | [1-9] ({Digits}? | _+ {Digits})
Digits = {Digit} | {Digit} {DigitsAndUnderscores}? {Digit}
Digit = [0-9]
DigitsAndUnderscores = ({Digit} | _)+

%x AFTER_LEXEM
%x COMMENT
%%

<YYINITIAL> {

  // keywords
  "if"                           { yybegin(AFTER_LEXEM); return keyword("If"); }
  "then"                         { yybegin(AFTER_LEXEM); return keyword("Then"); }
  "else"                         { yybegin(AFTER_LEXEM); return keyword("Else"); }
  "while"                        { yybegin(AFTER_LEXEM); return keyword("While"); }
  "do"                           { yybegin(AFTER_LEXEM); return keyword("Do"); }
  "read"                         { yybegin(AFTER_LEXEM); return keyword("Read"); }
  "write"                        { yybegin(AFTER_LEXEM); return keyword("Write"); }
  "begin"                        { yybegin(AFTER_LEXEM); return keyword("Begin"); }
  "end"                          { yybegin(AFTER_LEXEM); return keyword("End"); }
    
  // boolean literals
  "true"                         { yybegin(AFTER_LEXEM); return literal("Bool", Boolean.toString(true)); }
  "false"                        { yybegin(AFTER_LEXEM); return literal("Bool", Boolean.toString(false)); }
  
  // operators
  ":="                           { yybegin(AFTER_LEXEM); return operator("Assign"); }
  "+"                            { yybegin(AFTER_LEXEM); return operator("Plus"); }
  "-"                            { yybegin(AFTER_LEXEM); return operator("Minus"); }
  "*"                            { yybegin(AFTER_LEXEM); return operator("Mult"); }
  "/"                            { yybegin(AFTER_LEXEM); return operator("Div"); }
  "%"                            { yybegin(AFTER_LEXEM); return operator("Mod"); }
  "=="                           { yybegin(AFTER_LEXEM); return operator("Eq"); }
  "!="                           { yybegin(AFTER_LEXEM); return operator("NotEq"); }
  ">"                            { yybegin(AFTER_LEXEM); return operator("GT"); }
  ">="                           { yybegin(AFTER_LEXEM); return operator("GTEq"); }
  "<"                            { yybegin(AFTER_LEXEM); return operator("LT"); }
  "<="                           { yybegin(AFTER_LEXEM); return operator("LTEq"); }
  "&&"                           { yybegin(AFTER_LEXEM); return operator("LAnd"); }
  "||"                           { yybegin(AFTER_LEXEM); return operator("LOr"); }

  // numeric literals
  {NumLiteral}                   { yybegin(AFTER_LEXEM); return literal("Num", Float.toString(new Float(yytext().replaceAll("_", "")))); }

  // identifiers
  {Identifier}                   { yybegin(AFTER_LEXEM); return identifier(yytext()); }

  // comment
  {CommentStart}                 { yybegin(COMMENT); }
  
  // separators
  "("                            { return separator("LParen"); }
  ")"                            { return separator("RParen"); }
  ";"                            { return separator("Colon"); }

  // whitespaces
  {Whitespace}                   { }
}

<AFTER_LEXEM> {
  // comment
  {CommentStart}                 { yybegin(COMMENT); }

  // separators
  "("                            { yybegin(YYINITIAL); return separator("LParen"); }
  ")"                            { yybegin(YYINITIAL); return separator("RParen"); }
  ";"                            { yybegin(YYINITIAL); return separator("Colon"); }

  // whitespaces
  {Whitespace}+                  { yybegin(YYINITIAL); }
}

<COMMENT> {
  {CommentBody}                  { return comment(yytext()); }
  {CommentEnd}                   { yybegin(YYINITIAL); }
}

// errors
[^]                              { throw new Error(String.format("Illegal character <%s> at (%d, %d)", yytext(), yyline, yycolumn)); }