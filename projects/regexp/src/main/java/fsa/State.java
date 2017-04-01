package wcn.fsa;

/**
 * Состояние конечного автомата.
 * Во внутреннем представлении это просто число, 
 * однако мы используем обращения к состояниям через State,
 * чтобы уменьшить шансы обращения к несущестующему объекту.
 */
public class State {
     /** Создает состояние с данным номером */
     State(int id) { this.id=id; };
     /** Возвращает номер состояния */
     public int getId() { return this.id; }
     /** хранилище для номера */
     private int id;
     @Override public int hashCode() { return this.id; }
     @Override public String toString() { return String.valueOf(this.id); };
     @Override public boolean equals(Object obj) { 
        if(obj==null) return false;
        if(this.getClass()!=obj.getClass()) return false;
        State state=(State)obj;
        return this.id==state.id;
     };
}