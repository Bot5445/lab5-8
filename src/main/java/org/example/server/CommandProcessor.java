package org.example.server;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.server.commands.ICommand;
import org.example.server.db.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Модуль обработки полученных команд.
 * Маршрутизирует запросы к соответствующим исполнителям ICommand.
 */
public class CommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);
    private final Map<String, ICommand> commands;
    private final DatabaseManager dbManager;

    public CommandProcessor(Map<String, ICommand> commands, DatabaseManager dbManager) {
        this.commands = commands;
        this.dbManager = dbManager;
    }

    /**
     * Обрабатывает запрос, вызывая нужную команду.
     * @param request запрос
     * @return ответ
     */
    public Response process(Request request) {
        String cmdName = request.commandName();

        // Разрешаем register и login без строгой проверки, так как пользователь еще не вошел в систему
        if (!cmdName.equals("register") && !cmdName.equals("login")) {
            if (!dbManager.authenticate(request.username(), request.password())) {
                return new Response("Ошибка доступа: Неверный логин или пароль. Выполнение команд запрещено.", ResponseStatus.ERROR);
            }
        }

        ICommand command = commands.get(cmdName);
        if (command == null) {
            logger.warn("Получена неизвестная команда: {}", cmdName);
            return new Response("Неизвестная команда: " + cmdName, ResponseStatus.ERROR);
        }

        try {
            logger.debug("Выполнение команды: {} пользователем: {}", cmdName, request.username());
            return command.execute(request);
        } catch (Exception e) {
            logger.error("Ошибка при выполнении команды {}: {}", cmdName, e.getMessage());
            return new Response("Ошибка на сервере: " + e.getMessage(), ResponseStatus.ERROR);
        }
    }
}