package rustless;

import rustless.type.*;

import java.lang.StringBuilder;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public class Value {
    public Value() {
        this.type=new VoidType();
    }
    public Value(Double value) {
        this.data=value;
        this.type=new DoubleType();
    }
    public Value(Integer value) {
        this.data=value;
        this.type=new IntegerType();
    }
    public Value(String value) {
        this.data=value;
        this.type=new StringType();
    }
    public Value(Boolean value) {
        this.data=value;
        this.type=new BooleanType();
    }
    public Value(Type type, Object value) {
        this.data=value;
        this.type=type;
    }
    public Value(Type type) {
        this.data=type.defaultValue();
        this.type=type;
    }
    public Value(List<Value> values) {
        ArrayList<Type> types=new ArrayList(values.size());
        ArrayList<Object> data=new ArrayList(values.size());
        for(Value value: values) {
            types.add(value.type);
            data.add(value.data);
        };
        this.type=new TupleType(types);
        this.data=data; this.type=type;
    }
    public Value(Function<List<Object>,Object> fn, Type result, TupleType argument) {
        this.data=fn;
        this.type=new LambdaType(argument,result);
    }
    public Type type;
    public Object data;
    
    /** Methods of Object */
    @Override public String toString() {
        StringBuilder builder=new StringBuilder();
        if(this.data==null) builder.append("null"); else builder.append(this.data.toString());
        builder.append(" : ");
        if(this.type==null) builder.append("?"); else builder.append(this.type.toString());
        return builder.toString();
    }
    @Override public boolean equals(Object obj) {
		if(this==obj) return true;
		if(obj==null) return false;
		if(this.getClass()!=obj.getClass()) return false;
        Value other=(Value)obj;
        if(!(this.type==null && other.type==null || this.type!=null && this.type.equals(other.type))) 
            return false; 
        if(!(this.data==null && other.data==null || this.data!=null && this.data.equals(other.data))) 
            return false; 
        return true;
	}
    @Override public int hashCode() {
		int code=this.getClass().hashCode();
        if(this.type!=null) code+=this.type.hashCode();
        if(this.data!=null) code+=this.data.hashCode();
        return code;
	}
}