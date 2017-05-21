grammar Scalanus;

//////////////////////////////////////////////////////////////////////////////
//  Grammar
//////////////////////////////////////////////////////////////////////////////


// Top-level program rule

program : stmts EOF;


// Paths

path : IDENT ;


// Various building blocks

block   : '{' stmts '}' ;

literal : CHAR_LIT
        | STRING_LIT
        | FLOAT_LIT
        | INT_LIT
        | BOOL_LIT
        | unit
        ;

unit    : '(' ')' ;


// Statements

stmt  : pattern '=' expr  # assignStmt
      | item              # itemStmt
      | expr              # exprStmt
      ;

// FIXME Splitting with new line (issue #1)
stmts : ';'* ( stmt ( ';'* stmt)* )? ';'* ;


// Patterns

pattern        : simplePattern ( ',' simplePattern )* ;

simplePattern : '_'                # wildcardPattern
              | expr '.' IDENT     # memAccPattern
              | expr '[' expr ']'  # idxAccPattern
              | path               # pathPattern
              | '^' expr           # valuePattern
              ;


// Items

item : 'fn' IDENT '(' pattern? ')' block  # fnItem
     | block                              # blockItem
     ;


// Expressions

expr : literal                          # literalExpr
     | path                             # pathExpr

     | tuple                            # tupleExpr
     | dict                             # dictExpr

     | block                            # blockExpr

     | '(' expr ')'                     # parenExpr

     | expr '.' IDENT                   # memAccExpr
     | expr '[' expr ']'                # idxAccExpr

     | expr '(' fnCallArgs? ')'         # fnCallExpr

     | expr '++'                        # postfixIncrExpr
     | expr '--'                        # postfixDecrExpr

     | <assoc=right> '!' expr           # notExpr
     | <assoc=right> '~' expr           # bnotExpr
     | <assoc=right> '++' expr          # prefixIncrExpr
     | <assoc=right> '--' expr          # prefixDecrExpr
     | <assoc=right> '+' expr           # unaryPlusExpr
     | <assoc=right> '-' expr           # unaryMinusExpr

     | <assoc=right> expr '**' expr     # powExpr

     | expr '*' expr                    # mulExpr
     | expr '/' expr                    # divExpr
     | expr 'mod' expr                  # modExpr

     | expr '+' expr                    # addExpr
     | expr '-' expr                    # subExpr

     | expr '<<' expr                   # bitshiftLeftExpr
     | expr '>>' expr                   # bitshiftRightExpr

     | expr '&' expr                    # bandExpr
     | expr '^' expr                    # xorExpr
     | expr '|' expr                    # borExpr

     | expr '==' expr                   # eqExpr
     | expr '!=' expr                   # neqExpr
     | expr '<' expr                    # ltExpr
     | expr '>' expr                    # gtExpr
     | expr '<=' expr                   # lteqExpr
     | expr '>=' expr                   # gteqExpr

     | expr 'and' expr                  # andExpr
     | expr 'or' expr                   # orExpr

     | ifCond                           # ifExpr
     | forLoop                          # forExpr
     | whileLoop                        # whileExpr
     | loop                             # loopExpr

     | 'break'                          # breakExpr
     | 'continue'                       # continueExpr
     | 'return' expr                    # returnExpr
     ;


fnCallArgs : expr ( ',' expr )* ;


// Tuples

tuple : '(' expr ',' ( ( expr ',' )* expr ','? )? ')' ;


// Dicts

dict     : '#' '{' ( dictElem ( ',' dictElem )* ','? )? '}' ;
dictElem : expr '=' expr ;


// If expressions

ifCond   : 'if' expr block elseTail? ;
elseTail : 'else' ( ifCond | block ) ;


// Loops

forLoop   : 'for' pattern 'in' expr block ;
whileLoop : 'while' expr block ;
loop      : 'loop' block ;


//////////////////////////////////////////////////////////////////////////////
//  Lexer
//////////////////////////////////////////////////////////////////////////////

// Keywords

