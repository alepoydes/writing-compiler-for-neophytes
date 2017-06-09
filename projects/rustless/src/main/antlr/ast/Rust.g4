grammar Rust;
@header {
    package rustless.ast;

    import java.util.Map;
}

repl[Map<String, Double> variables]
    : 'quit' EOF { System.exit(0); }
    | expr EOF { System.out.format("\n  %s\n\n", $expr.value); } 
    | instruction
    ; 
module:	(function)* EOF ;
function: 'fn' ID '(' argumentDeclarationList ')' block (';'|) ; 
block: '{' (instruction ';')* '}' ;
instruction
    : declaration
    | ID '=' expr { 
            if(!$repl::variables.containsKey($ID.text)) 
                throw new ParseCancellationException(String.format("Variable '%s' is not defined",$ID.text));
            $repl::variables.put($ID.text,$expr.value); 
        } 
    | functionCall
    | macroCall
    ;
declaration
    : 'let' 'mut'? ID { $repl::variables.put($ID.text,Double.NaN); }
    | 'let' 'mut'? ID '=' expr { $repl::variables.put($ID.text,$expr.value); }
    ;
functionCall returns [Double value]
    : ID '(' argumentList ')' { throw new ParseCancellationException("Functions are not implemented"); }
    ;
macroCall
    : ID '!' '(' argumentList ')'  { throw new ParseCancellationException("Macros are not implemented"); }
    ;
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
expr returns [Double value]
    : functionCall { $value=$functionCall.value; }
    | MINUS expr { $value=-$expr.value; }
    | literal { $value=$literal.value; }
    | ID { 
            $value=$repl::variables.get($ID.text); 
            if($value==null) 
                throw new ParseCancellationException(String.format("Variable '%s' is not defined",$ID.text));
         } 
    | a=expr MULT b=expr { $value=$a.value*$b.value; }    
    | a=expr DIV b=expr { $value=$a.value/$b.value; }            
    | a=expr PLUS b=expr { $value=$a.value+$b.value; }
    | a=expr MINUS b=expr { $value=$a.value-$b.value; }
    | '(' expr ')' { $value=$expr.value; }
    ;
literal returns [Double value]
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