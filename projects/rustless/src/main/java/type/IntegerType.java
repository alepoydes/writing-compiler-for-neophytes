package rustless.type;

// Parent class for all types
public class IntegerType extends Type {
    public IntegerType() {};
    @Override public Object defaultValue() { return new Integer(0); }
    /** Methods of Object */
    @Override public String toString() { 
        return "int";
    }
}