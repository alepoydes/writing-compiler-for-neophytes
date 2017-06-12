package rustless.type;

import java.lang.StringBuilder;

// Parent class for all types
public class LambdaType extends Type {
    public LambdaType(Type argument, Type result) {
        this.argument=argument;
        this.result=result;
    };
    public final Type argument;
    public final Type result;
    @Override public boolean isCallable() { return true; }
    /** Methods of Object */
    @Override public String toString() {
        return String.format("%s -> %s", this.argument.toString(), this.result.toString());
    }
    @Override public boolean equals(Object obj) {
		if(this==obj)return true;
		if(obj==null) return false;
		if(this.getClass()!=obj.getClass()) return false;
        LambdaType other=(LambdaType)obj;
        return this.argument.equals(other.argument) && this.result.equals(other.result);
	}
    @Override public int hashCode() {
        int code=this.getClass().hashCode();
        code+=this.argument.hashCode();
        code+=this.result.hashCode();
        return code;
	}
}