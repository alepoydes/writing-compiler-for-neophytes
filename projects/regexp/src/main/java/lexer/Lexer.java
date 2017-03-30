package wcn.lexer;

import wcn.fsa.*;
import wcn.terminal.*;

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
    public Lexer(List<FSA<T,F,P>> lexemes) {
        FSA<T,F,P> fsa;
        if(!lexemes.isEmpty()) {
            Combinators<T,F,P> combinators=new Combinators(lexemes.get(0).getFactory());
            fsa=combinators.union(lexemes);
        } else fsa=new FSA(new KeyPredicateMultiMap());
        this.automaton=new DFA(new KeyPredicateMap(), fsa);
        this.reset();
    }
    public void reset() {
        this.startNewToken();
    };
    public void startNewToken() {
        this.state=this.automaton.initialState();
        this.terminals=new LinkedList();
    };
    /**
     * Главная функция для лексического анализа.
     * Принимает терминал. 
     * Возвращает LexerResult, если выделена лексема, или null в противном случае.
     * Если возникает ошибка разбора, выбрасывается исключение.
     */
    public LexerResult<T,F> parse_symbol(T symbol) throws LexerError {
        State nextState=this.automaton.makeTransition(this.state, symbol);
        if(nextState!=null) {
            this.state=nextState;
            this.terminals.add(symbol);
            return null;
        };
        F lexeme=this.automaton.getMarker(this.state);
        if(lexeme==null) 
            throw new LexerError(String.format("Unexpected symbol '%s'", symbol.toString()));
        LexerResult<T,F> result=new LexerResult(this.terminals, lexeme);
        this.startNewToken();
        nextState=this.automaton.makeTransition(this.state, symbol);
        if(nextState==null) 
            throw new LexerError(String.format("Unexpected symbol '%s'", symbol.toString()));
        this.state=nextState;
        this.terminals.add(symbol);
        return result;
    };
    /**
     * Эта функция вызывается, когда строка для разбора закончена.
     * Выбрасывает исключение, если найти лексему не удалось выделить.
     */
    public LexerResult<T,F> parse_eol() throws LexerError {
        F lexeme=this.automaton.getMarker(this.state);
        if(lexeme==null) throw new LexerError(String.format("Unexpected end of line"));
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
    private IDFA<T,F> automaton;
    /** Текущее состояние автомата. */
    private State state;
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