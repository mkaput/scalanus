grammar Scalanus;

//////////////////////////////////////////////////////////////////////////////
//  Grammar
//////////////////////////////////////////////////////////////////////////////


init : '{' value ( ',' value )* '}' ;

value : init
      | INT_LIT
      ;


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


// Unit literal

UNIT : '()' ;


// Identifiers

IDENT : [\p{XID_Start}] [\p{XID_Continue}]*
      | '_' [\p{XID_Continue}]+
      ;


// Whitespace & comments

WHITE_SPACE   : [ \t\r\n]+                       -> channel(HIDDEN) ;

BLOCK_COMMENT : '/*' ( BLOCK_COMMENT | .)*? '*/' -> channel(HIDDEN) ;
LINE_COMMENT  : '//' .*? ( '\n' | EOF )          -> channel(HIDDEN) ;


// Digit fragments

fragment HEX_DIGIT       : [a-fA-F0-9] ;
fragment OCT_DIGIT       : [0-7] ;
fragment DEC_DIGIT       : [0-9] ;
fragment NONZERO_DEC     : [1-9] ;
