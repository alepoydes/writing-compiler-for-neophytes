package wcn.lexer;

import wcn.fsa.*;

import java.util.Optional;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collection;

public class Lexer<T,F,P> {
    /**
     * Создает лексер, набор лексем для которого задается набором конечных 
     * автоматов из перечня lexems.
     * Лексемы идентифицируются маркерами остановочных состояний.
     * Обычно автоматы должны иметь разные маркеры остановочных состояний.
     */
    public Lexer(Iterable<FSA<T,F,P>> lexemes) {
        IPredicateMapFactory<P,T,State> factory=new KeyPredicateMapFactory();
        Combinators<T,F,P> combinators=new Combinators(factory);
        this.automaton=combinators.union(lexemes);
        this.reset();
    }
    public void reset() {
        this.startNewToken();
    };
    public void startNewToken() {
        this.automaton.reset();
        this.terminals=new LinkedList();
    };
    /**
     * Главная функция для лексического анализа.
     * Принимает терминал. 
     * Возвращает LexerResult, если выделена лексема, или null в противном случае.
     * Если возникает ошибка разбора, выбрасывается исключение.
     */
    public LexerResult<T,F> parse_symbol(T symbol) throws LexerError {
        if(this.automaton.makeTransition(symbol)) {
            this.terminals.add(symbol);
            return null;
        };
        Iterator<F> iterator=this.automaton.getMarkers().iterator();
        if(!iterator.hasNext()) 
            throw new LexerError(String.format("Unexpected symbol '%s'", symbol.toString()));
        F lexeme=iterator.next();
        if(iterator.hasNext()) 
            throw new LexerError(String.format("Ambiguity %s,%s,..", lexeme.toString(), iterator.next().toString()));
        LexerResult<T,F> result=new LexerResult(this.terminals, lexeme);
        this.startNewToken();
        if(!this.automaton.makeTransition(symbol)) 
            throw new LexerError(String.format("Unexpected symbol '%s'", symbol.toString()));
        this.terminals.add(symbol);
        return result;
    };
    /**
     * Эта функция вызывается, когда строка для разбора закончена.
     * Выбрасывает исключение, если найти лексему не удалось выделить.
     */
    public LexerResult<T,F> parse_eol() throws LexerError {
        Iterator<F> iterator=this.automaton.getMarkers().iterator();
        if(!iterator.hasNext()) 
            throw new LexerError(String.format("Unexpected end of line"));
        F lexeme=iterator.next();
        if(iterator.hasNext()) 
            throw new LexerError(String.format("Ambiguity %s,%s,..", lexeme.toString(), iterator.next().toString()));        
        LexerResult<T,F> result=new LexerResult(this.terminals, lexeme);
        return result;
    };
    /**
     * Преобразует поток терминалов в поток лексем.
     * Обьект лексера после вызова этой функции использовать нельзя,
     * так как его состояние будет изменяться непредсказуемо.
     */
    public<R extends LexerResult<T,F>> Iterator<R> parse(Iterable<T> input) throws LexerError {
        this.reset();
        return new LexerIterator(this, input.iterator());
    };
    public<R extends LexerResult<T,F>> ILexerIterator<R> parseE(Iterable<T> input) throws LexerError {
        this.reset();
        return new LexerIterator(this, input.iterator());
    };
    /**
     * Хранилице автомата, делающего всю работу
     */
    private IFSA<T,F> automaton;
    /**
     * Хранилице подстроки, соответсвующей текущей лексеме
     */
    private List<T> terminals;
}

/**
 * Вспомогательный класс для итерирования по последовательности лексем
 * без их сохранения в памяти.
 */
class LexerIterator<T,F,P> implements Iterator<LexerResult<T,F>>, ILexerIterator<LexerResult<T,F>> {
    /**
     * Создает итератор по input с помощью лексера lexer
     */
    public LexerIterator(Lexer<T,F,P> lexer, Iterator<T> input) throws LexerError {
        this.lexer=lexer;
        this.input=input;
        this.eol=false;
        updateNext();
    }
    public boolean hasNextE() throws LexerError {
        return hasNext();
    }
    public LexerResult<T,F> nextE() throws LexerError {
        LexerResult<T,F> result=this.next;
        this.updateNext();
        return result;
    }
    public boolean hasNext() {
        return this.next!=null;
    }
    public LexerResult<T,F>	next() {
        try {
            return this.nextE();
        } catch(LexerError err) {
            throw new RuntimeException(err.toString());
        }
    }
    /**
     * Извлекает одну лексему из потока ввода
     */
    private void updateNext() throws LexerError {
        while(!this.eol) {
            if(this.input.hasNext()) 
                this.next=this.lexer.parse_symbol(this.input.next());
            else {
                this.next=this.lexer.parse_eol();
                this.eol=true;
            };
            if(this.next!=null) return;
        };
        this.next=null;
    }
    /**
     * Хранит следующую разобранную лексему
     */
    private LexerResult<T,F> next;
    /**
     * Лексер, который делает всю работу
     */
    private Lexer<T,F,P> lexer;
    /**
     * Строка для разбора
     */
    private Iterator<T> input;
    /**
     * Маркер того, что вся входная строка разобрана
     */
    private boolean eol;
}