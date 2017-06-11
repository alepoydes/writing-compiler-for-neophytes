grammar Rust;
@header {
    package rustless.ast;

    import rustless.*;

    import java.util.Map;
    import java.util.Arrays;
}

repl[Context ctx]
    : 'quit' EOF { System.exit(0); }
    | expr[ctx] EOF { System.out.format("\n  %s\n\n", $expr.value.toString()); } 
    | instruction[ctx]
    ; 
module[Context ctx]: (function[ctx])* EOF ;
function[Context ctx]: 'fn' ID '(' argumentDeclarationList ')' block[ctx] (';'|) ; 
block[Context ctx]: '{' (instruction[ctx] ';')* '}' ;
instruction[Context ctx]
    : declaration[ctx]
    | ID '=' expr[ctx] { 
            if(!$ctx.variables.containsKey($ID.text)) 
                throw new ParseCancellationException(String.format("Variable '%s' is not defined",$ID.text));
            $ctx.variables.put($ID.text,$expr.value); 
        } 
    | functionCall[ctx]
    | macroCall[ctx]
    ;
declaration[Context ctx]
    : 'let' 'mut'? ID { $ctx.variables.put($ID.text,new Value()); }
    | 'let' 'mut'? ID '=' expr[ctx] { $ctx.variables.put($ID.text,$expr.value); }
    ;
functionCall[Context ctx] returns [Value value]
    : ID '(' argumentList[ctx] ')' { $value=ctx.call($ID.text,$argumentList.arg); }
    ;
macroCall[Context ctx]
    : ID '!' '(' argumentList[ctx] ')'  { throw new ParseCancellationException("Macros are not implemented"); }
    ;
argumentList[Context ctx] returns [List<Value> arg]
    : 
    | a=argument[ctx] { $arg=new ArrayList(); $arg.add($a.value); } ( ',' b=argument[ctx] { $arg.add($b.value); } )*
    ;
argument[Context ctx] returns [Value value]: expr[ctx] { $value=$expr.value; } ;
argumentDeclarationList
    : 
    | arg+=argumentDeclaration ( ',' arg+=argumentDeclaration )* 
    ;
argumentDeclaration: ID ;
expr[Context ctx] returns [Value value]
    : a=expr4[ctx] op='||' b=expr[ctx] { $value=ctx.call($op.text,$a.value,$b.value); }
    | a=expr4[ctx] { $value=$a.value; }
    ;
expr4[Context ctx] returns [Value value]
    : a=expr5[ctx] op='&&' b=expr4[ctx] { $value=ctx.call($op.text,$a.value,$b.value); }
    | a=expr5[ctx] { $value=$a.value; }
    ;        
expr5[Context ctx] returns [Value value]
    : a=expr6[ctx] op=('<'|'>'|'>='|'<='|'=='|'!=') b=expr5[ctx] { $value=ctx.call($op.text,$a.value,$b.value); }
    | a=expr6[ctx] { $value=$a.value; }
    ;    
expr6[Context ctx] returns [Value value]
    : a=expr7[ctx] op='|' b=expr6[ctx] { $value=ctx.call($op.text,$a.value,$b.value); }
    | a=expr7[ctx] { $value=$a.value; }
    ;                
expr7[Context ctx] returns [Value value]
    : a=expr8[ctx] op='^' b=expr7[ctx] { $value=ctx.call($op.text,$a.value,$b.value); }
    | a=expr8[ctx] { $value=$a.value; }
    ;            
expr8[Context ctx] returns [Value value]
    : a=expr9[ctx] op='&' b=expr8[ctx] { $value=ctx.call($op.text,$a.value,$b.value); }
    | a=expr9[ctx] { $value=$a.value; }
    ;        
expr9[Context ctx] returns [Value value]
    : a=expr10[ctx] op=('<<'|'>>') b=expr9[ctx] { $value=ctx.call($op.text,$a.value,$b.value); }
    | a=expr10[ctx] { $value=$a.value; }
    ;    
expr10[Context ctx] returns [Value value]
    : a=expr20[ctx] { $value=$a.value; }
        (
            op=('+'|'-') b=expr20[ctx] { $value=ctx.call($op.text,$value,$b.value); }
        )*
    ;
expr20[Context ctx] returns [Value value]
    : a=expr30[ctx] { $value=$a.value; } 
        (
            op=('*'|'/'|'%') b=expr30[ctx] { $value=ctx.call($op.text,$value,$b.value); }
        )*
    ;
expr30[Context ctx] returns [Value value]
    : functionCall[ctx] { $value=$functionCall.value; }
    | op=('-'|'!') a=expr30[ctx] { $value=ctx.call($op.text,$a.value); }
    | b=expr40[ctx] { $value=$b.value; }
    ;
expr40[Context ctx] returns [Value value]
    : ID { 
            $value=$ctx.variables.get($ID.text); 
            if($value==null) 
                throw new ParseCancellationException(String.format("Variable '%s' is not defined",$ID.text));
         } 
    | literal { $value=$literal.value; }
    | '(' expr[ctx] ')' { $value=$expr.value; }
    ;
literal returns [Value value]
    : FLOAT { $value=new Value(Double.parseDouble($text)); }
    | INT { $value=new Value(Integer.parseInt($text)); }
    | STRING { $value=new Value($text); } 
    | TRUE { $value=new Value(true); } 
    | FALSE { $value=new Value(false); } 
    ;

TRUE: 'true' ;
FALSE: 'false' ;
ID : [a-zA-Z][a-zA-Z0-9]* ;
fragment NEWLINE : [\r]?[\n] ;
INT : [0-9]+ ;
FLOAT : [0-9]+([.][0-9]+)?([Ee][-]?[0-9]+)? ;
STRING : '"' ( ESC | ~[\\"] )* '"';
fragment ESC : '\\"' | '\\\\' ;

WS : [ \n\r\t]+ -> channel(HIDDEN); 