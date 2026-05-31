package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;

import org.example.network.data.ICollManager;

import java.util.stream.Collectors;

/**
 * Команда для вывода всех элементов коллекции в виде форматированной таблицы.
 * Если коллекция пуста, выводит соответствующее сообщение.
 */
public class Show implements ICommand {
    private final ICollManager collectionManager;

    /**
     * Создает команду вывода содержимого коллекции.
     *
     * @param collectionManager менеджер, предоставляющий доступ к коллекции
     */
    public Show(ICollManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Возвращает название команды.
     *
     * @return строка "show"
     */
    @Override
    public String getName() {
        return "show";
    }

    /**
     * Выполняет формирование и вывод таблицы элементов.
     * Метод определяет ширину колонок динамически на основе заголовков и данных,
     * ограничивая максимальную ширину для удобства чтения.
     *
     * @param request аргументы команды (не используются в данной реализации)
     * @return строка с отформатированной таблицей или сообщение о том, что коллекция пуста
     */
    @Override
    public Response execute(Request request) {
        if (collectionManager.isEmpty()) {
            return new Response("Коллекция пуста.", ResponseStatus.OK);
        }

        // 1. Формируем заголовок с фиксированной шириной столбцов
        String header = String.format("| %-5s | %-15s | %-10s | %-15s |", "ID", "Имя", "Рост", "Паспорт");
        String separator = "-".repeat(55);

        // 2. Используем Stream API: сортируем (по compareTo в Person), форматируем, склеиваем
        String dataLines = collectionManager.getAllPersons().stream()
                .sorted() // Использует ваш метод compareTo (по росту, затем по ID)
                .map(p -> {
                    // Обрезаем длинные значения (truncate), чтобы не ломать таблицу
                    String name = truncate(p.getName(), 15);
                    String passport = truncate(p.getPassportID() == null ? "null" : p.getPassportID(), 15);
                    return String.format("| %-5d | %-15s | %-10d | %-15s |", p.getId(), name, p.getHeight(), passport);
                })
                .collect(Collectors.joining("\n"));

        // 3. Склеиваем всё в одну красивую строку и отправляем клиенту
        String result = separator + "\n" + header + "\n" + separator + "\n" + dataLines + "\n" + separator;

        return new Response(result, ResponseStatus.OK);
    }

    /**
     * Утилитный метод для обрезки слишком длинных строк.
     * Заменяет старый prettify/truncate.
     * Работает по принципу: если строка длиннее maxLen — обрезаем и ставим '...'
     */
    private String truncate(String value, int maxLen) {
        if (value == null) return "null";
        if (value.length() <= maxLen) return value;
        return value.substring(0, maxLen - 3) + "...";
    }

    /**
     * Возвращает описание команды для справки.
     *
     * @return текстовое описание команды
     */
    @Override
    public String getDescription() {
        return "выводит все элементы коллекции в строковом представлении";
    }

    /**
     * Команда show не принимает аргументов
     * @return выводит false
     */
    @Override
    public boolean acceptsArguments() {
        return false;
    }
}