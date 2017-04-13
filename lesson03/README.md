# Теория компиляторов для неофитов

## Лабораторная работа №3: "Лексер"

[Содержание](../tutorial/content.md)

[Лекция: Лексический анализатор](../tutorial/lexer.md)

-------

В этой работе мы реализуем конечные автоматы, регулярные выражения и лексер.
Необходимая теория изложена в лекции по ссылке вверху.
Полный исходный код находится в папке проектов: [projects/regexp](../projects/regexp).
Проект целиком реализует компиляцию регулярных выражений,
сейчас нас будут интересовать только некоторые классы из пакетов
`terminal`, `fsa` и `lexer`.

Начнем с реализации конечного автомата.
Создадим интерфейс [fsa.IFSA]](../projects/regexp/src/main/java/fsa/IFSA.java) 
недетерминированного конечного автомата, декларирующего все необходимые функции
для запуска готового конечного автомата и просмотра его состояния.

```java
public interface IFSA<T, F> {
```

Конечный автомат состоит из некоторого числа состояний,
переходы между состояниями маркируются метками,
состояния также могут быть помечены, такие состояния считаются остановочными.
Конкретный вид состояния не важен для его работы,
поэтому мы не включаем его в интерфейс.
Метки переходов будут определяться алфавитом разбираемого языка,
в качестве которого можно выбрать ASCII, UNICODE,
или еще что-то, поэтому мы не будем ограничивать себя
одним типом, а введем параметр `T`, который будет задавать тип меток.
Маркеры остановочных состояний также могут быть разными,
например, это может быть перечисление или функция, задающая семантическое 
действие, поэтому маркеры остановочных состояний параметризуем
типом `F`.

Первый метод, который мы включим в интерфейс, будет перезапусать автомат,
т.е. переводить его в стартовое состояние, что нужно для многократного 
использования автомата.

```java
    void reset();
```

Следующий метод выполняет переход из текущего состояния по данной метке `label`.
Если такой переход в автомате возможен, то метод вернет `true`, и автомат
перейдет в новое состояние, в противном случае метод вернет `false`
и состояние автомата не изменится.
Интерфейс допускает работу с недетерминированными автоматами,
в этом случае текущих состояний может быть несколько и
переход осущесталяется сразу во все состояния, в которые возможен переход.

```java
    boolean makeTransition(T label);
```

Теперь обратимся к остановочным состояниям.
Мы разрешим состояниям иметь несколько маркеров, которые могут, например,
помечать, какую из лексем удалось свернуть.
Определим функцию, которая будет возвращать все маркеры текущего состояия.
Спрячем реализацию хранилища маркеров за интерфейсом `java.lang.Iterable`
и будем действовать подобным образом далее.

```java
    Iterable<F> getMarkers();
```

Наконец, создадим метод, который послужит примером использования
интерфейса.
Метод будет возвращать `true`, если строка `string` порождается автоматом
и `false` в противном случае.
Приведем реализацию по умолчанию, которая будет перезапускать автомат,
затем выполнять переходы до тех пор, пока есть символы в строке, 
а в конце проверит, является ли состояние остановочным.

```java
    default boolean match(Iterable<T> string) {
        this.reset();
        for(T label: string) 
            if(!this.makeTransition(label)) return false;
        return this.getMarkers().iterator().hasNext();
    };
}
```

Если автомат детерминированный, то он может иметь не более
одного маркера на состояние, в этом случае создание коллекции
маркеров нерационально использует ресурсы и не исключает возможность
возвращение некорректного значения.
Поэтому для детерминированного автомата мы создадим интерфейс
[fsa.IDFA]](../projects/regexp/src/main/java/fsa/IDFA.java),
который добавляет к `fsa.IFSA` одну функцию,
возвращающую единственный маркер состояния.

```java
public interface IDFA<T, F> extends IFSA<T, F> {
    F getMarker();
}
```

Любая реализация интерфейса должна удовлетворять следующим условиям:

1. Eсли состояние не остановочное, то `getMarker` возвращает `null`,
а `getMarkers` возвращает пустую коллекцию.

1. Если `getMarker` возвращает маркер `m`, то `getMarkers` должно
возвращать коллекцию из одного только `m`.

