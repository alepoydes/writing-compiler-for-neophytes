grammar Rust;
@header {
    package rustless.ast;

    import rustless.*;
    import rustless.command.*;

    import java.util.Map;
    import java.util.Arrays;
}

repl returns [Command cmd]
    : 'quit' EOF { System.exit(0); }
    | instruction EOF { $cmd=$instruction.cmd; }
    ; 
module returns [List<Command> cmds]
    : { $cmds=new ArrayList(); } (instruction { $cmds.add($instruction.cmd); })* EOF 
    ;
function: 'fn' ID '(' argumentDeclarationList ')' block (';'|) ; 
block returns [Command cmd] locals[List<Command> cmds]
    : '{' { $cmds=new ArrayList(); } (instruction ';'? { $cmds.add($instruction.cmd); } )* '}' { 
        $cmd=new Block($cmds); }
    ;
instruction returns [Command cmd]
    : block { $cmd=$block.cmd; }
    | declaration { $cmd=$declaration.cmd; } 
    | ID '=' expr { $cmd=new Assign($ID.text, $expr.cmd); } 
    | functionCall { $cmd=$functionCall.cmd; }
    | macroCall { $cmd=$macroCall.cmd; }
    | expr { $cmd=$expr.cmd; }
    ;
declaration returns [Command cmd] locals [boolean mut, Command value]
    : 'let' ('mut' {$mut=true;})? ID ( '=' a=expr { $value=$a.cmd; })?
        { $cmd=new Declare($ID.text,$mut,null,$value); }
    ;
functionCall returns [Command cmd]
    : ID '(' argumentList ')' { $cmd=new Call($ID.text,$argumentList.arg); }
    ;
macroCall returns [Command cmd]
    : ID '!' '(' argumentList ')'  { throw new ParseCancellationException("Macros are not implemented"); }
    ;
argumentList returns [List<Command> arg]
    : { $arg=new ArrayList(); } a=argument { $arg.add($a.cmd); } ( ',' b=argument { $arg.add($b.cmd); } )*
    ;
argument returns [Command cmd]: expr { $cmd=$expr.cmd; } ;
argumentDeclarationList
    : 
    | arg+=argumentDeclaration ( ',' arg+=argumentDeclaration )* 
    ;
argumentDeclaration: ID ;
expr returns [Command cmd]
    : a=expr4 op='||' b=expr { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr4 { $cmd=$a.cmd; }
    ;
expr4 returns [Command cmd]
    : a=expr5 op='&&' b=expr4 { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr5 { $cmd=$a.cmd; }
    ;        
expr5 returns [Command cmd]
    : a=expr6 op=('<'|'>'|'>='|'<='|'=='|'!=') b=expr5 { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr6 { $cmd=$a.cmd; }
    ;    
expr6 returns [Command cmd]
    : a=expr7 op='|' b=expr6 { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr7 { $cmd=$a.cmd; }
    ;                
expr7 returns [Command cmd]
    : a=expr8 op='^' b=expr7 { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr8 { $cmd=$a.cmd; }
    ;            
expr8 returns [Command cmd]
    : a=expr9 op='&' b=expr8 { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr9 { $cmd=$a.cmd; }
    ;        
expr9 returns [Command cmd]
    : a=expr10 op=('<<'|'>>') b=expr9 { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr10 { $cmd=$a.cmd; }
    ;    
expr10 returns [Command cmd]
    : a=expr20 { $cmd=$a.cmd; }
        (
            op=('+'|'-') b=expr20 { $cmd=new Call($op.text,$cmd,$b.cmd); }
        )*
    ;
expr20 returns [Command cmd]
    : a=expr30 { $cmd=$a.cmd; } 
        (
            op=('*'|'/'|'%') b=expr30 { $cmd=new Call($op.text,$cmd,$b.cmd); }
        )*
    ;
expr30 returns [Command cmd]
    : functionCall { $cmd=$functionCall.cmd; }
    | op=('-'|'!') a=expr30 { $cmd=new Call($op.text,$a.cmd); }
    | b=expr40 { $cmd=$b.cmd; }
    ;
expr40 returns [Command cmd]
    : ID { $cmd=new Access($ID.text); } 
    | literal { $cmd=$literal.cmd; }
    | '(' expr ')' { $cmd=$expr.cmd; }
    ;
literal returns [Command cmd]
    : FLOAT { $cmd=new Literal(new Value(Double.parseDouble($text))); }
    | INT { $cmd=new Literal(new Value(Integer.parseInt($text))); }
    | STRING { $cmd=new Literal(new Value($text)); } 
    | TRUE { $cmd=new Literal(new Value(true)); } 
    | FALSE { $cmd=new Literal(new Value(false)); } 
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