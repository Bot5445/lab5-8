package org.example.client;

import org.example.network.data.Color;
import org.example.network.data.Country;
import org.example.network.data.PersonFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Утилитный класс для пошагового (интерактивного) чтения данных объекта из консоли.
 * <p>В отличие от старой версии, возвращает не CSV-строку, а массив строк {@link String[]},
 * чтобы избежать проблем с разделителями внутри самих данных и упростить сериализацию
 * в объект {@link org.example.network.data.Person} на стороне клиента.</p>
 */
public class PersonInputReader {
    private final Scanner scanner;

    /**
     * Создает ридер ввода данных.
     * @param scanner сканер консоли для чтения пользовательского ввода
     */
    public PersonInputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Запускает интерактивный опрос пользователя для заполнения полей Person.
     * @param args начальные аргументы (если были введены в строке с командой),
     *             могут быть использованы как первые значения. Если null, опрос начинается с первого поля.
     * @return Массив строк со всеми заполненными полями в порядке, ожидаемом {@link PersonFactory}.
     */
    public String[] readPersonData(String args) {
        List<String> fields = new ArrayList<>();
        Queue<String> argQueue = new LinkedList<>();

        if (args != null && !args.isBlank()) {
            Collections.addAll(argQueue, args.split("[,\\s]+"));
        }

        // 1. ID (Генерируется на сервере, передаем null)
        fields.add(null);

        // 2. Имя
        fields.add(readNextArgOrPrompt(argQueue, "Имя (латиница)", isString()));

        // 3-4. Координаты
        fields.add(readField("Координата X (float)", isFloat(x -> true), ".-"));
        fields.add(readField("Координата Y (float)", isFloat(y -> true), ".-"));

        // 5. Дата (Генерируется на сервере, передаем null)
        fields.add(null);

        // 6. Рост
        fields.add(readNextArgOrPrompt(argQueue, "Рост (Long > 0)",
                isNumber(Long::parseLong, h -> h > 0), "-"));

        // 7. PassportID (Nullable)
        String passID = readNextArgOrPrompt(argQueue, "PassportID (пусто = null)", isPassportId());
        fields.add(passID.isEmpty() ? null : passID);

        // 8. Enum Цвет
        fields.add(readFieldEnum("Цвет волос (пусто = null)", Color.class));

        // 9. Enum Страна
        fields.add(readFieldEnum("Национальность (пусто = null)", Country.class));

        // 10-12. Локация
        fields.add(readField("Локация X (long)", isNumber(Long::parseLong), "-"));
        fields.add(readField("Локация Y (double)", isDouble(y -> true), ".-"));
        String locName = readField("Название локации (пусто = null)", s -> true);
        // ИСПРАВЛЕНО: передаем null вместо строкового слова "null"
        fields.add(locName.isEmpty() ? null : locName);

        return fields.toArray(new String[0]);
    }

    private String readField(String prompt, Predicate<String> validator) {
        return readField(prompt, validator, "");
    }

    /**
     * Запрашивает данные у пользователя с поддержкой доп. символов при очистке.
     * @param formatNumber строка с символами, которые нужно сохранить в числе (например, ".-")
     * @param prompt текст для вывода сообщения пользователю
     * @param validator условие валидации
     * @return полученные и очищенные данные от пользователя
     */
    private String readField(String prompt, Predicate<String> validator, String formatNumber) {
        while (true) {
            System.out.print(prompt + ": ");
            String rawInput = scanner.nextLine();
            if (validator.test(rawInput.trim())) {
                return cleinerStr(rawInput, formatNumber);
            }
            // Если валидация не прошла, валидатор уже вывел ошибку. Повторяем цикл.
        }
    }

    /**
     * Пытается взять значение из очереди аргументов. Если очередь пуста или значение не прошло валидацию,
     * запрашивает ввод у пользователя.
     * @param argQueue очередь аргументов из командной строки
     * @param prompt сообщение для пользователя
     * @param validator условие правильности данных
     * @return полученное сообщение
     */
    private String readNextArgOrPrompt(Queue<String> argQueue, String prompt, Predicate<String> validator) {
        return readNextArgOrPrompt(argQueue, prompt, validator, "");
    }

    /**
     * Пытается взять значение из очереди аргументов, с информативной ошибкой.
     * @param argQueue очередь аргументов
     * @param prompt сообщение для пользователя
     * @param validator условие правильности данных
     * @param formatNumber строка с символами, которые нужно сохранить в числе (например, ".-")
     * @return полученное сообщение
     */
    private String readNextArgOrPrompt(Queue<String> argQueue, String prompt,
                                       Predicate<String> validator, String formatNumber) {
        if (!argQueue.isEmpty()) {
            String val = argQueue.poll();
            if (validator.test(val.trim())) {
                return cleinerStr(val, formatNumber);
            }
            System.out.println("Значение для \"" + prompt + "\" некорректно. Требуется ручной ввод.");
        }
        return readField(prompt, validator, formatNumber);
    }

