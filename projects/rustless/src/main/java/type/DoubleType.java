package rustless.type;

// Parent class for all types
public class DoubleType extends Type {
    public DoubleType() {};
    @Override public Object defaultValue() { return new Double(0.0); }
    /** Methods of Object */
    @Override public String toString() { 
        return "double";
    }
}