Перед тем, как мы перейдем к реализации интерфейсов,
создадим класс [fsa.State]](../projects/regexp/src/main/java/fsa/State.java), 
который будет хранить состояние автомата.
Состояний конечное состояние, больше мы ничего про состояния не знаем,
поэтому будем просто нумеровать их целыми числами.
Мы хотим гарантировать, что обьект состояния всегда
соответствует некоторому состоянию, поэтому запретим пользователям самим 
создавать обьекты состояния, их должен создавать только сам автомат.

```java
public class State {
     private int id;
     State(int id);
```

Однако иметь доступ к номеру состояния удобно для отладки и ускорения некоторых операций.
Создадим метод, который возвращает номер состояния.

```java
     public int getId() { return this.id; }
```

Также переопределим операции сравнения и хэш-функции,
так чтобы сравнивались номера состояний.

```java
     @Override public int hashCode() { return this.id; }
     @Override public boolean equals(Object obj) { 
        if(obj==null) return false;
        if(this.getClass()!=obj.getClass()) return false;
        State state=(State)obj;
        return this.id==state.id;
     };
```

Наконец, пусть строкове представление класса выводим номер состояния:

```java
     @Override public String toString() { return String.valueOf(this.id); };
}
```

Мы почти готовы к реализации конечного автомата,
однако перед тем, как начать, давайте подумаем как автомат будет
использоваться.
Определенный нами интерфейс позволяет делать переходы по
символам алфавита, что ествественно, так как символы алфавита 
мы читаем из разбираемой строки.
Однако при составлении автомата перечислять все символы 
перехода может быть как неудобно, так и расточительно.
Действительно, `Unicode` определяет более 100000 символов,
и если мы хотим создать переход по всем символам, кроме
нескольких, то создание перехода для каждого символа
представляется крайне нерациональным.
Вместо этого лучше задавать классы символов, по которым делается переход.
Тогда переходы, допустимые из текущего состояния,
можно хранить в виде квази ассоциативного массива,
ключами в котором будут классы, а извлекать элементы массива
можно по отдельным символам, представителям классов.
Если в качестве классов использовать сами символы, то новая коллекция
должна быть идентична `java.util.Map`.
Зададим интерфейс [terminal.IPredicateMap](../projects/regexp/src/main/java/terminal/IPredicateMap.java)
такой коллекции.

```java
public interface IPredicateMap<P extends ICharSet<K,P>,K,V, SELF extends IPredicateMap<P,K,V,SELF>> {
```

Здесь параметр `P` задает тип для класса символов,
параметр `K` задает тип для одного символа,
параметр `V` задает тип хранимого в коллекции массива.
Параметр `SELF` содержит тип самой коллекции, и используется для создания
экземпляров коллекции.
Зададим метод, который будет возвращать пустую коллекцию,
что позволит нам создавать новые экземпляры коллекции,
имея один экземпляр, даже не зная точный тип коллекции.

```java
    SELF empty();
```

Следующий метод позволит извлекать из коллекции значения,
в качестве ключа используется конкретный символ, поэтому его тип `K`.

```java
    V get(K key);
```

А этот метод позволяет помещать в массив новые элементы,
причем в качестве ключа используется класс символов,
поэтому его тип `P`.
Так как одному символу типа `K` должно соответствовать одно значение,
то ключи-классы не должны пересекаться.
Если ключи пересекаются, то выбрасывается исключение `IndexOutOfBoundsException`.

```java
    void put(P predicate, V value) throws IndexOutOfBoundsException;
```

Наконец, мы хотим извлекать полное содержание коллекции,
чтобы иметь возможность выполнять преобразование коллекции.

```java
    Iterable<Map.Entry<P,V>> entrySet();
}
```

Так как недетерминированный автомат может делать несколько
переходов по одному символу, то однозначное соответствие между
ключами и переходами, описываемое интерфейсом `terminal.IPredicateMap`
не пригодно для хранения переходов в недетерминированном автомате.
Чтобы работать с недетерминированными автоматами, мы определим 
новый интерфейс [terminal.IPredicateMap](../projects/regexp/src/main/java/terminal/IPredicateMap.java),
допускающий несклько значений для одного ключа:

```java
public interface IPredicateMultiMap<P,K,V, SELF extends IPredicateMultiMap<P,K,V,SELF>> {
    SELF empty();
    Iterable<V> get(K key);
    void put(P predicate, V value);
    Iterable<Map.Entry<P,V>> entrySet();
```

