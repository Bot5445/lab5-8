package org.example.network.data;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Фабрика для создания и валидации объектов {@link Person}.
 * <p>Отвечает за преобразование строковых данных (например, из консоли или CSV-файла)
 * в валидированный объект Person. Инкапсулирует логику парсинга и проверки ограничений полей.</p>
 */
public class PersonFactory {

    /** Форматтер для парсинга и форматирования даты в формате "день.месяц.год" (например, 25.12.2023) */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Проверяет и парсит строку в ID (Integer).
     * @param input строковое представление ID (может быть "null" или пустым)
     * @return распарсенное положительное число или null, если входная строка пустая/"null"
     * @throws IllegalArgumentException если строка не является числом или число <= 0
     */
    public static Integer validateAndParseId(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty() || input.equalsIgnoreCase("null")) return null;
        try {
            int id = Integer.parseInt(input.trim());
            if (id <= 0) throw new IllegalArgumentException("ID должен быть > 0");
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID должен быть целым числом! " + e.getMessage());
        }
    }

    /**
     * Проверяет и парсит имя.
     * Имя должно содержать только латиницу, дефис и символ &.
     * @param input строка с именем
     * @return валидированная и очищенная строка имени
     * @throws IllegalArgumentException если имя пустое или содержит недопустимые символы
     */
    public static String validateAndParseName(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) throw new IllegalArgumentException("Имя не может быть пустым");
        String name = input.trim();
        // Разрешаем латиницу, дефис и символ &
        if (!name.matches("^[a-zA-Z&\\-]+$")) {
            throw new IllegalArgumentException("Имя \"" + name + "\" должно содержать только латиницу, дефис и символ &");
        }
        return name;
    }

    /**
     * Парсит значение Enum из строки без учета регистра.
     * @param <E> тип Enum-а
     * @param enumClass класс Enum-а
     * @param str строковое представление значения
     * @return соответствующий элемент Enum или null, если строка пустая/"null"
     * @throws IllegalArgumentException если строка не соответствует ни одному значению Enum
     */
    public static <E extends Enum<E>> E parseEnum(Class<E> enumClass, String str) throws IllegalArgumentException {
        if (str == null || str.trim().isEmpty() || str.equalsIgnoreCase("null")) return null;
        try {
            return Enum.valueOf(enumClass, str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Недопустимое значение '" + str.trim() + "'. Ожидается одно из: "
                            + Arrays.toString(enumClass.getEnumConstants()), e);
        }
    }

    /**
     * Парсит дату из строки и преобразует её в {@link LocalDateTime}.
     * <p>Если строка "null" или пустая, возвращает текущую дату и время (авто-генерация).</p>
     * <p>Ожидает формат dd.MM.yyyy. Время устанавливается на 00:00:00 начала дня.</p>
     *
     * @param dateStr строковое представление даты
     * @return объект LocalDateTime
     * @throws IllegalArgumentException если формат строки неверен или дата в будущем
     */
    private static LocalDateTime parseDate(String dateStr) throws IllegalArgumentException {
        if (dateStr == null || dateStr.trim().isEmpty() || dateStr.equalsIgnoreCase("null")) {
            return LocalDateTime.now(); // Авто-генерация даты создания
        }

        try {
            // Парсим строку в LocalDate, затем переводим в начало дня (00:00)
            LocalDate parsedDate = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            LocalDateTime dateTime = parsedDate.atStartOfDay();

            // Проверка, что дата не в будущем
            if (dateTime.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("Дата создания не может быть в будущем");
            }
            return dateTime;
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Неверный формат даты: '" + dateStr.trim() + "'. Ожидается формат: dd.MM.yyyy (например, 25.12.2023)");
        }
    }

    /**
     * Парсит строку в Float с проверкой на пустоту.
     * @param input строковое значение
     * @param fieldName имя поля для сообщения об ошибке
     * @return число Float
     * @throws NumberFormatException если строка пуста или не является числом
     */
    public static Float parseFloat(String input, String fieldName) throws NumberFormatException {
        if (input == null || input.trim().isEmpty())
            throw new NumberFormatException(fieldName + " не может быть пустым");
        return Float.parseFloat(input.trim());
    }

    /**
     * Создает объект Person из массива строк (CSV формата).
     * <p>Массив должен содержать поля в порядке: ID, Name, CoordX, CoordY, Date, Height,
     * PassportID, HairColor, Nationality, LocX, LocY, LocName.</p>
     * <p>Если массив короче ожидаемого, недостающие поля заполняются null.</p>
     *
     * @param str массив строковых данных
     * @return созданный и валидированный объект Person
     * @throws IllegalArgumentException если данные невалидны
     */
    public static Person createFromStringArray(String[] str) throws IllegalArgumentException {
        // Защита от ArrayIndexOutOfBoundsException: дополняем массив null, если данных не хватило
        int expectedLength = Person.getHeaders().length;
        if (str.length < expectedLength) {
            str = Arrays.copyOf(str, expectedLength);
        }

        // 1. ID (на клиенте генерируется заглушка, реальный ID назначается сервером)
        Integer id = validateAndParseId(str[0]);
        if (id == null) {
            id = 0; // Временное значение, сервер перезапишет его через generateNextId()
        }

        // 2. Имя
        String name = validateAndParseName(str[1]);

        // 3-4. Координаты
        Float x = parseFloat(str[2], "Координата X");
        Float y = parseFloat(str[3], "Координата Y");

        // 5. Дата создания
        LocalDateTime creationDate = parseDate(str[4]);

        // 6. Рост
        if (str[5] == null || str[5].trim().isEmpty()) {
            throw new IllegalArgumentException("Рост не может быть пустым");
        }
        Long height = Long.parseLong(str[5].trim());
        if (height <= 0) throw new IllegalArgumentException("Рост должен быть > 0");

        // 7. PassportID
        String passportID = (str[6] == null || str[6].isBlank() || str[6].trim().equalsIgnoreCase("null"))
                ? null
                : str[6].trim();

        // 8-9. Enum
        Color hairColor = parseEnum(Color.class, str[7]);
        Country nationality = parseEnum(Country.class, str[8]);

        // 10-12. Локация
        if (str[9] == null || str[9].trim().isEmpty()) throw new IllegalArgumentException("Локация X не может быть пустой");
        if (str[10] == null || str[10].trim().isEmpty()) throw new IllegalArgumentException("Локация Y не может быть пустой");

        long locX = Long.parseLong(str[9].trim());
        double locY = Double.parseDouble(str[10].trim());

        String locName = (str[11] == null || str[11].isBlank() || str[11].trim().equalsIgnoreCase("null"))
                ? null
                : str[11].trim();

        Location location = new Location(locX, locY);
        location.setName(locName);

        Person person = new Person(
                id,
                name,
                new Coordinates(x, y),
                height,
                location
        );

        // Выставляем остальные поля через сеттеры
        person.setCreationDate(creationDate);
        person.setPassportID(passportID);
        person.setNationality(nationality);
        person.setHairColor(hairColor);

        return person;
    }

    /**
     * Форматирует LocalDateTime в строку формата dd.MM.yyyy.
     * Используется при сохранении объекта в файл (CSV) или для вывода в toString().
     *
     * @param date объект даты и времени
     * @return строковое представление даты или "null", если передан null
     */
    public static String formatDate(LocalDateTime date) {
        if (date == null) return "null";
        return date.format(DATE_FORMATTER);
    }
}