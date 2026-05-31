package org.example.network.data;

import java.text.ParseException;

/**
 * Утилитный класс для динамического обновления и получения значений полей объекта {@link Person} по их названию.
 */
public class PersonFieldUpdater {

    /**
     * Обновляет указанное поле объекта новым значением.
     * @param oldPerson исходный объект
     * @param fieldName название поля (например, "height", "name")
     * @param newValue строковое представление нового значения
     * @return новый объект Person с обновленным полем
     * @throws IllegalArgumentException если поле не найдено или значение невалидно
     */
    public static Person updateField(Person oldPerson, String fieldName, String newValue) throws ParseException {
        // Превращаем человека в массив строк (как это было в командах)
        String[] values = oldPerson.toString().split(",");
        String[] headers = Person.getHeaders(); // Получаем заголовки ["id", "name", ...]

        // Ищем индекс поля (та самая логика цикла)
        int indexToUpdate = -1;
        for (int i = 0; i < headers.length; i++) {
            // Сравниваем имя поля с заголовком (убираем префиксы, если есть, например "coordinates: x")
            String headerName = headers[i].contains(":") ? headers[i].split(":")[1].trim() : headers[i].trim();

            if (headerName.equalsIgnoreCase(fieldName)) {
                indexToUpdate = i;
                break;
            }
        }

        if (indexToUpdate == -1) {
            throw new IllegalArgumentException("Поле '" + fieldName + "' не найдено.");
        }
        if (headers[indexToUpdate].equalsIgnoreCase("id")) {
            throw new IllegalArgumentException("Изменение ID запрещено.");
        }

        // Меняем значение
        values[indexToUpdate] = newValue;

        // Собираем объект заново через Фабрику (это решает проблему SRP)
        return PersonFactory.createFromStringArray(values);
    }

    /**
     * Получает строковое представление значения указанного поля объекта.
     * @param person объект Person
     * @param fieldName название поля
     * @return строковое значение поля
     * @throws IllegalArgumentException если поле не существует
     */
    public static String getFieldValue(Person person, String fieldName) {
        // Аналогичная логика поиска индекса, но возврат значения
        String[] values = person.toString().split(",");
        String[] headers = Person.getHeaders();

        for (int i = 0; i < headers.length; i++) {
            String headerName = headers[i].contains(":") ? headers[i].split(":")[1].trim() : headers[i].trim();
            if (headerName.equalsIgnoreCase(fieldName)) {
                return values[i];
            }
        }
        throw new IllegalArgumentException("Поле не найдено");
    }
}