Отличие интерфейсов в том, что `get` может возвращатт несколько значений,
а `put` не выбрасывает исключение, если ключи пересекаются.
Наконец, создадим метод, добавляющий из коллекции `other` все элементы,
применив к ним предварительно функцию `map`.

```java
    default void mergeMap(IPredicateMultiMap<P,K,V,?> other, Function<V,V> map) {
        for(Map.Entry<P,V> entry: other.entrySet())
            this.put(entry.getKey(), map.apply(entry.getValue()));
    }
}
```

После длительной подготовки, перейдем к написанию класса
[fsa.FSA](../projects/regexp/src/main/java/fsa/FSA.java),
реализующего недетерминированный автомат.

```java
public class FSA<T,F,P> implements IFSA<T,F> {
```

Автомат делает переходы по символам типа `T`,
классы символов имеют тип `P`,
состояния маркируются метками типа `F`.
Структура автомата полностью описывается следующими свойствами:

```java
    protected int numberOfStates;
    protected HashMap<State, IPredicateMultiMap<P,T,State,?>> transitions;
    protected HashMap<State, HashSet<F>> markers;
```

Автомат имеет все состояния, начиная с `0` (стартовое состояние) 
и заканчивая `numberOfStates-1`.
Каждому состоянию `state` сопоставляется множество меток,
хранимых в `markers`.
Множество переходов для каждого состояния храним в `transitions`.

Мы храним в `activeStates` множество всех состояний, достигнутых
к настоящему времени, т.е. тех, в которые можно попасть из стартового
состояния переходами по переданным автомату символам.

```java
    protected HashSet<State> activeStates;
```

Так как тип хранилища для переходов может быть произвольным,
мы должны дать автомату подсказку, как создавать эти хранилища,
для этого мы храним одно из хранилищ внутри обьекта, 
и создаем новые с помощью функции `IPredicateMultiMap.empty()`.

```java
    protected IPredicateMultiMap<P,T,State,?> factory;
```

Этот экзмепляр хранилища передается конструктуру.
Других аргументов конструктор не имеет, создавая автомат,
не имеющий ни одного состояния.

```java
    public<M extends IPredicateMultiMap<P,T,State,M>> FSA(M factory) { ... };
```

Класс реализует интерфейс конечного автомата:

```java
    public void reset() { ... };
    public boolean makeTransition(T label) { ... };
    public Iterable<F> getMarkers() { ... };
```

Реализация интерфейса достаточно очевидна.
Метки состояний уже хранятся в `markers`.
Чтобы сделать переходы, нужно для каждого состояния из `activeStates`
взять из `transitions` все переходы по данному символу,
и записать цели этих переходов в новое состояние.
Чтобы перезапустить автомат, нужно перезаписать `activeStates`,
поместив в него только стартовое состояние.
Однако при выполнении `reset` и `makeTransitions`,
необходимо учитывать, что недетерминированный автомат может
содержать эпсилон-переходы, для выполнения которых не требуется
читать ни одного символа.
Определим метод `doEpsilonTransitions`, делающий все эпсилон-переходы
из текущего состояния, он будет вызываться из `reset` и `makeTransition`
после выполнения не эпсилон-переходов.

```java
    protected void doEpsilonTransition(Set<State> states) { ... };
```

Для конструирования автомата нам подтребуются метод, создающий новые состояния:

```java
    public State newState() { ... };
```

метод, добавляющий переход из состояния `from` в состояние `to`
по классу меток `label`:

```java
    public void newTransition(State from, State to, P label) { ... };
```

и метод, помечающий состояние `state` меткой `mark`:

```java
    public void markState(State state, F mark) { ... };
```

Для отладки будет удобно иметь выводить структуру автомата
в читаемом человеком виде:

```java
    @Override public String toString() { ... };
```

Текстовое представление имеет вид, подобный следующему примеру:

```
#0:0: eps>0 'b'>2 eps>1
#1: 'a'>2
#2: eps>1 'a'>0 'b'>1
```

Описание нового состояния начинается с символа `#`,
затем идет номер состояния и `:`.
Далее перечисляются метки состояния, если они есть, за ними `:`.
Затем список переходов из состояния перечисляется через пробел.
Сначала указывается метка перехода, потом `>`, потом состояние,
в которое осуществляется переход.
Если переход не имеет метки (т.е. эпсилон-переход),
то вместо метки пишется `eps`.
Если метка есть, то она указывается между обратных кавычек.

