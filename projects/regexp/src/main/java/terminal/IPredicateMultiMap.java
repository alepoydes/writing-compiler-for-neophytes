package wcn.terminal;

import java.util.Optional;
import java.util.Map;
import java.util.function.Function;

/**
 * Интерфейс для ассоциативного массива, ключами которого могут быть
 * предткаты, диапазоны значений и т.п.
 * Эта коллекция похожа на java.util.Map<K,V>,
 * тем что тоже ищет значения V по ключу K,
 * однако IPredicateMultiMap может добавлять ключи группами,
 * где группа описывается типом P.
 */
public interface IPredicateMultiMap<P,K,V, SELF extends IPredicateMultiMap<P,K,V,SELF>> {
    /**
     * Создает пустую коллекцию
     */
    SELF empty();
    /**
     * Ищет значение для ключа key.
     */
    Iterable<V> get(K key);
    /**
     * Добавляет значение value для набора ключей, удовлетворяющих
     * предикату predicate.
     */
    void put(P predicate, V value);
    /**
     * Возвращает все переходы.
     */
    Iterable<Map.Entry<P,V>> entrySet();
    /**
     * Оьединяет массив с массивом other.
     */
    default void mergeMap(IPredicateMultiMap<P,K,V,?> other, Function<V,V> map) {
        for(Map.Entry<P,V> entry: other.entrySet())
            this.put(entry.getKey(), map.apply(entry.getValue()));
    }
}