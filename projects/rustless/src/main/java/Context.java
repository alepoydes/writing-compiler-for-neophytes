package rustless;

import rustless.type.*;

import org.antlr.v4.runtime.misc.*;
import java.util.function.Function;
import java.util.List;
import java.util.Arrays;

public class Context {
    public Context() {
        this.variables=new IncHashMap();
        this.prelude();
    }
    public Context(Context parent) {
        this.variables=new IncHashMap(parent.variables);
    }
    public IncHashMap<String,Value> variables;

    public void prelude() {
        this.declare("+",x -> (Double)x.get(0)+(Double)x.get(1),new DoubleType(),new DoubleType(),new DoubleType());
        this.declare("-",x -> (Double)x.get(0)-(Double)x.get(1),new DoubleType(),new DoubleType(),new DoubleType());
        this.declare("*",x -> (Double)x.get(0)*(Double)x.get(1),new DoubleType(),new DoubleType(),new DoubleType());
        this.declare("/",x -> (Double)x.get(0)/(Double)x.get(1),new DoubleType(),new DoubleType(),new DoubleType());
        this.declare("-",x -> -(Double)x.get(0),new DoubleType(),new DoubleType());

        this.declare("+",x -> (Integer)x.get(0)+(Integer)x.get(1),new IntegerType(),new IntegerType(),new IntegerType());
        this.declare("-",x -> (Integer)x.get(0)-(Integer)x.get(1),new IntegerType(),new IntegerType(),new IntegerType());
        this.declare("*",x -> (Integer)x.get(0)*(Integer)x.get(1),new IntegerType(),new IntegerType(),new IntegerType());
        this.declare("/",x -> (Integer)x.get(0)/(Integer)x.get(1),new IntegerType(),new IntegerType(),new IntegerType());
        this.declare("-",x -> -(Integer)x.get(0),new IntegerType(),new IntegerType());

        this.declare("^",x -> (Integer)x.get(0)^(Integer)x.get(1),new IntegerType(),new IntegerType(),new IntegerType());
        this.declare("&",x -> (Integer)x.get(0)&(Integer)x.get(1),new IntegerType(),new IntegerType(),new IntegerType());
        this.declare("|",x -> (Integer)x.get(0)|(Integer)x.get(1),new IntegerType(),new IntegerType(),new IntegerType());
        this.declare("!",x -> ~(Integer)x.get(0),new IntegerType(),new IntegerType());

        this.declare("<",x -> (Double)x.get(0)<(Double)x.get(1),new BooleanType(),new DoubleType(),new DoubleType());
        this.declare(">",x -> (Double)x.get(0)>(Double)x.get(1),new BooleanType(),new DoubleType(),new DoubleType());
        this.declare("<=",x -> (Double)x.get(0)<=(Double)x.get(1),new BooleanType(),new DoubleType(),new DoubleType());        
        this.declare(">=",x -> (Double)x.get(0)>=(Double)x.get(1),new BooleanType(),new DoubleType(),new DoubleType());
        this.declare("==",x -> (Double)x.get(0)==(Double)x.get(1),new BooleanType(),new DoubleType(),new DoubleType());
        this.declare("!=",x -> (Double)x.get(0)!=(Double)x.get(1),new BooleanType(),new DoubleType(),new DoubleType());

        this.declare("<",x -> (Integer)x.get(0)<(Integer)x.get(1),new BooleanType(),new IntegerType(),new IntegerType());
        this.declare(">",x -> (Integer)x.get(0)>(Integer)x.get(1),new BooleanType(),new IntegerType(),new IntegerType());
        this.declare("<=",x -> (Integer)x.get(0)<=(Integer)x.get(1),new BooleanType(),new IntegerType(),new IntegerType());
        this.declare(">=",x -> (Integer)x.get(0)>=(Integer)x.get(1),new BooleanType(),new IntegerType(),new IntegerType());
        this.declare("==",x -> (Integer)x.get(0)==(Integer)x.get(1),new BooleanType(),new IntegerType(),new IntegerType());
        this.declare("!=",x -> (Integer)x.get(0)!=(Integer)x.get(1),new BooleanType(),new IntegerType(),new IntegerType());

        this.declare("!",x -> !(Boolean)x.get(0),new BooleanType(),new BooleanType());
        this.declare("&",x -> (Boolean)x.get(0)&(Boolean)x.get(1),new BooleanType(),new BooleanType(),new BooleanType());
        this.declare("|",x -> (Boolean)x.get(0)|(Boolean)x.get(1),new BooleanType(),new BooleanType(),new BooleanType());
        this.declare("&&",x -> (Boolean)x.get(0)&(Boolean)x.get(1),new BooleanType(),new BooleanType(),new BooleanType());
        this.declare("||",x -> (Boolean)x.get(0)|(Boolean)x.get(1),new BooleanType(),new BooleanType(),new BooleanType());

        this.declare("sin",x -> Math.sin((Double)x.get(0)),new DoubleType(),new DoubleType());
        this.declare("cos",x -> Math.cos((Double)x.get(0)),new DoubleType(),new DoubleType());
        this.declare("exp",x -> Math.exp((Double)x.get(0)),new DoubleType(),new DoubleType());
        this.declare("log",x -> Math.log((Double)x.get(0)),new DoubleType(),new DoubleType());
        this.declare("abs",x -> Math.abs((Double)x.get(0)),new DoubleType(),new DoubleType());
        this.declare("pow",x -> Math.pow((Double)x.get(0),(Double)x.get(1)),new DoubleType(),new DoubleType(),new DoubleType());
        this.declare("random",x -> Math.random(),new DoubleType());

        this.variables.put("PI",new Value(Math.PI));
        this.variables.put("E",new Value(Math.E));
    }

    public void declare(String name,Function<List<Object>,Object> fn, Type result, Type... arguments) {
        TupleType argument=new TupleType(Arrays.asList(arguments));
        String fullname=String.format("%s%s",name,argument.toString());
        this.variables.put(fullname, new Value(fn,result,argument));
    }

    public Value call(String name, Value... arguments) throws ParseCancellationException {
        return this.call(name, Arrays.asList(arguments));
    }
    public Value call(String name, List<Value> arguments) throws ParseCancellationException {
        Value argument=new Value(arguments);
        String fullname=String.format("%s%s",name,argument.type.toString());
        Value var=this.variables.get(fullname);
        if(var==null) throw new ParseCancellationException(String.format("Function '%s' is not defined", fullname));
        if(var.type==null || !var.type.isCallable()) 
            throw new ParseCancellationException(String.format("Internal error: '%s' is not a function", fullname));
        LambdaType fntype=(LambdaType)var.type;
        if(!fntype.argument.equals(argument.type))
            throw new ParseCancellationException(String.format(
                "Function '%s' has unexpected signature '%s'", 
                fullname, fntype.argument.toString()));
        Function<List<Object>,Object> fn=(Function<List<Object>,Object>)var.data;
        Object result=fn.apply((List<Object>)argument.data);
        return new Value(fntype.result, result);
    }
}