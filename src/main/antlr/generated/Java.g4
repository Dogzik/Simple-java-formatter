grammar Java;

WHITESPACE:     [ \t\r\n\u000C]+ -> skip;

fragment PUBLIC:         'public';
fragment PRIVATE:        'private';
fragment PROTECTED:      'protected';

ACCESS_MOD: PUBLIC | PRIVATE | PROTECTED;

CLASS:          'class';

IMPLEMENTS:     'implements';
EXTENDS:        'extends';
FINAL:          'final';
IF:             'if';
ELSE:           'else';
SUPER:          'super';
WHILE:          'while';
NEW:            'new';
RETURN:         'return';
BREAK:          'break';
CONTINUE:       'continue';
FOR:            'for';

L_SQ_PAREN:     '[';
R_SQ_PAREN:     ']';

fragment INT:            'int';
fragment BYTE:           'byte';
fragment LONG:           'long';
fragment FLOAT:          'float';
fragment DOUBLE:         'double';
fragment BOOLEAN:        'boolean';
PRIMITIVE_TYPE: INT | BYTE | LONG | FLOAT | DOUBLE | BOOLEAN;

fragment Letter: [a-zA-Z];
IDENTIFIER:     Letter (Letter | [0-9$_])*;


fragment NULL:      'null';
LITERAL:        NULL | ([0-9]+) | '"'.*?'"';


BINARY_OP:       '+' | '-' | '*' | '/' | '^' | '&' | '|' | '==' | '!=' | '>' | '<' | '>=' | '<=' | '&&' | '||';
NOT:            '!';

type: IDENTIFIER | PRIMITIVE_TYPE;

typeList : type (',' type)*;

classDeclaration: ACCESS_MOD? FINAL? CLASS IDENTIFIER (EXTENDS type)? (IMPLEMENTS typeList)? '{' classBody '}';

classBody: (field | constructor | function)*;

field: ACCESS_MOD? FINAL? type (L_SQ_PAREN R_SQ_PAREN)* IDENTIFIER ';';

function: ACCESS_MOD? FINAL? type IDENTIFIER '(' argumentList ')' '{' functionBody '}';

argument: FINAL? type IDENTIFIER;

argumentList: (argument (',' argument)*)?;

functionParam: expression;

functionParamList: (functionParam (',' functionParam)*)?;

functionCall: IDENTIFIER '(' functionParamList ')';

classFunctionCall: IDENTIFIER '.' functionCall ('.' functionCall)*;

elemFunctionCall:   IDENTIFIER (L_SQ_PAREN expression R_SQ_PAREN)+;

anyFunctionCall: functionCall | classFunctionCall | elemFunctionCall;

assigment: IDENTIFIER '=' (expression | newStatement);

newStatement: NEW type ((L_SQ_PAREN expression R_SQ_PAREN)* | '(' functionParamList ')');

localVariable: FINAL? type (L_SQ_PAREN R_SQ_PAREN)* IDENTIFIER;

condition: expression;

ifStatement: IF '(' condition ')' '{' functionBody '}' (ELSE '{' functionBody '}')?;

whileStatement: WHILE '(' condition ')' '{' functionBody '}';

forStatement : FOR '(' assigment ';' condition ';' assigment ')' '{' functionBody '}';

statement: (localVariable | assigment | anyFunctionCall | BREAK | CONTINUE) ';';

composedStatement: forStatement | ifStatement | whileStatement | statement | (RETURN expression ';');

functionBody: composedStatement*;

constructor: IDENTIFIER '(' argumentList ')' '{' constructorBody '}';

constructorBody: (SUPER '(' functionParamList ')' ';')? functionBody;

expression
    : expression BINARY_OP expression
    | NOT expression
    | '(' expression ')'
    | IDENTIFIER
    | LITERAL
    | anyFunctionCall
    ;