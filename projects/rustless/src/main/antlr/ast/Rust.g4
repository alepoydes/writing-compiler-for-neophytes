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
    ;
functionCall: ID '(' argumentList ')';
argumentList
    : 
    | arg+=argument ( ',' arg+=argument )*
    ;
argument: ID ;
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
    ;
literal
    : INT
    ;

PREFIXOPERATOR : [-] ;
BINARYOPERATOR : [+*/-] ;
ID : [a-zA-Z][a-zA-Z0-9]* ;
NEWLINE : [\r\n]+ ;
INT     : [0-9]+ ;
