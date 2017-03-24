package wcn.terminal;

import java.util.Optional;
import java.util.Map;
import java.util.function.Function;

/**
 * Интерфейс для ассоциативного массива, ключами которого могут быть
 * предикаты, диапазоны значений и т.п.
 * Отличается от IPredicateMultiMap тем, что хранит только одно значение
 * для каждого ключа.
 * Предикаты, используемые вместо ключей, должны поддерживать операцию пересечения.
 */
public interface IPredicateMap<P extends ICharSet<K,P>,K,V, SELF extends IPredicateMap<P,K,V,SELF>> {
    /**
     * Создает пустую коллекцию
     */
    SELF empty();
    /**
     * Возвращает значение для ключа key.
     * Если такого ключа нет, то вернет null.
     */
    V get(K key);
    /**
     * Добавляет значение value для набора ключей, удовлетворяющих
     * предикату predicate.
     * Если добавляемые ключи перемекаются с уже имеющимися,
     * то выбрасывается исключение.
     */
    void put(P predicate, V value) throws IndexOutOfBoundsException;
    /**
     * Возвращает все переходы.
     */
    Iterable<Map.Entry<P,V>> entrySet();
}