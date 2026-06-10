package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;

import org.example.network.data.ICollManager;
import org.example.network.data.PersonFactory;

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

        // 1. Формируем заголовок со ВСЕМИ полями объекта
        String header = String.format(
                "| %-4s | %-12s | %-12s | %-10s | %-6s | %-10s | %-8s | %-10s | %-15s | %-10s |",
                "ID", "Name", "Coords", "Date", "Height", "Passport", "Color", "Country", "Location", "Owner"
        );
        String separator = "-".repeat(header.length());

        // 2. Используем Stream API: сортируем, форматируем все поля, склеиваем
        String dataLines = collectionManager.getAllPersons().stream()
                .sorted()
                .map(p -> {
                    // Собираем строковые представления для сложных полей
                    String coords = p.getCoordinates().getX() + "," + p.getCoordinates().getY();
                    String date = PersonFactory.formatDate(p.getCreationDate()); // Используем вашу фабрику
                    String passport = p.getPassportID() == null ? "null" : p.getPassportID();
                    String color = p.getHairColor() == null ? "null" : p.getHairColor().name();
                    String country = p.getNationality() == null ? "null" : p.getNationality().name();

                    String locName = p.getLocation().getName() == null ? "null" : p.getLocation().getName();
                    String location = p.getLocation().getX() + "," + p.getLocation().getY() + "," + locName;

                    String owner = p.getOwner() == null ? "null" : p.getOwner();

                    // Форматируем строку с учетом всех полей
                    return String.format(
                            "| %-4d | %-12s | %-12s | %-10s | %-6d | %-10s | %-8s | %-10s | %-15s | %-10s |",
                            p.getId(),
                            truncate(p.getName(), 12),
                            truncate(coords, 12),
                            truncate(date, 10),
                            p.getHeight(),
                            truncate(passport, 10),
                            truncate(color, 8),
                            truncate(country, 10),
                            truncate(location, 15),
                            truncate(owner, 10)
                    );
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