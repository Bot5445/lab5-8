package org.example.network.data;

/**
 * Интерфейс для проверки наличия ключей и состояния пустоты коллекции.
 */
public interface IsKey {
    /**
     * Проверяет, существует ли элемент с указанным ID в коллекции.
     * @param id идентификатор для проверки
     * @return true, если элемент существует, иначе false
     */
    boolean containsId(Integer id);

    /**
     * Проверяет, является ли коллекция пустой.
     * @return true, если коллекция не содержит элементов, иначе false
     */
    boolean isEmpty();
}