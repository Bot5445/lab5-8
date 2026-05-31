package org.example.server;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.server.commands.ICommand;
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

    public CommandProcessor(Map<String, ICommand> commands) {
        this.commands = commands;
    }

    /**
     * Обрабатывает запрос, вызывая нужную команду.
     * @param request запрос
     * @return ответ
     */
    public Response process(Request request) {
        ICommand command = commands.get(request.getCommandName());
        if (command == null) {
            logger.warn("Получена неизвестная команда: {}", request.getCommandName());
            return new Response("Неизвестная команда: " + request.getCommandName(), ResponseStatus.ERROR);
        }
        try {
            // Логируем начало выполнения команды
            logger.debug("Выполнение команды: {}", request.getCommandName());
            return command.execute(request);
        } catch (Exception e) {
            logger.error("Ошибка при выполнении команды {}: {}", request.getCommandName(), e.getMessage());
            return new Response("Ошибка на сервере: " + e.getMessage(), ResponseStatus.ERROR);
        }
    }
}