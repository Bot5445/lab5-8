package org.example.network.data;

/**
 * Интерфейс, определяющий базовые операции CRUD (Create, Read, Update, Delete) для управления коллекцией.
 */
public interface ICrud {
    /**
     * Добавляет новый объект в коллекцию.
     * @param person объект для добавления
     */
    void addPerson(Person person);

    /**
     * Удаляет объект из коллекции по его ID.
     * @param id идентификатор удаляемого объекта
     */
    void deletePerson(Integer id);

    /**
     * Обновляет (заменяет) объект с указанным ID на новый.
     * @param id идентификатор обновляемого объекта
     * @param person новый объект для замены
     */
    void updatePerson(Integer id, Person person);

    /**
     * Очищает коллекцию, удаляя все элементы.
     */
    void clear();
}