grammar Pulse;

prog: stat*;

stat: (st_import SEMICOLON)
    | (asg_expression SEMICOLON)
    | (function_call SEMICOLON)
    ;


// STATEMENTS
st_import: (IMPORT import_entities_array FROM STRING)
         | (IMPORT VARIABLE FROM STRING USING VARIABLE)
         ;

import_entities_array: LBRACKET (VARIABLE COMMA)* VARIABLE? RBRACKET;


// EXPRESSIONS
asg_expression: VARIABLE ASG add_sous_expression;

add_sous_expression: (mul_div_mod_expression (ADD add_sous_expression)?)
                   | (mul_div_mod_expression (HYP add_sous_expression)?)
                   ;

mul_div_mod_expression: (pow_expression (MOD mul_div_mod_expression)?)
                      | (pow_expression (MUL mul_div_mod_expression)?)
                      | (pow_expression (DIV mul_div_mod_expression)?)
                      ;

pow_expression: or_expression (POW pow_expression)?;

or_expression: xor_expression (OR or_expression)?;
xor_expression: and_expression (XOR xor_expression)?;
and_expression: expression (AND and_expression)?;

comp_expression: (expression (LESS comp_expression)?)
               | (expression (LESS_EQ comp_expression)?)
               | (expression (GREATER comp_expression)?)
               | (expression (GREATER_EQ comp_expression)?)
               ;

expression: STRING
          | VARIABLE
          | INTEGER
          | FLOAT
          | LPAREN (asg_expression|function_call) RPAREN
          ;


// FUNCTIONS - OOP
function_call: VARIABLE function_arguments;
function_arguments: LPAREN ((asg_expression COMMA)* asg_expression)? RPAREN;


// KEYWORDS
IMPORT: 'import';
FROM: 'from';
USING: 'using';

MOD_KW: 'mod';
POW_KW: 'pow';


// NATIVE TYPES SYNTAX
STRING: (DBLQUOTE ANY DBLQUOTE)
      | (SMPQUOTE ANY SMPQUOTE)
      ;

VARIABLE: (LETTER|UNDSCORE)+ (LETTER|DIGIT|UNDSCORE)*;

INTEGER: DIGIT+;

FLOAT: (DIGIT* PERIOD DIGIT+)
     | (DIGIT+ PERIOD DIGIT*)
     ;


// SPECIAL
ANY: .*?;


// CHARACTERS
COMMA: ',';
SEMICOLON: ';';
PERIOD: '.';

LPAREN: '(';
RPAREN: ')';

LBRACKET: '[';
RBRACKET: ']';

LBRACE: '{';
RBRACE: '}';

DBLQUOTE: '"';
SMPQUOTE: '\'';

// Boolean comparison operators
EQUALS: '==';
NOT_EQUALS: '!=';

LESS: '<';
LESS_EQ: '<=';
GREATER: '>';
GREATER_EQ: '>=';

// Boolean operators
AND: '&&' | (WS 'and' WS);
OR: '||' | (WS 'or' WS);
XOR: '^' | (WS 'xor' WS);

// Arithmetic operators
ADD: '+';
HYP: '-';
MUL: '*';
DIV: '/';
MOD: '%' | (WS MOD_KW WS);
POW: '**' | (WS POW_KW WS);

UNDSCORE: '_';

NOT: '!';

ASG: '=';


// FRAGMENTS
fragment LETTER: [a-zA-Z];
fragment DIGIT: [0-9];


// SKIP AND HIDDEN-CHANNEL rules
NEWLINE: [\n\r]             -> channel(HIDDEN);  // TODO: take a look to alternative of HIDDEN channel!!
WS: [ \t]+                  -> channel(HIDDEN);

ILCOM: '//' ANY NEWLINE     -> channel(HIDDEN);
MLCOM: '/*' ANY '*/'        -> channel(HIDDEN);