package wcn.parser;

public class Nonterminal {
    /** 
     * Создает нетерминал с данным именем. 
     * Имя передается без угловых скобок.
     */
    Nonterminal(String name) { this.name=name; }
    /** Возвращает название состояния */
    public String getName() { return this.name; }
    /** хранилище для номера */
    private String name;
    @Override public String toString() { return String.format("◂%s▸",this.name); };
};