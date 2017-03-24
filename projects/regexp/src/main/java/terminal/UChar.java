package wcn.terminal;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Обертка на Character, реализующая дополнительные интерфейсы
 */
public class UChar implements ICharSet<UChar, UChar> {
    /** Значение */
    private char value;
    /** Конструкторы */
    public UChar(char c) { this.value=c; }
    public UChar(Character c) { this.value=c.charValue(); }
    /** Возвращает примитивное значение */
    public char toChar() { return this.value; }
    /** Реализация ICharSet */
    public UChar intersect(UChar other) {
        return (this.equals(other))?this:null;
    }
    public Collection<UChar> subtract(UChar other) {
        ArrayList<UChar> result=new ArrayList();
        if(this.value!=other.value) result.add(new UChar(this.value));
        return result;
    }
    /** Конвертирует строку в последовательность символов */
    public static List<UChar> asList(String str) {
        return str.chars().mapToObj(i -> new UChar((char)i)).collect(Collectors.toList());
    };
    /** методы Object */
    @Override public boolean equals(Object obj) {
        if(obj==null) return false;
        if(this==obj) return true;
        if(this.getClass()!=obj.getClass()) return false;
        return this.value==((UChar)obj).value;
    }
    @Override public int hashCode() { return this.value; }
    @Override public String toString() { 
        return String.format("%c", this.value);
    };
}