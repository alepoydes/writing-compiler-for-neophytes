package rustless.type;

// Parent class for all types
public class Type {
	public boolean isCallable() { return false; }
	public Object defaultValue() { return null; }
    /** Methods of Object */
   	@Override public boolean equals(Object obj) {
		if (this==obj)return true;
		if (obj==null) return false;
		return (this.getClass()==obj.getClass());
	}
    @Override public int hashCode() {
		return this.getClass().hashCode();
	}
}