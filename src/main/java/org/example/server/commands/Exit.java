package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;

/**
 * Завершает программу (без сохранения в файл), с помощью вывода строки exit
 */
public class Exit implements ICommand {
    /**
     * @return название
     */
    @Override
    public String getName() {
        return "exit";
    }

    /**
     * @return exit чтобы завершилась программа
     */
    @Override
    public Response execute(Request args) {
        return new Response("exit");
    }

    /**
     * @return описание
     */
    @Override
    public String getDescription() {
        return "завершает программу (без сохранения в файл)";
    }

    /**
     * Команда exit не принимает аргументов
     * @return выводит false
     */
    @Override
    public boolean acceptsArguments() {
        return false;
    }
}
