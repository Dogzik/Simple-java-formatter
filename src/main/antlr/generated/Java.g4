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

fragment INT:            'int';
fragment BYTE:           'byte';
fragment LONG:           'long';
fragment FLOAT:          'float';
fragment DOUBLE:         'double';
fragment BOOLEAN:        'boolean';
PRIMITIVE_TYPE: INT | BYTE | LONG | FLOAT | DOUBLE | BOOLEAN;

fragment Letter: [a-zA-Z];
IDENTIFIER:     Letter (Letter | [0-9$_])*;

LITERAL:        ([0-9]+) | '"'.*?'"';

type: IDENTIFIER | PRIMITIVE_TYPE;

typeList : type (',' type)*;

classDeclaration: ACCESS_MOD? FINAL? CLASS IDENTIFIER (EXTENDS type)? (IMPLEMENTS typeList)? '{' classBody '}';

classBody: (field | constructor | function)*;

field: ACCESS_MOD? FINAL? type IDENTIFIER ';';

function: ACCESS_MOD? FINAL? type IDENTIFIER '(' argumentList ')' '{' functionBody '}';

argument: FINAL? type IDENTIFIER;

argumentList: (argument (',' argument)*)?;

functionParam: IDENTIFIER | LITERAL | functionCall | classFunctionCall;

functionParamList: (functionParam (',' functionParam)*)?;

functionCall: IDENTIFIER '(' functionParamList ')';

classFunctionCall: IDENTIFIER '.' functionCall ('.' functionCall)*;

anyFunctionCall: functionCall | classFunctionCall;

assigment: IDENTIFIER '=' (LITERAL | IDENTIFIER | anyFunctionCall);

localVariable: FINAL? type IDENTIFIER;

condition: anyFunctionCall | IDENTIFIER;

ifStatement: IF '(' condition ')' '{' functionBody '}' (ELSE '{' functionBody '}')?;

whileStatement: WHILE '(' condition ')' '{' functionBody '}';

statement: (localVariable | assigment | anyFunctionCall) ';';

composedStatement: ifStatement | whileStatement | statement;

functionBody: composedStatement*;

constructor: IDENTIFIER '(' argumentList ')' '{' constructorBody '}';

constructorBody: (SUPER '(' functionParamList ')' ';')? functionBody;