Класс `fsa.FSA` имеет еще несколько вспомогательных
методов, про которые мы поговорим, когда они потребуются.

Детерминированный автомат реализуется классом
[fsa.DFA](../projects/regexp/src/main/java/fsa/DFA.java).
Часть его методов почти идентична методам `fsa.SFA`,
но устраняет неоднозначность:

```java
public class DFA<T,F,P extends ICharSet<T,P>> implements IDFA<T, F> {
    public<M extends IPredicateMap<P,T,State,M>> DFA(M factory) { ... };
    public void reset() { ... };
    public boolean makeTransition(T label) { ... };
    public Iterable<F> getMarkers() { ... };
    public F getMarker() { ... };
    public State newState() { ... };
    public void newTransition(State from, State to, P label) throws IllegalArgumentException { ... };
    public void markState(State state, F mark) { ... };
    @Override public String toString() { ... };
```

Внутреннее хранилище также почти идентично `fsa.FSA`,
с поправкой на однозначность перехода, текущего состояния, маркеров состояний и т.п.

```java
    protected int activeState;
    protected int numberOfStates;
    protected List<IPredicateMap<P,T,State,?>> transitions;
    protected List<F> markers;
    protected IPredicateMap<P,T,State,?> factory;
```

Добавим новый конструктор, который будет конструировать эквивалентный детерминированный
автомат из недетерминированного автомата `automaton`.

```java
    public<M extends IPredicateMap<P,T,State,M>> DFA(M factory, FSA<T,F,P> automaton) {
```

Данный метод работает как описано в лекции, уточним только несколько деталей.
Так как недетерминированный автомат работает с классами символов,
а все переходы в детерминированном автомате должны быть различными,
нам нужно уметь находить пересечения классов.
Чтобы иметь возможность это делать, мы определим интерфейс
[terminal.ICharSet](../projects/regexp/src/main/java/terminal/ICharSet.java),
определяющий операции пересечений и вычитания множеств.

```java
public interface ICharSet<T, SELF extends ICharSet<T, SELF>> {
    SELF intersect(SELF other);
    Collection<SELF> subtract(SELF other);
}
```

Так как стандартные классы не реализуют `terminal.ICharSet`,
мы создадим свой класс [terminal.UChar](../projects/regexp/src/main/java/terminal/UChar.java),
для символов, обертывающий `char`,
т.е. хранящий символы из подмножества `Unicode`, и реализующий
весь необходимый функционал.

```java
public class UChar implements ICharSet<UChar, UChar> {
    private char value;
    public UChar(char c) { this.value=c; }
    public UChar(Character c) { this.value=c.charValue(); }
    public char toChar() { return this.value; }
    public UChar intersect(UChar other) {
        return (this.equals(other))?this:null;
    }
    public Collection<UChar> subtract(UChar other) {
        ArrayList<UChar> result=new ArrayList();
        if(this.value!=other.value) result.add(new UChar(this.value));
        return result;
    }
    @Override public boolean equals(Object obj) { ... }
    @Override public int hashCode() { ... }
    @Override public String toString() { ... };
```

Мы также поместим в этот класс статический метод для
преобразования строки в список символов, который мы можем передать
затем лексеру.

```java
    public static List<UChar> asList(String str) {
        return str.chars().mapToObj(i -> new UChar((char)i)).collect(Collectors.toList());
    };
}
```

Для построения автоматов для регулярных выражений
нам потребуются комбинаторы, выполняющие примитивные операции.
Поместим эти операторы в класс
[fsa.Combinators](../projects/regexp/src/main/java/fsa/Combinators.java).
Параметры типов имеют тоже значение, что и `fsa.FSA`.
Конструктор получает образец хранилища для переходов, аналогично
конструктору `fsa.FSA`.

```java
public class Combinators<T,F,P> {
    protected IPredicateMultiMap<P,T,State,?> factory;
    public<M extends IPredicateMultiMap<P,T,State,M>> Combinators(M factory) {
        this.factory=factory;
    }
```

Следующие методы возвращают недетерминированные автоматы для примитивных операций.

