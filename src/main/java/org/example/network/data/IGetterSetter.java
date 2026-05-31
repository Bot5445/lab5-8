package org.example.network.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс, предоставляющий методы для получения и установки данных коллекции.
 */
public interface IGetterSetter {
    /**
     * Возвращает отображение (Map) элементов коллекции, где ключом является ID.
     * @return карта элементов коллекции
     */
    Map<Integer, Person> getPerson();

    /**
     * Возвращает объект Person по его уникальному идентификатору.
     * @param id уникальный идентификатор объекта
     * @return объект Person или null, если объект не найден
     */
    Person getPersonById(Integer id);

    /**
     * Возвращает все элементы коллекции в виде коллекции значений.
     * @return коллекция объектов Person
     */
    Collection<Person> getAllPersons();

    /**
     * Полностью заменяет текущую коллекцию новым списком объектов.
     * @param persons список объектов для установки
     */
    void setPersons(List<Person> persons);
}