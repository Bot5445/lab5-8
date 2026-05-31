package org.example.network.data;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Класс, представляющий местоположение человека.
 */
@Getter
public class Location implements Serializable {
    private final long x;
    /**
     * Поле не может быть null
     */
    private final Double y;

    /**
     * Поле может быть null
     */
    @Setter
    private String name;

    /**
     * Создает объект локации.
     *
     * @param x координата X
     * @param y координата Y (не может быть null)
     */
    public Location(long x, Double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Возвращает строковое представление объекта в формате CSV.
     *
     * @return строка формата "x,y,name"
     */
    public String toString() {
        return x + "," + y + "," + name;
    }

    /**
     * Возвращает массив заголовков полей класса для таблицы вывода.
     *
     * @return массив строк с названиями полей Location
     */
    public static String[] toStrings() {
        String[] str = new String[3];
        str[0] = "Location: " + 'x';
        str[1] = "Location: " + 'y';
        str[2] = "Location: " + "name";
        return str;
    }
}