```java
    /** Автомат, принимающий только string с маркером остановочного состояния marker.  */
    public FSA<T,F,P> literal(Iterable<P> string, F marker) { ... }
    /** Автомат, принимающий любой из перечисленных символов `label` с маркером о.с. marker. */
    public FSA<T,F,P> anyOf(Iterable<P> labels, F marker) { ... }
    /** Обьединение автоматов automata */
    public FSA<T,F,P> union(Iterable<FSA<T,F,P>> automata) { ... }
    /** Конкатенация автоматов */
    public FSA<T,F,P> concatenation(Iterable<FSA<T,F,P>> automata) { ... }
    /** Автомат, исполняющий automaton любое число раз, включая ноль.
     *  Остановочное состояние для пустой строки маркируется marker.
     */
    public FSA<T,F,P> star(FSA<T,F,P> automaton, F marker) { ... }
    /** Автомат, повторяющий automaton один или более раз. */
    public FSA<T,F,P> repeat(FSA<T,F,P> automaton) { ... }
    /** Автомат исполняющий automaton или принимающий пустую строку.
     *  Остановочное состояние для пустой строки имеет маркер marker.
     */
    public FSA<T,F,P> option(FSA<T,F,P> automaton, F marker) { ... }
}
```

С использованием этих комбинаторов можно создать любое
регулярное выражение.
К сожалению, мы пока не можем создавать регулярное выражение из его текстового представления,
так как язык описания регулярных выражений контекстно-свободен, а мы пока
можем разбирать только регулярные выражения.

Теперь напишем лексер.
Лексер будет разбирать одновременно несколько регулярных выражений
и возвращать выражение, которое первым сработает.
Лексер будет пытаться собрать как можно больше символов в одно регулярное выражение.
Создадим класс для лексера и помести его в 
[lexer.Lexer](../projects/regexp/src/main/java/lexer/Lexer.java).
Также как и регулярные выражения, лексер будет читать символы типа `T`,
а переходы будет делать классам символов `P`.
Чтобы узнать, какую из лексем удалось собрать, мы будем помечать остановочные 
состояния маркерами типа `F`.

```java
public class Lexer<T,F,P> {
```

Конструктор лексера будет принимать на вход готовые автоматы,
каждый из которых соответствует своей лексеме.
Остановочные состояния должны быть уже помечены своим маркером для каждой лексемы.
Конструктор построит собственный автомат `this.automaton` для лексера,
который будет простым объединением автоматов, которые мы уже можем строить
с помощью `Combinators.union`.

```java
    private IDFA<T,F> automaton;
    public Lexer(List<FSA<T,F,P>> lexemes) { ... }
```

В отличии от регулярного выражения мы будем хранить состояние автомата
внутри объекта для лексера.
Поэтому нам потребуется метод для перезапуска лексера,
если мы захочем разбирать новую строку.

```java
    private State state;
    public void reset() { ... };
```

Метод `reset` должен перезапускать автомат внутри лексера,
однако также его придется перезапускать и после выделения каждой лексемы.
Поэтому мы создадим специализиорованный метод для перезапуска автомата
при старте разбора новой лексемы.

```java
    public void startNewToken() { 
        this.state=this.automaton.initialState();
        ...
    }
```

Самый важный метод лексера `parse_symbol`, который отдает лексеру новый символ для разбора.
Может оказаться, что такой символ не может входить ни в одну из лексем,
в этом случае лексер выбросит исключение, для которого мы создали
собственный класс [lexer.LexerError](../projects/regexp/src/main/java/lexer/LexerError.java).
Чтобы собрать лексему, прочитанного символа может быть недостаточно,
тогда метод вернет `null`.
Если лексему удалось выделить, то метод должен вернуть маркер,
который сообщит нам какая лексема найдена, и подстроку,
которую удалось свернуть в лексему.
Для хранения этих значений мы создали специальный класс:
[lexer.LexerResult](../projects/regexp/src/main/java/lexer/LexerResult.java).
Заметим, что лексема выделяется, если новый символ в нее уже не удается добавить,
т.е. последний прочитанный символ будет началом новой лексемы.

```java
    public LexerResult<T,F> parse_symbol(T symbol) throws LexerError { ... }
```

Когда разбираемая строка будет прочитана полностью,
может оказаться, что суффикс строки тоже формирует лексему,
которую мы однако не могли получить при вызове `parse_symbol`.
Поэтому нам потребуется аналогичный метод,
который должен вызываться в конце строки:

