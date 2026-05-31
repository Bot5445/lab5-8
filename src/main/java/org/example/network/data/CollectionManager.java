package org.example.network.data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация менеджера коллекции. Хранит элементы {@link Person} в {@link TreeMap}.
 */
public class CollectionManager implements ICollManager {
    private final Map<Integer, Person> collection = new TreeMap<>();


    /**
     * @return коллекцию
     */
    @Override
    public Map<Integer, Person> getPerson() {
        return collection;
    }

    /**
     * Добавляет объект Person в коллекцию. Ключом становится ID объекта.
     * Если объект с таким ID уже существует, он будет перезаписан.
     *
     * @param person объект для добавления
     */
    @Override
    public void addPerson(Person person) {
        collection.put(person.getId(), person);
    }

    /**
     * Удаляет объект из коллекции по его ID.
     *
     * @param id ID объекта для удаления
     */
    @Override
    public void deletePerson(Integer id) {
        collection.remove(id);
    }

    /**
     * Заменяет старый объект с указанным ID на новый.
     *
     * @param id     ID заменяемого элемента
     * @param person новый объект Person
     * @throws IllegalArgumentException если элемента с таким ID нет в коллекции
     */
    @Override
    public void updatePerson(Integer id, Person person) {
        if (collection.containsKey(id)) {
            collection.remove(id);
            collection.put(id, person);
        } else {
            throw new IllegalArgumentException("No id");
        }
    }

    /**
     * Отчищает коллекцию
     */
    @Override
    public void clear() {
        collection.clear();
    }

    /**
     * @param id ID человека
     * @return есть ли человек в коллекции
     */
    @Override
    public boolean containsId(Integer id) {
        return collection.containsKey(id);
    }

    /**
     * @param id ID человека
     * @return Person по ID
     */
    @Override
    public Person getPersonById(Integer id) {
        return collection.get(id);
    }

    /**
     * @param persons заново создает коллекцию
     */
    @Override
    public void setPersons(List<Person> persons) {
        collection.clear();
        for (Person p : persons) {
            collection.put(p.getId(), p);
        }
    }

    /**
     * @return получить всю коллекцию
     */
    @Override
    public Collection<Person> getAllPersons() {
        return collection.values(); // Возвращает коллекцию значений напрямую
    }

    /**
     * @return пустая ли коллекция
     */
    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    /**
     * Генерирует уникальный ID для нового элемента.
     * @return новый ID
     */
    public int generateNextId() {
        if (collection.isEmpty()) return 1;
        // Используем Stream API для поиска максимального ключа
        return collection.keySet().stream()
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    /**
     * Удаляет из коллекции все элементы, которые меньше заданного шаблона.
     * Использует естественный порядок сортировки Person (compareTo).
     *
     * @param template объект-шаблон для сравнения
     * @return количество удаленных элементов
     */
    @Override
    public int removeLower(Person template) {
        // Собираем ID элементов, которые меньше шаблона, и удаляем их
        List<Integer> idsToRemove = collection.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(template) < 0)
                .map(Map.Entry::getKey)
                .toList();

        idsToRemove.forEach(collection::remove);
        return idsToRemove.size();
    }

    /**
     * Удаляет все элементы коллекции, ключ (ID) которых меньше переданного порогового значения.
     *
     * @param thresholdId пороговый ID
     * @return количество удаленных элементов
     */
    @Override
    public int removeLowerKey(int thresholdId) {
        List<Integer> idsToRemove = collection.keySet().stream()
                .filter(key -> key < thresholdId)
                .toList();

        idsToRemove.forEach(collection::remove);
        return idsToRemove.size();
    }

    @Override
    public String toString() {
        return collection.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue().toString())
                .collect(Collectors.joining("\n"));
    }

}
