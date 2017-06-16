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
    | statement EOF { $cmd=$statement.cmd; }
    ; 
module returns [List<Command> cmds]
    : { $cmds=new ArrayList(); } (statement ';'? { $cmds.add($statement.cmd); })* EOF 
    ;
function: 'fn' ID '(' argumentDeclarationList ')' block (';'|) ; 
block returns [Command cmd] locals[List<Command> cmds]
    : '{' { $cmds=new ArrayList(); } (statement ';'? { $cmds.add($statement.cmd); } )* '}' { 
        $cmd=new Block($cmds); }
    ;
statement returns [Command cmd]
    : declaration { $cmd=$declaration.cmd; } 
    | ID '=' expr { $cmd=new Assign($ID.text, $expr.cmd); } 
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
condition returns [Command cmd] locals [Command negative]
    : 'if' c=expr p=block ('else' n=block { $negative=$n.cmd; })? { $cmd=new Condition($c.cmd,$p.cmd,$negative); }
    ;
expr returns [Command cmd]
    : ID { $cmd=new Access($ID.text); } 
    | literal { $cmd=$literal.cmd; }
    | '(' expr ')' { $cmd=$expr.cmd; }
    | functionCall { $cmd=$functionCall.cmd; }
    | macroCall { $cmd=$macroCall.cmd; }
    | block { $cmd=$block.cmd; }
    | condition  { $cmd=$condition.cmd; }
    | op=('-'|'!') a=expr { $cmd=new Call($op.text,$a.cmd); }
    | a=expr op=('*'|'/'|'%') b=expr { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr op=('+'|'-') b=expr { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr op=('<<'|'>>') b=expr { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr op='&' b=expr { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr op='^' b=expr { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr op='|' b=expr { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr op=('<'|'>'|'>='|'<='|'=='|'!=') b=expr { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr op='&&' b=expr { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
    | a=expr op='||' b=expr { $cmd=new Call($op.text,$a.cmd,$b.cmd); }
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
ErrorTocken : . ;