```java
    public LexerResult<T,F> parse_eol() throws LexerError { ... }
```

Метод `parse_symbol` делает переход в автомате,
до тех пор, пока перехода по предлагаемому символу не окажется.
Если дальше переходи некуда, то оба метода `parse_symbol` и `parse_eol`
должны выяснить, является ли состояние остановочным,
и если да, то вернуть маркеры лексем.
Так как лексер получает символы по одному, а вернуть должен
подстроку, отвечающую лексеме, то лексер должен накапливать
эту подстроку в переменной, назовем ее `terminals`.

```java
    private List<T> terminals;
```

Наконец, удобно иметь метод, который будет создавать по данному потоку
терминалов поток лексем.
Это избавит нас от необходимости многократно вызываться `parse_symbol`,
чтобы получить одну лексему.
Потоки мы будем передавать через итераторы, что дает большой простор для
конкретных реализаций.

```java
    public<R extends LexerResult<T,F>> Iterator<R> parse(Iterable<T> input) throws LexerError { ... }
    public<R extends LexerResult<T,F>> ILexerIterator<R> parseE(Iterable<T> input) throws LexerError { ... }
}
```

Стандартный интерфейс `java.util.Iterator` не может выбрасывать исключения,
мы же используем исключения для сообщений об ошибках,
поэтому мы создали собственный интерфейс итератора
[lexer.ILexerIterator](../projects/regexp/src/main/java/lexer/ILexerIterator.java)..

```java
public interface ILexerIterator<T> {
    boolean hasNextE() throws LexerError;
    T nextE() throws LexerError;
}
```

Класс [lexer.LexerResult](../projects/regexp/src/main/java/lexer/LexerResult.java).
для хранения результата позволяет вернуть лексему и соответствующую ей строку,
никаких других действий он не делает.

```java
public class LexerResult<T,F> {
    LexerResult(List<T> string, F lexeme) { ...  }
    public List<T> getString() { ... }
    public F getLexeme() { ... }
    @Override public String toString() { ... }
    @Override public boolean equals(Object obj) { ... };
    private List<T> string;
    private F lexeme;
}
```

Одной из важных задач синтаксического анализа текста является
обнаружение ошибок, о которых человеку нужно сообщить в понятных ему терминах.
В частности, человеку нужно сообщить место возникновения ошибки.
Обычно место ошибки задает номеро строки и столбца.
До сих пор мы не конкретизировали понятие терминала,
однако чтобы говорить о номере строки, необходимо
выделить среди нетерминалов символы перевода строки и т.п.
Мы создадим специальный класс 
[lexer.CharLexer](../projects/regexp/src/main/java/lexer/CharLexer.java),
который расширяет
[lexer.Lexer](../projects/regexp/src/main/java/lexer/Lexer.java)
тем, что считает положение лексемы в файле,
но работает только с терминалами типа `UChar`.

```java
public class CharLexer<F,P> extends Lexer<UChar,F,P> {
    public CharLexer(List<FSA<UChar,F,P>> lexemes) { ... }
    public void reset() { ... };
    @Override public CharLexerResult<F> parse_symbol(UChar symbol) throws CharLexerError { ... }
    @Override public CharLexerResult<F> parse_eol() throws CharLexerError { ... }
    private int currentLine;
    private int currentColumn;
    private int line;
    private int column;
}
```

Класс `CharLexer` использует для лексического анализа методы своего
предка, однако дополнительно обновляет при каждом чтении символа
текущее положение `currentLine`, `currentColumn` в файле,
а при выделении лексемы обновляет положение начала лексемы
`line`, `column`.

Положение лексемы в файле лексер должен каким-либо образом вернуть,
для этого мы расширяем класс результата `LexerResult`,
добавляя в новый класс 
[lexer.CharLexerResult](../projects/regexp/src/main/java/lexer/CharLexerResult.java),
поля для номера строки и столбца:

```java
public class CharLexerResult<F> extends LexerResult<UChar, F> {
    CharLexerResult(List<UChar> string, F lexeme, int line, int column) { ... }
    public int getLine() { ... }
    public int getColumn() { ... }
    @Override public boolean equals(Object obj) { ... };
    @Override public String toString() { ...  };
    private int line;
    private int column;
}
```

