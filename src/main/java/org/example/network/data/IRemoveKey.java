package org.example.network.data;

public interface IRemoveKey {
    /**
     * Удаляет все элементы, ID которых меньше ID заданного шаблона.
     *
     * @param template объект-шаблон для сравнения
     * @return количество удаленных элементов
     */
    int removeLower(Person template);

    /**
     * Удаляет все элементы, ключ которых меньше заданного.
     *
     * @param thresholdId пороговое значение ID
     * @return количество удаленных элементов
     */
    int removeLowerKey(int thresholdId);
}