    /**
     * Аналогичен readNextArgOrPrompt, но для Enum-полей с выводом доступных значений.
     * @paramFieldName сообщение для пользователя
     * @param enumClass класс Enum
     * @return полученное значение
     */
    private <E extends Enum<E>> String readFieldEnum(String fieldName, Class<E> enumClass) {
        String availableValues = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
        String prompt = fieldName + " (доступно: " + availableValues + ")";

        Predicate<String> enumValidator = s -> {
            if (s == null || s.isEmpty()) return true;
            try {
                Enum.valueOf(enumClass, s.toUpperCase());
                return true;
            } catch (IllegalArgumentException e) {
                // ИСПРАВЛЕНО: изменен текст ошибки с "формата числа" на "значения"
                System.out.println("Неверное значение: \""+s+"\". Выберите из списка: " + availableValues);
                return false;
            }
        };

        return readField(prompt, enumValidator);
    }

    private static Predicate<String> isString() {
        return s -> {
            try {
                PersonFactory.validateAndParseName(cleinerStr(s));
                return true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        };
    }

    private static <T extends Number> Predicate<String> isNumber(
            Function<String, T> parser,
            Predicate<T> condition,
            boolean allowDecimal) {

        return s -> {
            if (s == null || s.isBlank()) {
                System.out.println("Поле не может быть пустым.");
                return false;
            }

            String normalized = s.trim().replace(',', '.');

            if (!isValidNumberFormat(normalized, allowDecimal)) {
                String hint = allowDecimal
                        ? "Используйте только цифры, точку (.) и знак минус (-) в начале."
                        : "Используйте только целые цифры (точка не допускается).";
                System.out.println("Неверный формат числа: \"" + s.trim() + "\". " + hint);
                return false;
            }

            try {
                T value = parser.apply(normalized);

                if (value instanceof Float fl && (Float.isInfinite(fl) || Float.isNaN(fl))) {
                    System.out.println("Значение \"" + s.trim() + "\" слишком большое.");
                    return false;
                }
                if (value instanceof Double db && (Double.isInfinite(db) || Double.isNaN(db))) {
                    System.out.println("Значение \"" + s.trim() + "\" слишком большое.");
                    return false;
                }

                if (!condition.test(value)) {
                    System.out.println("Значение \"" + s.trim() + "\" не удовлетворяет условию (например, должно быть > 0).");
                    return false;
                }
                return true;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка преобразования числа: \"" + s.trim() + "\".");
                return false;
            }
        };
    }

    private static <T extends Number> Predicate<String> isNumber(Function<String, T> parser) {
        return isNumber(parser, v -> true, false);
    }
    private static <T extends Number> Predicate<String> isNumber(Function<String, T> parser, Predicate<T> condition) {
        return isNumber(parser, condition, false);
    }
    private static Predicate<String> isFloat(Predicate<Float> condition) {
        return isNumber(Float::parseFloat, condition, true);
    }
    private static Predicate<String> isDouble(Predicate<Double> condition) {
        return isNumber(Double::parseDouble, condition, true);
    }

    /**
     * Базовая очистка для текстовых полей (без спецсимволов).
     */
    public static String cleinerStr(String input) {
        return cleinerStr(input, "");
    }

    /**
     * Очищает строку: заменяет запятую на точку, удаляет управляющие символы,
     * оставляет только буквы, цифры, пробелы и указанные доп. символы, схлопывает пробелы.
     * @param input исходная строка
     * @param formatNumber строка с доп. символами, которые нужно сохранить (например, ".-")
     */
    public static String cleinerStr(String input, String formatNumber) {
        if (input == null) return null;

        String normalized = input.replace(',', '.');
        String safeFormat = formatNumber.replaceAll("([.\\-\\[\\]^\\\\])", "\\\\$1");
        String filter = "[^\\p{L}\\p{N}\\s" + safeFormat + "]";

        return normalized.replaceAll("\\p{Cntrl}", " ")
                .replaceAll(filter, "")
                .trim()
                .replaceAll("\\s+", " ");
    }

    /**
     * Проверяет, является ли строка корректным числом.
     * @param s строка
     * @param allowDecimal допускаются ли десятичные дроби
     * @return boolean
     */
    private static boolean isValidNumberFormat(String s, boolean allowDecimal) {
        if (s == null || s.isBlank()) return false;
        String normalized = s.trim().replace(',', '.');
        return normalized.matches(allowDecimal
                ? "^-?(\\d+\\.?\\d*|\\.\\d+)$"
                : "^-?\\d+$");
    }

    /**
     * Валидатор для PassportID: пустая строка → null (допустимо), иначе латиница и цифры.
     * @return predicate для проверки
     */
    private static Predicate<String> isPassportId() {
        return s -> {
            if (s == null || s.isBlank()) return true;
            if (s.trim().matches("^[A-Za-z0-9]{1,32}$")) return true;
            System.out.println("PassportID должен содержать только латиницу и цифры (1-32 символа).");
            return false;
        };
    }
}