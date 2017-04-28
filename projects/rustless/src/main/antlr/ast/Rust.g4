grammar Rust;
@header {
    package rustless.ast;
}

module:	(function)* EOF ;
function: 'fn' ID '(' argumentDeclarationList ')' block (';'|) ; 
block: '{' (instruction ';')* '}' ;
instruction
    : ID '=' expr 
    | functionCall
    | macroCall
    ;
functionCall: ID '(' argumentList ')';
macroCall: ID '!' '(' argumentList ')';
argumentList
    : 
    | arg+=argument ( ',' arg+=argument )*
    ;
argument: expr ;
argumentDeclarationList
    : 
    | arg+=argumentDeclaration ( ',' arg+=argumentDeclaration )* 
    ;
argumentDeclaration: ID ;    
expr
    : functionCall 
    | PREFIXOPERATOR
    | expr BINARYOPERATOR expr
    | '(' expr ')'
    | literal
    | ID
    ;
literal
    : INT
    | STRING
    ;

PREFIXOPERATOR : [-] ;
BINARYOPERATOR : [+*/-] ;
ID : [a-zA-Z][a-zA-Z0-9]* ;
fragment NEWLINE : [\r]?[\n] ;
INT     : [0-9]+ ;
STRING: '"' ( ESC | ~[\\"] )* '"';
fragment ESC : '\\"' | '\\\\' ;

WS : [ \n\r\t]+ -> channel(HIDDEN); 