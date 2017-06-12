package rustless.type;

import java.lang.StringBuilder;
import java.util.List;

// Parent class for all types
public class TupleType extends Type {
    public TupleType(List<Type> content) {
        this.content=content;
    };
    protected List<Type> content;
    /** Methods of Object */
    @Override public String toString() { 
        StringBuilder builder=new StringBuilder();
        builder.append("(");
        boolean first=true;
        for(Type type: this.content) {
            if(first) first=false; else builder.append(",");
            builder.append(type.toString());
        };
        builder.append(")");
        return builder.toString();
    }
    @Override public boolean equals(Object obj) {
		if(this==obj) return true;
		if(obj==null) return false;
		if(this.getClass()!=obj.getClass()) return false;
        TupleType other=(TupleType)obj;
        return this.content.equals(other.content);
	}
    @Override public int hashCode() {
        int code=this.getClass().hashCode();
        for(Type type: this.content) code+=type.hashCode();
        return code;
	}
}