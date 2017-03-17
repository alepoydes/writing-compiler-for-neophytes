package wcn.terminal;

import java.util.stream.Collectors;
import java.util.List;

/**
 * Обертка на Character, реализующая дополнительные интерфейсы
 */
public class UChar implements ICharSet<UChar> {
    /** Значение */
    private char value;
    /** Конструкторы */
    public UChar(char c) { this.value=c; }
    public UChar(Character c) { this.value=c.charValue(); }
    /** Возвращает примитивное значение */
    public char toChar() { return this.value; }
    /** Реализация ICharSet */
    public UChar intersection(UChar other) {
        return (this.equals(other))?this:null;
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