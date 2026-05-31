package org.example.network.data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Класс, представляющий координаты (x, y) на плоскости.
 */
@Getter
@Setter
public class Coordinates implements Serializable {
    private float x;
    private float y;

    /**
     * Создает объект координат.
     * @param x координата X
     * @param y координата Y
     */
    public Coordinates(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Возвращает массив строковых представлений имен осей координат.
     * Используется для формирования заголовков CSV/таблиц.
     * @return массив из двух элементов: ["coordinates: x", "coordinates: y"]
     */
    @Override
    public String toString() {
        return x + "," + y;
    }

    /**
     * Возвращает массив строковых представлений имен осей координат.
     * Используется для формирования заголовков CSV-файла или таблицы вывода.
     * @return массив из двух элементов: ["coordinates: x", "coordinates: y"]
     */
    public static String[] toStrings() {
        String[] str= new String[2];
        str[0] = "coordinates: " + 'x';
        str[1] = "coordinates: " + 'y';
        return str;
    }
}
