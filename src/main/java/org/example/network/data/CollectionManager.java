package org.example.network.data;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Реализация менеджера коллекции. Хранит элементы {@link Person} в {@link TreeMap}.
 */
public class CollectionManager implements ICollManager {
    private final Map<Integer, Person> collection = new TreeMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();


    /**
     * @return коллекцию
     */
    @Override
    public Map<Integer, Person> getPerson() {
        lock.readLock().lock();
        try {
            return new TreeMap<>(collection); // Возвращаем копию для безопасности
        } finally {
            lock.readLock().unlock();
        }
    }
    /**
     * Добавляет объект Person в коллекцию. Ключом становится ID объекта.
     * Если объект с таким ID уже существует, он будет перезаписан.
     *
     * @param person объект для добавления
     */
    @Override
    public void addPerson(Person person) {
        lock.writeLock().lock();
        try {
            collection.put(person.getId(), person);
        } finally {
            lock.writeLock().unlock();
        }
    }
    /**
     * Удаляет объект из коллекции по его ID.
     *
     * @param id ID объекта для удаления
     */
    @Override
    public void deletePerson(Integer id) {
        lock.writeLock().lock();
        try {
            collection.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
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
        lock.writeLock().lock();
        try {
            if (collection.containsKey(id)) {
                collection.put(id, person);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Отчищает коллекцию
     */
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            collection.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @param id ID человека
     * @return есть ли человек в коллекции
     */
    @Override
    public boolean containsId(Integer id) {
        lock.readLock().lock();
        try {
            return collection.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @param id ID человека
     * @return Person по ID
     */
    @Override
    public Person getPersonById(Integer id) {
        lock.readLock().lock();
        try {
            return collection.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @param persons заново создает коллекцию
     */
    @Override
    public void setPersons(List<Person> persons) {
        lock.writeLock().lock();
        try {
            collection.clear();
            for (Person p : persons) {
                collection.put(p.getId(), p);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @return получить всю коллекцию
     */
    @Override
    public Collection<Person> getAllPersons() {
        lock.readLock().lock();
        try {
            return collection.values().stream().collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    /**
     * @return пустая ли коллекция
     */
    @Override
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return collection.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Генерирует уникальный ID для нового элемента.
     * @return новый ID
     */
    @Override
    public int generateNextId() {
        lock.readLock().lock();
        try {
            if (collection.isEmpty()) return 1;
            return collection.keySet().stream()
                    .max(Integer::compareTo)
                    .orElse(0) + 1;
        } finally {
            lock.readLock().unlock();
        }
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
        lock.writeLock().lock();
        try {
            List<Integer> idsToRemove = collection.entrySet().stream()
                    .filter(entry -> entry.getValue().compareTo(template) < 0)
                    .map(Map.Entry::getKey)
                    .toList();
            idsToRemove.forEach(collection::remove);
            return idsToRemove.size();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Удаляет все элементы коллекции, ключ (ID) которых меньше переданного порогового значения.
     *
     * @param thresholdId пороговый ID
     * @return количество удаленных элементов
     */
    @Override
    public int removeLowerKey(int thresholdId) {
        lock.writeLock().lock();
        try {
            List<Integer> idsToRemove = collection.keySet().stream()
                    .filter(key -> key < thresholdId)
                    .toList();
            idsToRemove.forEach(collection::remove);
            return idsToRemove.size();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Возвращает строковое представление коллекции.
     * <p>Формирует строку, где каждый элемент коллекции представлен в виде пары
     * "ID: строковое_представление_Person", разделенных символом переноса строки.</p>
     * Метод потокобезопасен, так как использует блокировку на чтение {@code lock.readLock()}.
     *
     * @return строка, содержащая отформатированные данные всех элементов коллекции
     */
    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return collection.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue().toString())
                    .collect(Collectors.joining("\n"));
        } finally {
            lock.readLock().unlock();
        }
    }

}
