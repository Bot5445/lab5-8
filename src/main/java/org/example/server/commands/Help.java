package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;

import java.util.Map;

import static java.lang.String.format;

/**
 * Команда для вывода справки по всем доступным командам приложения.
 * Динамически формирует список на основе переданного реестра команд.
 */
public class Help implements ICommand {
    //    private CommandExecutor executor;
    private final Map<String, ICommand> cmds;

    /**
     * Создает команду помощи.
     * @param cmds отображение названий команд на их реализации
     */
    public Help(Map<String, ICommand> cmds) {
        this.cmds = cmds;
    }

    /**
     * Возвращает название команды.
     * @return строка "help"
     */
    @Override
    public String getName() {
        return "help";
    }

    /**
     * Формирует и возвращает список всех команд с их описаниями.
     * @param request аргументы (не используются)
     * @return отформатированный список команд
     */
    @Override
    public Response execute(Request request) {
        StringBuilder str = new StringBuilder();
        str.append("Список команд:\n");

        // 1. Выводим команды БЕЗ нижнего подчеркивания (простые команды)
        cmds.entrySet().stream()
                .filter(entry -> !entry.getKey().contains("_"))
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> str.append(format("  %-25s - %s%n", entry.getKey(), entry.getValue().getDescription())));

        str.append("\n"); // Пустая строка-разделитель

        // 2. Выводим команды С нижним подчеркиванием (составные команды)
        cmds.entrySet().stream()
                .filter(entry -> entry.getKey().contains("_"))
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> str.append(format("  %-25s - %s%n", entry.getKey(), entry.getValue().getDescription())));

        return new Response(str.toString());
    }

    /**
     * Возвращает описание команды для справки.
     * @return текстовое описание команды
     */
    @Override
    public String getDescription() {
        return "выводит справку по доступным командам";
    }

    /**
     * Команда help не принимает аргументов
     * @return выводит false
     */
    @Override
    public boolean acceptsArguments() {
        return false;
    }
}
