grammar Rust;
@header {
    package rustless.ast;
}

repl returns [double value]
    : 'quit' EOF { System.exit(0); }
    | expr EOF { $value=$expr.value; } ; 
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
expr returns [double value]
    : functionCall { $value=Double.NaN; } 
    | MINUS expr { $value=-$expr.value; }
    | literal { $value=$literal.value; }
    | ID { $value=Double.NaN; } 
    | a=expr MULT b=expr { $value=$a.value*$b.value; }    
    | a=expr DIV b=expr { $value=$a.value/$b.value; }            
    | a=expr PLUS b=expr { $value=$a.value+$b.value; }
    | a=expr MINUS b=expr { $value=$a.value-$b.value; }
    | '(' expr ')' { $value=$expr.value; }
    ;
literal returns [double value]
    : FLOAT { $value=Double.parseDouble($text); }
    | INT { $value=Double.parseDouble($text); }
    | STRING { $value=Double.NaN; } 
    ;

MINUS : '-' ;
PLUS : '+' ;
DIV : '/' ;
MULT : '*' ;
ID : [a-zA-Z][a-zA-Z0-9]* ;
fragment NEWLINE : [\r]?[\n] ;
INT : [0-9]+ ;
FLOAT : [0-9]+([.][0-9]+)?([Ee][-]?[0-9]+)? ;
STRING : '"' ( ESC | ~[\\"] )* '"';
fragment ESC : '\\"' | '\\\\' ;

WS : [ \n\r\t]+ -> channel(HIDDEN); 