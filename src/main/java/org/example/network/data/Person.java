package org.example.network.data;

import java.io.Serial;
import java.io.Serializable;

import java.time.LocalDateTime;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Главный объект данных, хранящийся в коллекции.
 * Содержит информацию о человеке: координаты, рост, паспортные данные, цвет волос и локацию.
 */
@Getter
@AllArgsConstructor
public final class Person implements Serializable, Comparable<Person> {
    @Setter
    private String owner; // Имя пользователя, создавшего объект

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
     */
    @Setter
    private Integer id;
    /**
     * Поле не может быть null, Строка не может быть пустой
     */
    private final String name;
    /**
     * Поле не может быть null
     */
    private final Coordinates coordinates;
    /**
     * Поле не может быть null, Значение этого поля должно генерироваться автоматически
     */
    @Setter
    private LocalDateTime creationDate;
    /**
     * Поле не может быть null, Значение поля должно быть больше 0
     */
    private final Long height;

    /**
     * Поле может быть null
     */
    @Setter
    private String passportID;

    /**
     * Поле может быть null
     */
    @Setter
    private Country nationality;
    /**
     * Поле может быть null
     * */
    @Setter
    private Color hairColor;

    /**
     * Поле не может быть null
     */
    @NonNull
    private final Location location;

    /**
     * Конструктор для создания объекта Person с основными параметрами.
     * Дата создания устанавливается автоматически текущей.
     * Поля passportID, nationality и hairColor инициализируются как null.
     * @param id уникальный идентификатор
     * @param name имя человека
     * @param coordinates координаты
     * @param height рост
     * @param location местоположение
     * @throws IllegalArgumentException если location равен null
     */
    public Person(Integer id, String name, Coordinates coordinates, Long height, Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location не может быть null");
        }
        this.id = id; //abs((new Random()).nextInt())
        this.name = name;
        this.coordinates = coordinates;

        this.creationDate = LocalDateTime.now();

        this.height = height;
        this.location = location;
    }

    /**
     * Возвращает строковое представление объекта в формате CSV.
     * Значения null заменяются строкой "null".
     * @return строка с полями объекта, разделенными запятыми
     */
    @Override
    public String toString() {
        String hairColor = this.hairColor != null ? this.hairColor.toString() : "null";
        String nationality = this.nationality != null ? this.nationality.toString() : "null";
        String dateStr = PersonFactory.formatDate(this.creationDate);

        // Убрали owner отсюда, чтобы не ломать формат CSV
        return id.toString() + "," + name + "," + coordinates.toString() + ","
                + dateStr + "," + height.toString() + "," + passportID + ","
                + hairColor + "," + nationality + "," + location;
    }

    /**
    * Возвращает массив заголовков всех полей класса для формирования шапки таблицы/CSV.
    * @return массив имен полей
    */
    public static String[] getHeaders(){
        return concat(
            new String[]{"id", "name"},
            Coordinates.toStrings(),
            new String[]{"creationDate", "height", "passportID", "hairColor", "nationality"},
            Location.toStrings());
    }

    /**
     * Объединяет несколько массивов строк в один.
     * @param arrays массивы строк для объединения
     * @return объединенный массив строк
     */
    private static String[] concat(String[]... arrays) {
        return Arrays.stream(arrays)
                .flatMap(Arrays::stream)
                .toArray(String[]::new);
    }

    /**
     * Сравнивает кто больше
     * @param o the object to be compared.
     * @return число какое будет больше
     */
    @Override
    public int compareTo(Person o) {
        int heighCompare = Long.compare(this.height, o.height);

        if (heighCompare != 0) {
            return heighCompare;
        }
        return Long.compare(this.id, o.id);
    }
}

