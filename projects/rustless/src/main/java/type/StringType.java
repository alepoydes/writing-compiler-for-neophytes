package rustless.type;

// Parent class for all types
public class StringType extends Type {
    public StringType() {};
    @Override public Object defaultValue() { return ""; }
    /** Methods of Object */
    @Override public String toString() { 
        return "string";
    }
}