Наконец, исключение, выбрасываемое при ошибке лексического разбора,
должно содержать информацию о месте ошибки.
Поэтому наш новый лексер `CharLexer` выбрасывает расширение
исключения `LexerError`, которое назовем
[lexer.CharLexerError](../projects/regexp/src/main/java/lexer/CharLexerError.java).

```java
public class CharLexerError extends LexerError {
    public CharLexerError(String message, int line, int column) { ... }
    public CharLexerError(String message, Throwable throwable) { ... }
    @Override public String getMessage() { ... };
    private int line;
    private int column;
}
```

Для проверки корректности работы автоматов и лексера
искользуются юнит-тесты, расположенные в поддиректориях
[test/java/*](../projects/regexp/test/java/).
Код юнит-тестов также дает использования наших класссов.

В качестве еще одного примера реализуем программу,
которая будет преобразовывать последовательность символов
на стандартном потоке ввода в последовательность лексем
для разбора регулярных выражений, которые будет записывать
в стандартный поток ввода.

```java
public class App {
```

Так как наш лексер работает с итераторами, а стандартные методы Java предпочитают
коллекции, то реализуем вспосогательные метод, собирающий все данные из 
итератора в список.

```java
    public static<T> List<T> asList(Iterator<T> src) { ... };
```

Создадим перечисление со списком всех токенов.

```java
    enum Token {
        LITERAL, STAR, PLUS, BEGIN, END, OR, OPTION, ANY;
    }
```

Следующий метод возвращает лексер, который будет разпознавать символы,
имеющие особенный смысл внутри регулярных выражений, а остальные
будет запаковывать в `Token.LITERAL`.
Реализация метода активно использует реализованные нами нарее комбинаторы.

```java
    static CharLexer<Token,UChar> makeLexer() {
        Combinators<UChar,Token,UChar> combinators=new Combinators(new KeyPredicateMultiMap());
        FSA<UChar,Token,UChar> star=combinators.literal(UChar.asList("*"),Token.STAR);
        FSA<UChar,Token,UChar> plus=combinators.literal(UChar.asList("+"),Token.PLUS);
        FSA<UChar,Token,UChar> option=combinators.literal(UChar.asList("?"),Token.OPTION);
        FSA<UChar,Token,UChar> begin=combinators.literal(UChar.asList("("),Token.BEGIN);
        FSA<UChar,Token,UChar> end=combinators.literal(UChar.asList(")"),Token.END);
        FSA<UChar,Token,UChar> or=combinators.literal(UChar.asList("|"),Token.OR);
        FSA<UChar,Token,UChar> any=combinators.literal(UChar.asList("."),Token.ANY);
        HashSet<UChar> reserved=new HashSet(UChar.asList("*+?()|\\."));
        HashSet<UChar> ordinary=new HashSet();
        for(char c=32; c<127; c++) {
            UChar uc=new UChar(c);
            if(!reserved.contains(uc)) ordinary.add(uc);
        };
        FSA<UChar,Token,UChar> symbolOrdinary=combinators.anyOf(ordinary,Token.LITERAL);
        FSA<UChar,Token,UChar> symbolEscaped=combinators.concatenation(Arrays.asList(
            combinators.literal(UChar.asList("\\"),Token.LITERAL),
            combinators.anyOf(reserved,Token.LITERAL)
        ));
        FSA<UChar,Token,UChar> symbol=combinators.union(Arrays.asList(symbolOrdinary,symbolEscaped));
        return new CharLexer(Arrays.asList(star, plus, option, begin, end, or, symbol, any));
    };
```

Наконец реализуем входную точку в программу метод `main`,
который будет последовательно считывать из стандартного потока ввода
строку за строкой запускать на каждой строке лексер.

```java
    public static void main(String[] args) throws IOException {
        CharLexer<Token,UChar> lexer=makeLexer();
        BufferedReader input=new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String str;
        System.out.println("Enter regexp:");
        while((str=input.readLine())!=null) {
            List<UChar> list=UChar.asList(str);
            try {
                ILexerIterator<LexerResult<UChar,Token>> iterator=lexer.parseE(list);
                while(iterator.hasNextE())
                    System.out.println(iterator.nextE()); 
            } catch(LexerError error) {
                System.out.println(error);
            };
        }
    }
}
```

-------

[Содержание](../tutorial/content.md)

[Лекция: Лексический анализатор](../tutorial/lexer.md)
