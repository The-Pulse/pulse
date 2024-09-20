grammar Pulse;

prog: (stat NEWLINE*)* stat?;

stat: istc
    | expr
    | VAR ASG expr
    | NEWLINE
    ;

istc: PRINT muex
    | IMPORT muex
    | ifst
    | forst
    | whlst
    | funst
    ;

muex: expr (COMMA expr)*;
muvar: VAR (COMMA VAR)*;

ifst: IF expr NEWLINE* blck (NEWLINE* ELSE (ifst|blck))?;
forst: FOR inExpr NEWLINE* blck;
whlst: WHILE eqlExpr NEWLINE* blck;
funst: FUNCTION VAR (LPAREN muvar RPAREN)? NEWLINE* blck;


inExpr: LPAREN? VAR IN ranExpr RPAREN?
      | LPAREN? VAR DICASG VAR IN ranExpr RPAREN?;

blck: LBRACE stat* RBRACE;

dict: DICT NEWLINE* (dicasg NEWLINE* (COMMA NEWLINE* dicasg NEWLINE*)*)? NEWLINE* RBRACE;
dicasg: VAR NEWLINE* (DICASG|ASG) NEWLINE* expr;

list: LBRACE NEWLINE* (expr NEWLINE* (COMMA NEWLINE* expr NEWLINE*)*)? RBRACE;

funcall: VAR LPAREN muex? RPAREN;

expr: ranExpr;

ranExpr: eqlExpr | addExpr RANGE addExpr;
eqlExpr: orExpr (EQL orExpr
                 | NEQ orExpr
                 | STS orExpr
                 | SEQ orExpr
                 | IFS orExpr
                 | IEQ orExpr)*;
orExpr: andExpr (OR andExpr)*;
andExpr: xorExpr (AND xorExpr)*;
xorExpr: addExpr (XOR addExpr)*;
addExpr: mulExpr ((ADD|SUB) mulExpr)*;
mulExpr: powExpr ((MUL|DIV|MOD) powExpr)*;
powExpr: funExpr (POW funExpr)*;
funExpr: funcall | base;

base: list
    | dict
    | STRING
    | INT
    | FLOAT
    | BOOL
    | NULL
    | LPAREN expr RPAREN
    | VAR
    ;

// Keywords
FUNCTION: 'function' WS;

IMPORT: 'import' WS;

PRINT: 'print' WS;

IF: 'if';
ELSE: 'else';

FOR: 'for';
IN: 'in';

WHILE: 'while';

// Range
RANGE: '..' | WS 'to' WS;

// Lists and Dictionaries
DICT: 'd' LBRACE;
DICASG: '->' | ':';

// Types
STRING: '"' ANY '"';
BOOL: 'true'|'false';
NULL: 'null';
VAR: LETTER+;
INT: SUB? DIGIT+;
FLOAT: SUB? DIGIT+ '.' DIGIT+;

// Characters
NEWLINE: [\r\n]+;
ANY: .*?;

// Fragments
fragment DIGIT: [0-9];
fragment LETTER: [a-zA-Z];

// Boolean operators
AND: '&&' | WS 'and' WS;
OR: '||' | WS 'or' WS;
XOR: '^^' | WS 'xor' WS;

// Boolean equality operators
EQL: '==';
NEQ: '!=';
STS: '>';
SEQ: '>=';
IFS: '<';
IEQ: '<=';

// Arithmetic operators
POW: '^' | '**' | WS 'pow' WS;
MOD: '%' | WS 'mod' WS;
MUL: '*';
DIV: '/';
ADD: '+';
SUB: '-';
ASG: '=';

// Priority operators
LPAREN: '(';
RPAREN: ')';

// Blocks
LBRACE: '{';
RBRACE: '}';


// Multi Values
COMMA: ',';

// SKIP AND HIDDEN-CHANNEL rules
WS: [ \t]+                  -> channel(HIDDEN);

ILCOM: '//' ANY NEWLINE     -> channel(HIDDEN);
MLCOM: '/*' ANY '*/'        -> channel(HIDDEN);