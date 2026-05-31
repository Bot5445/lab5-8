package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;

/**
 * Базовый интерфейс для всех команд приложения.
 * Определяет контракт для выполнения команды, получения её имени и описания.
 */
public interface ICommand {
    /**
     * Возвращает ключевое слово (имя) команды, по которому она вызывается пользователем.
     * @return имя команды (например, "clear", "insert")
     */
    String getName();

    /**
     * Выполняет логику команды.
     * @param request объектный запрос от клиента
     * @return объектный ответ для клиента
     */
    Response execute(Request request);

    /**
     * Возвращает текстовое описание команды для вывода в справке.
     * @return описание команды
     */
    String getDescription();

    /**
     * Определяет, требует ли команда ввода составного объекта (например, Person)
     * в интерактивном режиме, а не просто передачи аргументов в одной строке.
     * Если возвращает true, {@link CommandExecutor} запустит диалог опроса полей.
     * @return true, если команда требует интерактивного ввода составных данных, иначе false
     */
    default boolean requiresCompoundDataInput() {
        return false;
    }

    /**
     * Определяет, принимает ли команда аргументы (текст после названия команды).
     * Если возвращает false, то при вводе любых аргументов будет выдана ошибка.
     * По умолчанию true (аргументы разрешены).
     */
    default boolean acceptsArguments() {
        return true;
    }
}