grammar Language;

inicio: programa EOF;

programa
    : sentencia programa
    | // lambda
    ;

sentencia
    : declaracion PUNTO_COMA
    | asignacion PUNTO_COMA
    | print PUNTO_COMA
    | if
    ;

declaracion
    : INT ID IGUAL expresion
    ;

asignacion
    : ID IGUAL expresion
    ;

print
    : PRINT PAREN_ABIERTO expresion PAREN_CERRADO
    ;

if
    : IF PAREN_ABIERTO expresion PAREN_CERRADO bloque
    ;

bloque
    : LLAVE_ABIERTA programa LLAVE_CERRADA
    ;

// Las etiquetas (#) se usan para que antlr cree metodos con esos nombres y sea mas facil redefinirlos.
expresion
    : expresion SUMA expresion      # ExpSuma
    | expresion RESTA expresion     # ExpResta
    | NUM                           # ExpNum
    | ID                            # ExpId
    | TRUE                          # ExpBoolean
    | FALSE                         # ExpBoolean
    ;


INT: 'int';
PRINT: 'print';
IF: 'if';
TRUE: 'true';
FALSE: 'false';

PUNTO_COMA: ';';
IGUAL: '=';
PAREN_ABIERTO: '(';
PAREN_CERRADO: ')';
LLAVE_ABIERTA: '{';
LLAVE_CERRADA: '}';
SUMA: '+';
RESTA: '-';

ID: [a-z]+;
NUM: [0-9]+;

WS: [ \t\r\n]+ -> skip;