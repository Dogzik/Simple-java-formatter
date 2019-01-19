grammar Java;

WHITESPACE:     [ \t\r\n\u000C]+ -> skip;

PUBLIC:         'public';
PRIVATE:        'private';
PROTECTED:      'protected';

ACCESS_MOD: PUBLIC | PRIVATE | PROTECTED;

CLASS:          'class';
INTERFACE:      'interface';

IMPLEMENTS:     'implements';
EXTENDS:        'extends';

INT:            'int';
BYTE:           'byte';
LONG:           'long';
FLOAT:          'float';
DOUBLE:         'double';
BOOLEAN:        'boolean';

fragment Letter: [a-zA-Z];

IDENTIFIER:     Letter (Letter | [0-9$_])*;

PRIMITIVE_TYPE: INT | BYTE | LONG | FLOAT | DOUBLE | BOOLEAN;

type
    : IDENTIFIER
    | PRIMITIVE_TYPE
    ;

typeList : type (',' type)*;

classDeclaration: PUBLIC? IDENTIFIER (EXTENDS type)? (IMPLEMENTS typeList)? '{' classBody '}';

classBody: field* function*;

field: ACCESS_MOD? type IDENTIFIER ';';

function: ACCESS_MOD? type IDENTIFIER '(' argument*')' '{' /*functionBody*/ '}';

argument: type IDENTIFIER;







