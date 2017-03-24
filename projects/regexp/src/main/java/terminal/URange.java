package wcn.terminal;

import java.util.Collection;
import java.util.ArrayList;

public class URange {
     /** Реализация ICharSet */
    public URange intersect(URange other) {
        URange a, b;
        if(this.min<other.min) { a=this; b=other; }
        else { b=this; a=other; };
        if(a.max<b.min) return null;
        if(a.max<b.max) return new URange(b.min, a.max);
        return new URange(b.min, b.max);
    }
    public Collection<URange> subtract(URange other) {
        ArrayList<URange> result=new ArrayList();
        if(this.min<other.min) 
            if(this.max<other.min) result.add(new URange(this.min, this.max));
            else result.add(new URange(this.min, (char)(other.min-1)));
        if(this.max>other.max) 
            if(this.min>other.max) result.add(new URange(this.min, this.max));
            else result.add(new URange((char)(other.max+1), this.max));
        return result;
    }
    /** Конструктор */
    public URange(char min, char max) {
        if(min<max) { this.min=min; this.max=max; }
        else { this.min=max; this.max=min; };
    }
    /** Реализация */
    private char min, max; // Диапазон значений [min,max)
    /** методы Object */
    @Override public boolean equals(Object obj) {
        if(obj==null) return false;
        if(this==obj) return true;
        if(this.getClass()!=obj.getClass()) return false;
        return this.min==((URange)obj).min && this.max==((URange)obj).max;
    }
    @Override public int hashCode() { return this.min+this.max; }
    @Override public String toString() { 
        return String.format("%c-%c", this.min, this.max);
    };
}