AND        : 'and' ;
AS         : 'as' ;
BREAK      : 'break' ;
CASE       : 'case' ;
CLASS      : 'class' ;
CONST      : 'const' ;
CONTINUE   : 'continue' ;
DO         : 'do' ;
ELSE       : 'else' ;
ENUM       : 'enum' ;
FN         : 'fn' ;
FOR        : 'for' ;
IF         : 'if' ;
IMPORT     : 'import' ;
IN         : 'in' ;
LOOP       : 'loop' ;
MOD        : 'mod' ;
OR         : 'or' ;
RETURN     : 'return' ;
TRAIT      : 'trait' ;
TYPE       : 'type' ;
UNDERSCORE : '_' ;
WHILE      : 'while' ;
YIELD      : 'yield' ;


// Symbols & Operators

ADD         : '+' ;
AMP         : '&' ;
CARET       : '^' ;
COMMA       : ',' ;
DIV         : '/' ;
DOT         : '.' ;
EQ          : '=' ;
EQEQ        : '==' ;
EXCL        : '!' ;
EXCLEQ      : '!=' ;
GT          : '>' ;
GTEQ        : '>=' ;
GTGT        : '>>' ;
HASH        : '#' ;
LBRACE      : '{' ;
LBRACK      : '[' ;
LPAREN      : '(' ;
LT          : '<' ;
LTEQ        : '<=' ;
LTLT        : '<<' ;
MINUS_MINUS : '--' ;
MUL         : '*' ;
MUL_MUL     : '**' ;
PIPE        : '|' ;
PLUS_PLUS   : '++' ;
RBRACE      : '}' ;
RBRACK      : ']' ;
RPAREN      : ')' ;
SEMICOLON   : ';' ;
SUB         : '-' ;
TILDE       : '~' ;


// Character and string literals

CHAR_LIT             : '\'' CHAR_BODY '\'' ;
STRING_LIT           : '"' STRING_BODY* '"' ;

fragment
CHAR_BODY            : ~['\\]
                     | '\\' ( '\'' | COMMON_ESCAPE )
                     ;

fragment
STRING_BODY          : ~["\\]
                     | '\\' ( '"' | COMMON_ESCAPE )
                     ;

fragment
COMMON_ESCAPE        : [\\nrt0]
                     | 'x' HEX_DIGIT HEX_DIGIT
                     | 'u' '{' UNICODE_ESCAPE_INNER '}'
                     ;

fragment
UNICODE_ESCAPE_INNER : HEX_DIGIT HEX_DIGIT? HEX_DIGIT? HEX_DIGIT? HEX_DIGIT? HEX_DIGIT? ; // Wtf Antlr


// Number literals

INT_LIT      : '0b' ( '1' | '0' | '_' )+
             | '0o' ( OCT_DIGIT | '_' )+
             | '0x' ( HEX_DIGIT | '_' )+
             |      ( DEC_DIGIT | '_' )+
             ;

FLOAT_LIT    : ( DEC_DIGIT | '_' )* FLOAT_SUFFIX ;

fragment
FLOAT_SUFFIX : '.' ( DEC_DIGIT | '_' )+ EXPONENT?
             | EXPONENT
             ;

fragment
EXPONENT     : [Ee] [\-+]? ( DEC_DIGIT | '_' )+ ;


// Boolean literals

BOOL_LIT : 'True' | 'False' ;


// Identifiers

IDENT : [\p{XID_Start}] [\p{XID_Continue}]*
      | '_' [\p{XID_Continue}]+
      ;


// Whitespace & comments

SPACE   : [ \t]+    -> channel(HIDDEN) ;
NEWLINE : [\r\n]+   -> channel(HIDDEN) ;

BLOCK_COMMENT : '/*' ( BLOCK_COMMENT | . )*? ( '*/' | EOF ) -> channel(HIDDEN) ;
LINE_COMMENT  : '//' .*? ( '\n' | EOF )                     -> channel(HIDDEN) ;


// Digit fragments

fragment HEX_DIGIT       : [a-fA-F0-9] ;
fragment OCT_DIGIT       : [0-7] ;
fragment DEC_DIGIT       : [0-9] ;
fragment NONZERO_DEC     : [1-9] ;
