package rustless.type;

// Parent class for all types
public class BooleanType extends Type {
    public BooleanType() {};
    @Override public Object defaultValue() { return new Boolean(false); }
    /** Methods of Object */
    @Override public String toString() { 
        return "bool";
    }
}